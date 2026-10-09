package com.freighttiger.driverassistant.core.network

import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.core.model.DriverSession
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.Milestone
import com.freighttiger.driverassistant.core.model.OutboxStatus
import com.freighttiger.driverassistant.core.model.PromptType
import com.freighttiger.driverassistant.core.model.SubmissionState
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.core.model.TripStatus
import com.freighttiger.driverassistant.core.model.Utterance
import com.freighttiger.driverassistant.core.network.mock.ConsentValidationMode
import com.freighttiger.driverassistant.core.network.mock.MockFreightTigerBackend
import com.freighttiger.driverassistant.domain.backend.BackendResult
import com.freighttiger.driverassistant.domain.conversation.DialogueEngine
import com.freighttiger.driverassistant.domain.conversation.DialogueStep
import com.freighttiger.driverassistant.domain.conversation.TapResponse
import com.freighttiger.driverassistant.domain.inmemory.InMemoryAccessTokenStore
import com.freighttiger.driverassistant.domain.inmemory.InMemoryActivityLog
import com.freighttiger.driverassistant.domain.inmemory.InMemoryConsentRepository
import com.freighttiger.driverassistant.domain.inmemory.InMemoryInboundEventRepository
import com.freighttiger.driverassistant.domain.inmemory.InMemoryOutboxRepository
import com.freighttiger.driverassistant.domain.inmemory.InMemoryPromptRepository
import com.freighttiger.driverassistant.domain.inmemory.InMemorySessionRepository
import com.freighttiger.driverassistant.domain.inmemory.InMemoryTripRepository
import com.freighttiger.driverassistant.domain.inmemory.ManualConnectivityMonitor
import com.freighttiger.driverassistant.domain.inmemory.MutableTimeSource
import com.freighttiger.driverassistant.domain.inmemory.SequentialIdGenerator
import com.freighttiger.driverassistant.domain.onboarding.OnboardingFlow
import com.freighttiger.driverassistant.domain.onboarding.OnboardingStep
import com.freighttiger.driverassistant.domain.sync.BackoffPolicy
import com.freighttiger.driverassistant.domain.sync.OutboxSyncEngine
import com.freighttiger.driverassistant.domain.voice.HindiPromptCatalog
import com.freighttiger.driverassistant.domain.voice.RecognitionResult
import com.freighttiger.driverassistant.domain.voice.RuleBasedIntentClassifier
import com.freighttiger.driverassistant.domain.workflow.ActivityRecorder
import com.freighttiger.driverassistant.domain.workflow.ProcessingResult
import com.freighttiger.driverassistant.domain.workflow.PromptScheduler
import com.freighttiger.driverassistant.domain.workflow.TripCoordinator
import com.freighttiger.driverassistant.domain.workflow.TripEventProcessor
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

/**
 * End-to-end scenarios through the real domain components and the simulated backend:
 * push event → processor → prompt → dialogue (voice/tap) → coordinator → outbox → sync → backend
 * → backend confirmation event → processor.
 */
class DriverAssistantScenarioTest {
    private val clock = MutableTimeSource(Instant.parse("2026-10-09T12:00:00Z"))
    private val ids = SequentialIdGenerator()
    private val mock = MockFreightTigerBackend(clock, ids)
    private val sessions = InMemorySessionRepository()
    private val trips = InMemoryTripRepository()
    private val consents = InMemoryConsentRepository()
    private val outbox = InMemoryOutboxRepository()
    private val prompts = InMemoryPromptRepository()
    private val log = InMemoryActivityLog()
    private val activity = ActivityRecorder(log, ids, clock)
    private val connectivity = ManualConnectivityMonitor(true)
    private val scheduler = PromptScheduler(prompts, ids, clock)
    private val processor = TripEventProcessor(sessions, trips, consents, InMemoryInboundEventRepository(), outbox, scheduler, activity, clock)
    private val coordinator = TripCoordinator(sessions, trips, consents, outbox, scheduler, activity, ids, clock)
    private val sync = OutboxSyncEngine(outbox, mock, connectivity, trips, consents, scheduler, activity, clock, BackoffPolicy(jitter = 0.0), Random(0))
    private val dialogue = DialogueEngine(RuleBasedIntentClassifier(), HindiPromptCatalog())
    private var pumped = 0

    /** Deliver simulated push events to the processor (what the app's collector does). */
    private suspend fun pump(): List<ProcessingResult> {
        val events = mock.emitted.value
        val results = events.drop(pumped).map { processor.process(it) }
        pumped = events.size
        return results
    }

    private suspend fun onboard(): DriverSession {
        val flow = OnboardingFlow(mock, mock, sessions, trips, InMemoryAccessTokenStore(), clock)
        flow.start()
        flow.onPhoneChanged("9876543210")
        flow.submitPhone()
        flow.submitOtp(MockFreightTigerBackend.DEMO_OTP)
        flow.confirmIdentity(true); flow.confirmLanguage(); flow.continueFromPermissions(); flow.acceptPrivacy(); flow.finish()
        assertEquals(OnboardingStep.DONE, flow.state.value.step)
        return sessions.current()!!
    }

    private suspend fun assignTrip(): Trip {
        mock.simulateTripAssigned(onboard().profile.driverId)
        pump()
        return trips.activeTrip()!!
    }

    /** Run a prompt through the dialogue engine with spoken answers; returns the final step. */
    private suspend fun converse(prompt: AssistantPrompt, vararg answers: String): DialogueStep {
        var step = dialogue.start(prompt, trips.get(prompt.tripId))
        if (prompt.type == PromptType.CONSENT) coordinator.onConsentPromptDelivered(prompt.consentRequestId!!)
        scheduler.markDelivered(prompt.promptId)
        for (a in answers) {
            if (step !is DialogueStep.Ask) break
            step = dialogue.onRecognition(step.session, RecognitionResult.Recognized(Utterance(a)))
        }
        if (step is DialogueStep.Complete) coordinator.applyOutcome(prompt, step.outcome)
        if (step is DialogueStep.Defer) coordinator.deferPrompt(prompt)
        return step
    }

    private suspend fun duePrompt(type: PromptType) = scheduler.nextDue().also { assertEquals(type, it?.type) }!!

    @Test
    fun `receiving a trip assignment gives a short spoken briefing`() = runTest {
        val trip = assignTrip()
        assertTrue(trip.simulated)
        val step = dialogue.start(duePrompt(PromptType.TRIP_BRIEFING), trip) as DialogueStep.Complete
        assertTrue(step.speech.contains(trip.loadingPoint.name))
        assertTrue(!step.speech.contains("9876543210"))
    }

    @Test
    fun `completing a consent flow - tracking only after backend confirmation`() = runTest {
        val trip = assignTrip()
        scheduler.complete(duePrompt(PromptType.TRIP_BRIEFING).promptId)
        mock.simulateConsentRequest(trip.tripId); pump()
        val prompt = duePrompt(PromptType.CONSENT)

        assertTrue(converse(prompt, "Haan") is DialogueStep.Complete)
        val requestId = prompt.consentRequestId!!
        assertEquals(ConsentState.GRANTED_PENDING_VALIDATION, consents.get(requestId)!!.state)
        assertEquals(SubmissionState.CAPTURED_LOCALLY, consents.get(requestId)!!.submissionState)

        sync.syncDue()
        assertEquals(SubmissionState.ACCEPTED, consents.get(requestId)!!.submissionState)
        assertEquals(ConsentState.GRANTED_PENDING_VALIDATION, consents.get(requestId)!!.state)
        assertEquals(TrackingStatus.NOT_STARTED, trips.get(trip.tripId)!!.trackingStatus)

        mock.simulateActivationResult(requestId, success = true); pump()
        assertEquals(ConsentState.GRANTED, consents.get(requestId)!!.state)
        assertEquals(TrackingStatus.ACTIVE, trips.get(trip.tripId)!!.trackingStatus)
        val info = duePrompt(PromptType.TRACKING_RESULT)
        assertEquals("ACTIVE", info.detail)
    }

    @Test
    fun `simulated activation failure`() = runTest {
        val trip = assignTrip()
        mock.updateControls { it.copy(consentValidationMode = ConsentValidationMode.AUTO_REJECT) }
        mock.simulateConsentRequest(trip.tripId); pump()
        val prompt = prompts.all().first { it.type == PromptType.CONSENT }
        converse(prompt, "ji haan")
        sync.syncDue(); pump()
        assertEquals(ConsentState.VALIDATION_FAILED, consents.get(prompt.consentRequestId!!)!!.state)
        assertEquals(TrackingStatus.FAILED, trips.get(trip.tripId)!!.trackingStatus)
    }

    @Test
    fun `declining consent never starts tracking`() = runTest {
        val trip = assignTrip()
        mock.updateControls { it.copy(consentValidationMode = ConsentValidationMode.AUTO_APPROVE) }
        mock.simulateConsentRequest(trip.tripId); pump()
        val prompt = prompts.all().first { it.type == PromptType.CONSENT }
        converse(prompt, "Nahi")
        sync.syncDue(); pump()
        assertEquals(ConsentState.DECLINED, consents.get(prompt.consentRequestId!!)!!.state)
        assertEquals(TrackingStatus.NOT_STARTED, trips.get(trip.tripId)!!.trackingStatus)
        // Even a forced activation attempt from the (simulated) backend is refused.
        assertEquals(null, mock.simulateActivationResult(prompt.consentRequestId!!, true))
    }

    @Test
    fun `clarifying an ambiguous answer`() = runTest {
        val trip = assignTrip()
        mock.simulateConsentRequest(trip.tripId); pump()
        val prompt = prompts.all().first { it.type == PromptType.CONSENT }
        val step = converse(prompt, "theek hai", "haan main sahmat hoon")
        assertTrue(step is DialogueStep.Complete)
        assertEquals(ConsentState.GRANTED_PENDING_VALIDATION, consents.get(prompt.consentRequestId!!)!!.state)
    }

    @Test
    fun `silence defers and never grants`() = runTest {
        val trip = assignTrip()
        mock.simulateConsentRequest(trip.tripId); pump()
        val prompt = prompts.all().first { it.type == PromptType.CONSENT }
        var step = dialogue.start(prompt, trip)
        repeat(2) { step = dialogue.onRecognition(step.session, RecognitionResult.Error(com.freighttiger.driverassistant.domain.voice.SpeechErrorKind.SILENCE)) }
        assertTrue(step is DialogueStep.Defer)
        coordinator.deferPrompt(prompt)
        assertEquals(ConsentState.REQUESTED, consents.get(prompt.consentRequestId!!)!!.state)
        assertTrue(outbox.all().none { it.payload.eventType.name == "CONSENT_RESPONSE_CAPTURED" })
    }

    @Test
    fun `reporting an ETA by voice`() = runTest {
        val trip = assignTrip()
        mock.simulateMilestone(trip.tripId, Milestone.ETA_TO_LOADING_POINT); pump()
        val prompt = prompts.all().first { it.type == PromptType.ETA }
        converse(prompt, "Haan", "Do ghante")
        assertEquals(120, trips.get(trip.tripId)!!.driverReportedEta!!.minutes)
        assertEquals(1, sync.syncDue().acknowledged)
        assertEquals("ETA_REPORTED", mock.submissions.value.last().eventType)
    }

    @Test
    fun `confirming loading arrival then loading status by tap`() = runTest {
        val trip = assignTrip()
        mock.simulateMilestone(trip.tripId, Milestone.ARRIVAL_AT_LOADING_POINT); pump()
        converse(prompts.all().first { it.type == PromptType.ARRIVAL }, "Main pahunch gaya")
        assertEquals(LoadingStatus.REACHED_LOADING_POINT, trips.get(trip.tripId)!!.loadingStatus)
        assertEquals(null, trips.get(trip.tripId)!!.verifiedArrival)

        mock.simulateMilestone(trip.tripId, Milestone.LOADING_STATUS); pump()
        val loading = prompts.all().first { it.type == PromptType.LOADING_STATUS }
        val step = dialogue.onTap((dialogue.start(loading, trip) as DialogueStep.Ask).session, TapResponse.Loading(LoadingStatus.LOADING_STARTED))
        coordinator.applyOutcome(loading, (step as DialogueStep.Complete).outcome)
        val report = sync.syncDue()
        assertEquals(2, report.acknowledged)
        assertTrue(outbox.all().all { it.status == OutboxStatus.ACKNOWLEDGED })
    }

    @Test
    fun `recovering after network loss`() = runTest {
        val trip = assignTrip()
        mock.simulateConsentRequest(trip.tripId); pump()
        connectivity.set(false)
        converse(prompts.all().first { it.type == PromptType.CONSENT }, "haan")
        coordinator.reportEta(trip.tripId, 45, false, com.freighttiger.driverassistant.core.model.CaptureMethod.TAP)
        assertTrue(sync.syncDue().offline)
        assertEquals(2, outbox.all().count { it.status == OutboxStatus.PENDING })

        // Server down even after reconnecting: retry scheduled, nothing lost.
        connectivity.set(true)
        mock.updateControls { it.copy(serverReachable = false) }
        sync.syncDue()
        assertTrue(outbox.all().all { it.status == OutboxStatus.RETRY_SCHEDULED })

        mock.updateControls { it.copy(serverReachable = true) }
        clock.advance(Duration.ofSeconds(6))
        assertEquals(2, sync.syncDue().acknowledged)
    }

    @Test
    fun `duplicate push does not trigger duplicate backend actions`() = runTest {
        val trip = assignTrip()
        mock.simulateConsentRequest(trip.tripId); pump()
        mock.resendLastEvent()
        val results = pump()
        assertTrue(results.single() is ProcessingResult.Duplicate)
        assertEquals(1, prompts.all().count { it.type == PromptType.CONSENT })
        converse(prompts.all().first { it.type == PromptType.CONSENT }, "haan")
        sync.syncDue()
        // Same idempotency key resubmitted is acknowledged as a duplicate, not re-applied.
        val event = outbox.all().single { it.payload.eventType.name == "CONSENT_RESPONSE_CAPTURED" }
        val again = mock.submit(event)
        assertEquals(com.freighttiger.driverassistant.domain.backend.AckStatus.DUPLICATE, (again as BackendResult.Success).value.status)
        assertEquals(1, mock.submissions.value.count { it.outcome == "ACCEPTED" && it.eventType == "CONSENT_RESPONSE_CAPTURED" })
    }

    @Test
    fun `consent expired while offline is not silently reused`() = runTest {
        val trip = assignTrip()
        mock.simulateConsentRequest(trip.tripId, Duration.ofMinutes(5)); pump()
        val prompt = prompts.all().first { it.type == PromptType.CONSENT }
        connectivity.set(false)
        converse(prompt, "haan")
        clock.advance(Duration.ofMinutes(20))
        connectivity.set(true)
        sync.syncDue(); pump()
        val c = consents.get(prompt.consentRequestId!!)!!
        assertEquals(ConsentState.EXPIRED, c.state)
        assertEquals("EXPIRED_NEEDS_REVIEW", c.reasonCode)
        assertEquals(TrackingStatus.NOT_STARTED, trips.get(trip.tripId)!!.trackingStatus)
    }

    @Test
    fun `trip cancelled while consent pending blocks activation`() = runTest {
        val trip = assignTrip()
        mock.simulateConsentRequest(trip.tripId); pump()
        val prompt = prompts.all().first { it.type == PromptType.CONSENT }
        converse(prompt, "haan")
        sync.syncDue()
        mock.simulateTripCancelled(trip.tripId); pump()
        assertEquals(TripStatus.CANCELLED, trips.get(trip.tripId)!!.status)
        assertEquals(null, mock.simulateActivationResult(prompt.consentRequestId!!, true))
        assertTrue(trips.get(trip.tripId)!!.trackingStatus != TrackingStatus.ACTIVE)
        assertNotNull(prompts.all().firstOrNull { it.type == PromptType.TRIP_CANCELLED })
    }
}
