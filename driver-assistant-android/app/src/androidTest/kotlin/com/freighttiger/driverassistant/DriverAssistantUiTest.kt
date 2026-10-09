package com.freighttiger.driverassistant

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.core.model.Milestone
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.core.network.mock.MockFreightTigerBackend
import com.freighttiger.driverassistant.domain.ports.ConsentRepository
import com.freighttiger.driverassistant.domain.ports.OutboxRepository
import com.freighttiger.driverassistant.domain.ports.TripRepository
import com.freighttiger.driverassistant.platform.connectivity.AndroidConnectivityMonitor
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class DriverAssistantUiTest : BaseUiTest() {

    @Inject lateinit var trips: TripRepository
    @Inject lateinit var consents: ConsentRepository
    @Inject lateinit var outbox: OutboxRepository
    @Inject lateinit var connectivity: AndroidConnectivityMonitor

    private fun assignTrip() = runBlocking {
        mock.simulateTripAssigned()
        compose.waitUntil(5_000) { runBlocking { trips.activeTrip() } != null }
    }

    private fun consentId(): String = runBlocking { consents.forTrip(trips.activeTrip()!!.tripId).first().consentRequestId }

    @Test
    fun firstTimeOnboarding_reachesHome() {
        launch()
        clickTag("welcome_start")
        tag("phone_input").performTextInput("9876543210")
        clickTag("send_otp")
        tag("otp_input").performTextInput(MockFreightTigerBackend.DEMO_OTP)
        clickTag("verify_otp")
        clickTag("identity_confirm")
        clickTag("language_continue")
        clickTag("permissions_continue")
        clickTag("accept_privacy")
        clickTag("finish_onboarding")
        tag("talk_button").assertIsDisplayed()
        assertTrue(settings.current.onboardingComplete)
    }

    @Test
    fun receivingTripAssignment_showsTripOnHome() {
        launchOnboarded()
        assignTrip()
        waitForText(str(R.string.route_from_to, "पुणे", "दिल्ली"))
        // Simulated trips are always labelled as such.
        waitForText(str(R.string.simulated_badge))
    }

    @Test
    fun completingConsentFlow_tracksOnlyAfterBackendConfirmation() {
        launchOnboarded()
        assignTrip()
        runBlocking { mock.simulateConsentRequest(trips.activeTrip()!!.tripId) }
        clickText(str(R.string.menu_consent))
        clickTag("consent_yes")
        waitForText(str(R.string.voice_result_queued))
        compose.waitUntil(5_000) { runBlocking { consents.get(consentId())?.state } == ConsentState.GRANTED_PENDING_VALIDATION }
        assertEquals(TrackingStatus.NOT_STARTED, runBlocking { trips.activeTrip()!!.trackingStatus })

        // Backend (simulated) confirms all checks passed.
        compose.waitUntil(5_000) { runBlocking { outbox.all().any { it.payload.eventType.name == "CONSENT_RESPONSE_CAPTURED" && it.status.name == "ACKNOWLEDGED" } } }
        runBlocking { mock.simulateActivationResult(consentId(), success = true) }
        compose.waitUntil(5_000) { runBlocking { trips.activeTrip()!!.trackingStatus } == TrackingStatus.ACTIVE }
        waitForText(str(R.string.tracking_active))
    }

    @Test
    fun decliningConsent_neverStartsTracking() {
        launchOnboarded()
        assignTrip()
        runBlocking { mock.simulateConsentRequest(trips.activeTrip()!!.tripId) }
        clickText(str(R.string.menu_consent))
        clickTag("consent_no")
        compose.waitUntil(5_000) { runBlocking { consents.get(consentId())?.state } == ConsentState.DECLINED }
        assertEquals(TrackingStatus.NOT_STARTED, runBlocking { trips.activeTrip()!!.trackingStatus })
        assertEquals(null, runBlocking { mock.simulateActivationResult(consentId(), true) })
    }

    @Test
    fun clarifyingAnAmbiguousAnswer_byVoice() {
        launchOnboarded()
        assignTrip()
        runBlocking { mock.simulateConsentRequest(trips.activeTrip()!!.tripId) }
        speechIn.say("theek hai", "haan main sahmat hoon")
        // Driver starts the conversation explicitly.
        clickTag("talk_button")
        compose.waitUntil(8_000) { runBlocking { consents.get(consentId())?.state } == ConsentState.GRANTED_PENDING_VALIDATION }
        assertTrue(speechOut.spoken.any { it.contains("साफ़ समझ नहीं पाया") })
    }

    @Test
    fun silence_neverGrantsConsent() {
        launchOnboarded()
        assignTrip()
        runBlocking { mock.simulateConsentRequest(trips.activeTrip()!!.tripId) }
        clickTag("talk_button")
        // Fake recogniser returns silence for every turn.
        waitForText(str(R.string.voice_result_deferred), 8_000)
        assertTrue(runBlocking { consents.get(consentId())!!.state }.acceptsResponse)
    }

    @Test
    fun reportingEta_byTap() {
        launchOnboarded()
        assignTrip()
        clickText(str(R.string.menu_report))
        clickTag("eta_60")
        waitForText(str(R.string.report_queued))
        assertEquals(60, runBlocking { trips.activeTrip()!!.driverReportedEta?.minutes })
    }

    @Test
    fun confirmingLoadingArrival_byVoiceThenTap() {
        launchOnboarded()
        assignTrip()
        runBlocking { mock.simulateMilestone(trips.activeTrip()!!.tripId, Milestone.ARRIVAL_AT_LOADING_POINT) }
        speechIn.say("main pahunch gaya")
        clickTag("talk_button")
        compose.waitUntil(8_000) { runBlocking { trips.activeTrip()!!.driverReportedArrival?.arrived } == true }
        assertEquals(null, runBlocking { trips.activeTrip()!!.verifiedArrival })
    }

    @Test
    fun recoveringAfterNetworkLoss() {
        launchOnboarded()
        assignTrip()
        connectivity.setSimulatedOffline(true)
        clickText(str(R.string.menu_report))
        clickTag("eta_30")
        waitForText(str(R.string.offline_banner))
        assertTrue(runBlocking { outbox.all().any { it.status.isOpen } })

        connectivity.setSimulatedOffline(false)
        compose.waitUntil(8_000) { runBlocking { outbox.all().none { it.status.isOpen } } }
        compose.onNodeWithTag("eta_30").assertIsDisplayed()
    }
}
