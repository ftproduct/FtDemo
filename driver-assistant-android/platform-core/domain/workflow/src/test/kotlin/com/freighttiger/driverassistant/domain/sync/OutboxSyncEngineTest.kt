package com.freighttiger.driverassistant.domain.sync

import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.ConsentDecision
import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.core.model.ConsentValidationStatus
import com.freighttiger.driverassistant.core.model.OutboxStatus
import com.freighttiger.driverassistant.core.model.PromptType
import com.freighttiger.driverassistant.core.model.SubmissionState
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.domain.backend.AckStatus
import com.freighttiger.driverassistant.domain.backend.BackendError
import com.freighttiger.driverassistant.domain.backend.BackendResult
import com.freighttiger.driverassistant.domain.backend.SubmissionAck
import com.freighttiger.driverassistant.domain.support.T0
import com.freighttiger.driverassistant.domain.support.TRIP
import com.freighttiger.driverassistant.domain.support.TestHarness
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class OutboxSyncEngineTest {

    @Test
    fun `queued is not delivered - offline keeps events pending`() = runTest {
        val h = TestHarness(online = false)
        h.processor.process(h.assigned())
        h.coordinator.reportEta(TRIP, 60, false, CaptureMethod.VOICE)
        val report = h.sync.syncDue()
        assertTrue(report.offline)
        assertEquals(0, h.backend.submitted.size)
        assertEquals(OutboxStatus.PENDING, h.outbox.all().single().status)
    }

    @Test
    fun `recovering after network loss delivers queued events in order`() = runTest {
        val h = TestHarness(online = false)
        h.withConsentRequest()
        h.coordinator.submitConsentDecision("consent-789", ConsentDecision.GRANTED, CaptureMethod.VOICE, "haan")
        h.clock.advance(Duration.ofSeconds(5))
        h.coordinator.reportEta(TRIP, 60, false, CaptureMethod.VOICE)
        h.sync.syncDue()
        h.connectivity.set(true)
        val report = h.sync.syncDue()
        assertEquals(2, report.acknowledged)
        assertEquals(listOf("CONSENT_RESPONSE_CAPTURED", "ETA_REPORTED"), h.backend.submitted.map { it.payload.eventType.name })
        assertTrue(h.outbox.all().all { it.status == OutboxStatus.ACKNOWLEDGED && it.acknowledgedAt != null })
        val consent = h.consents.get("consent-789")!!
        assertEquals(SubmissionState.ACCEPTED, consent.submissionState)
        // Accepted is not validated: still pending, tracking not active.
        assertEquals(ConsentState.GRANTED_PENDING_VALIDATION, consent.state)
        assertEquals(TrackingStatus.NOT_STARTED, h.trips.get(TRIP)?.trackingStatus)
    }

    @Test
    fun `retryable failures back off exponentially and escalate after max attempts`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        h.coordinator.reportEta(TRIP, 60, false, CaptureMethod.VOICE)
        repeat(4) { h.backend.responses.addLast(BackendResult.Failure(BackendError.Server(503))) }

        h.sync.syncDue()
        var e = h.outbox.all().single()
        assertEquals(OutboxStatus.RETRY_SCHEDULED, e.status)
        assertEquals(h.clock.now().plusSeconds(5), e.nextAttemptAt)

        // Not due yet: nothing is sent.
        h.sync.syncDue()
        assertEquals(1, h.backend.submitted.size)

        h.clock.advance(Duration.ofSeconds(5)); h.sync.syncDue()
        e = h.outbox.all().single()
        assertEquals(h.clock.now().plusSeconds(10), e.nextAttemptAt)
        h.clock.advance(Duration.ofSeconds(10)); h.sync.syncDue()
        h.clock.advance(Duration.ofSeconds(20)); h.sync.syncDue()
        e = h.outbox.all().single()
        assertEquals(OutboxStatus.FAILED_MAX_RETRIES, e.status)
        assertEquals(4, e.attempts)
        assertTrue(h.log.entries.any { it.detail.contains("FAILED_MAX_RETRIES") })

        // Driver-initiated retry re-queues it and it then succeeds.
        assertEquals(1, h.sync.retryFailed())
        h.sync.syncDue()
        assertEquals(OutboxStatus.ACKNOWLEDGED, h.outbox.all().single().status)
    }

    @Test
    fun `rate limit honours Retry-After`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        h.coordinator.reportEta(TRIP, 60, false, CaptureMethod.VOICE)
        h.backend.responses.addLast(BackendResult.Failure(BackendError.RateLimited(120)))
        h.sync.syncDue()
        assertEquals(h.clock.now().plusSeconds(120), h.outbox.all().single().nextAttemptAt)
    }

    @Test
    fun `backend validation failure on consent is permanent and informs driver`() = runTest {
        val h = TestHarness()
        h.withConsentRequest()
        h.coordinator.submitConsentDecision("consent-789", ConsentDecision.GRANTED, CaptureMethod.VOICE, "haan")
        h.backend.responses.addLast(BackendResult.Failure(BackendError.Validation("INVALID_ASSOCIATION")))
        val report = h.sync.syncDue()
        assertEquals(1, report.failedPermanently)
        assertEquals(OutboxStatus.FAILED_PERMANENT, h.outbox.all().single().status)
        val consent = h.consents.get("consent-789")!!
        assertEquals(ConsentState.VALIDATION_FAILED, consent.state)
        assertEquals(TrackingStatus.NOT_STARTED, h.trips.get(TRIP)?.trackingStatus)
        assertTrue(h.prompts.all().any { it.type == PromptType.TRACKING_RESULT && it.detail == "FAILED" })
        // Retrying a permanent failure does not resend it.
        h.sync.syncDue()
        assertEquals(1, h.backend.submitted.size)
    }

    @Test
    fun `ack carrying VALIDATED moves consent to GRANTED but not tracking`() = runTest {
        val h = TestHarness()
        h.withConsentRequest()
        h.backend.defaultValidation = ConsentValidationStatus.VALIDATED
        h.coordinator.submitConsentDecision("consent-789", ConsentDecision.GRANTED, CaptureMethod.VOICE, "haan")
        h.sync.syncDue()
        assertEquals(ConsentState.GRANTED, h.consents.get("consent-789")?.state)
        assertEquals(TrackingStatus.NOT_STARTED, h.trips.get(TRIP)?.trackingStatus)
    }

    @Test
    fun `consent captured before expiry but synced after is sent with original time and judged by backend`() = runTest {
        val h = TestHarness(online = false)
        h.withConsentRequest(validFor = Duration.ofMinutes(5))
        val capturedAt = h.clock.now()
        h.coordinator.submitConsentDecision("consent-789", ConsentDecision.GRANTED, CaptureMethod.VOICE, "haan")
        h.clock.advance(Duration.ofMinutes(30))
        h.connectivity.set(true)
        h.backend.defaultValidation = ConsentValidationStatus.EXPIRED_NEEDS_REVIEW
        h.sync.syncDue()
        assertEquals(capturedAt, h.backend.submitted.single().payload.capturedAt)
        assertEquals(ConsentState.EXPIRED, h.consents.get("consent-789")?.state)
        assertEquals(TrackingStatus.NOT_STARTED, h.trips.get(TRIP)?.trackingStatus)
    }

    @Test
    fun `events for a trip cancelled while offline are not sent`() = runTest {
        val h = TestHarness(online = false)
        h.processor.process(h.assigned())
        h.coordinator.reportEta(TRIP, 60, false, CaptureMethod.VOICE)
        h.coordinator.requestSupport(TRIP, com.freighttiger.driverassistant.core.model.SupportReason.DRIVER_REQUESTED)
        h.processor.process(h.cancelled())
        h.connectivity.set(true)
        h.sync.syncDue()
        assertEquals(listOf("SUPPORT_REQUESTED"), h.backend.submitted.map { it.payload.eventType.name })
        assertEquals(OutboxStatus.CANCELLED_CONFLICT, h.outbox.all().first { it.payload.eventType.name == "ETA_REPORTED" }.status)
    }

    @Test
    fun `duplicate ack is treated as delivered, not resent`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        h.coordinator.reportEta(TRIP, 60, false, CaptureMethod.VOICE)
        val id = h.outbox.all().single().eventId
        h.backend.responses.addLast(BackendResult.Success(SubmissionAck(id, AckStatus.DUPLICATE, T0)))
        h.sync.syncDue()
        h.sync.syncDue()
        assertEquals(1, h.backend.submitted.size)
        assertEquals(OutboxStatus.ACKNOWLEDGED, h.outbox.all().single().status)
    }

    @Test
    fun `stale in-flight event from a killed process is resent with the same idempotency key`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        h.coordinator.reportEta(TRIP, 60, false, CaptureMethod.VOICE)
        val e = h.outbox.all().single()
        h.outbox.update(e.copy(status = OutboxStatus.IN_FLIGHT, attempts = 1))
        h.sync.syncDue()
        assertEquals(e.idempotencyKey, h.backend.submitted.single().idempotencyKey)
        assertEquals(OutboxStatus.ACKNOWLEDGED, h.outbox.all().single().status)
    }

    @Test
    fun `unauthorized stops the batch and keeps events`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        h.coordinator.reportEta(TRIP, 60, false, CaptureMethod.VOICE)
        h.backend.responses.addLast(BackendResult.Failure(BackendError.Unauthorized))
        val report = h.sync.syncDue()
        assertTrue(report.authRequired)
        assertEquals(OutboxStatus.PENDING, h.outbox.all().single().status)
    }

    @Test
    fun `backoff is bounded`() {
        val p = BackoffPolicy(jitter = 0.0)
        assertEquals(Duration.ofSeconds(5), p.delayFor(1))
        assertEquals(Duration.ofSeconds(40), p.delayFor(4))
        assertEquals(Duration.ofMinutes(15), p.delayFor(30))
    }
}
