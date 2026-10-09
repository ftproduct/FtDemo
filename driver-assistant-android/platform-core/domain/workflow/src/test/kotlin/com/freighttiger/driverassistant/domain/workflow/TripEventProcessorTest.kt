package com.freighttiger.driverassistant.domain.workflow

import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.core.model.ConsentValidationStatus
import com.freighttiger.driverassistant.core.model.InboundPayload
import com.freighttiger.driverassistant.core.model.Milestone
import com.freighttiger.driverassistant.core.model.OutboxStatus
import com.freighttiger.driverassistant.core.model.PromptStatus
import com.freighttiger.driverassistant.core.model.PromptType
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.core.model.TripEventType
import com.freighttiger.driverassistant.core.model.TripStatus
import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.ConsentDecision
import com.freighttiger.driverassistant.domain.support.DRIVER
import com.freighttiger.driverassistant.domain.support.TRIP
import com.freighttiger.driverassistant.domain.support.TestHarness
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class TripEventProcessorTest {

    @Test
    fun `trip assignment is persisted and schedules a briefing`() = runTest {
        val h = TestHarness()
        val r = h.processor.process(h.assigned())
        assertTrue(r is ProcessingResult.Applied)
        assertEquals(PromptType.TRIP_BRIEFING, (r as ProcessingResult.Applied).prompt?.type)
        assertEquals(TRIP, h.trips.activeTrip()?.tripId)
        assertTrue(h.inbound.exists(r.eventId))
    }

    @Test
    fun `duplicate event is suppressed and triggers nothing twice`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        val consent = h.consentRequested()
        assertTrue(h.processor.process(consent) is ProcessingResult.Applied)
        val promptsBefore = h.prompts.all().size
        assertEquals(ProcessingResult.Duplicate(consent.eventId), h.processor.process(consent))
        assertEquals(promptsBefore, h.prompts.all().size)
        assertEquals(1, h.consents.forTrip(TRIP).size)
    }

    @Test
    fun `event for another driver is rejected`() = runTest {
        val h = TestHarness()
        val r = h.processor.process(h.assigned(driver = "someone-else"))
        assertEquals("DRIVER_MISMATCH", (r as ProcessingResult.Rejected).reasonCode)
        assertNull(h.trips.get(TRIP))
    }

    @Test
    fun `consent request for an unknown trip is rejected`() = runTest {
        val h = TestHarness()
        val r = h.processor.process(h.consentRequested(tripId = "unknown-trip"))
        assertEquals("UNKNOWN_TRIP", (r as ProcessingResult.Rejected).reasonCode)
    }

    @Test
    fun `invalid payload and unsupported schema are rejected`() = runTest {
        val h = TestHarness()
        val mismatched = h.event(TripEventType.CONSENT_REQUESTED, InboundPayload.TripCompleted)
        assertEquals("PAYLOAD_TYPE_MISMATCH", (h.processor.process(mismatched) as ProcessingResult.Rejected).reasonCode)
        val v2 = h.assigned().copy(eventId = "evt-v2", schemaVersion = "2.0")
        assertEquals("UNSUPPORTED_SCHEMA_VERSION", (h.processor.process(v2) as ProcessingResult.Rejected).reasonCode)
    }

    @Test
    fun `expired event is ignored`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        val e = h.milestone(Milestone.ARRIVAL_AT_LOADING_POINT).copy(expiresAt = h.clock.now().minusSeconds(1))
        assertEquals("EVENT_EXPIRED", (h.processor.process(e) as ProcessingResult.Ignored).reasonCode)
    }

    @Test
    fun `already expired consent request is not prompted`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        val r = h.processor.process(h.consentRequested(validFor = Duration.ZERO))
        assertEquals("CONSENT_REQUEST_EXPIRED", (r as ProcessingResult.Ignored).reasonCode)
    }

    @Test
    fun `newer consent request supersedes an unanswered older one`() = runTest {
        val h = TestHarness()
        h.withConsentRequest("c-1")
        h.processor.process(h.consentRequested("c-2"))
        assertEquals(ConsentState.EXPIRED, h.consents.get("c-1")?.state)
        assertEquals("SUPERSEDED", h.consents.get("c-1")?.reasonCode)
        assertEquals(ConsentState.REQUESTED, h.consents.get("c-2")?.state)
    }

    @Test
    fun `tracking activation is refused without a validated consent`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        val r = h.processor.process(h.tracking(TrackingStatus.ACTIVE, null, null))
        assertEquals("CONSENT_NOT_VALIDATED", (r as ProcessingResult.Rejected).reasonCode)
        assertEquals(TrackingStatus.NOT_STARTED, h.trips.get(TRIP)?.trackingStatus)
    }

    @Test
    fun `nahi must never start tracking - even if backend later claims validation`() = runTest {
        val h = TestHarness()
        h.withConsentRequest()
        h.coordinator.submitConsentDecision("consent-789", ConsentDecision.DECLINED, CaptureMethod.VOICE, "nahi")
        val r = h.processor.process(h.tracking(TrackingStatus.ACTIVE, "consent-789", ConsentValidationStatus.VALIDATED))
        assertTrue(r is ProcessingResult.Rejected)
        assertEquals(ConsentState.DECLINED, h.consents.get("consent-789")?.state)
        assertEquals(TrackingStatus.NOT_STARTED, h.trips.get(TRIP)?.trackingStatus)
    }

    @Test
    fun `backend validation plus activation makes tracking active and informs driver`() = runTest {
        val h = TestHarness()
        h.withConsentRequest()
        h.coordinator.submitConsentDecision("consent-789", ConsentDecision.GRANTED, CaptureMethod.VOICE, "haan")
        val r = h.processor.process(h.tracking(TrackingStatus.ACTIVE, "consent-789", ConsentValidationStatus.VALIDATED)) as ProcessingResult.Applied
        assertEquals(ConsentState.GRANTED, h.consents.get("consent-789")?.state)
        assertEquals(TrackingStatus.ACTIVE, h.trips.get(TRIP)?.trackingStatus)
        assertEquals(PromptType.TRACKING_RESULT, r.prompt?.type)
        assertEquals("ACTIVE", r.prompt?.detail)
    }

    @Test
    fun `cancelled trip does not accept a tracking activation`() = runTest {
        val h = TestHarness()
        h.withConsentRequest()
        h.coordinator.submitConsentDecision("consent-789", ConsentDecision.GRANTED, CaptureMethod.VOICE, "haan")
        h.processor.process(h.cancelled())
        val r = h.processor.process(h.tracking(TrackingStatus.ACTIVE, "consent-789", ConsentValidationStatus.VALIDATED))
        assertEquals("TRIP_NOT_ACTIVE", (r as ProcessingResult.Rejected).reasonCode)
        assertTrue(h.trips.get(TRIP)?.trackingStatus != TrackingStatus.ACTIVE)
    }

    @Test
    fun `trip cancellation expires consent, cancels prompts and queued updates`() = runTest {
        val h = TestHarness()
        h.withConsentRequest()
        h.coordinator.reportEta(TRIP, 60, false, CaptureMethod.VOICE) // queued, not yet synced
        val r = h.processor.process(h.cancelled()) as ProcessingResult.Applied
        assertEquals(TripStatus.CANCELLED, h.trips.get(TRIP)?.status)
        assertEquals(ConsentState.EXPIRED, h.consents.get("consent-789")?.state)
        assertTrue(h.prompts.all().filter { it.type != PromptType.TRIP_CANCELLED }.none { it.status.isOpen })
        assertEquals(PromptType.TRIP_CANCELLED, r.prompt?.type)
        assertTrue(h.outbox.all().all { it.status == OutboxStatus.CANCELLED_CONFLICT })
    }

    @Test
    fun `cancelled trip cannot receive new consent requests or milestones`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        h.processor.process(h.cancelled())
        assertEquals("TRIP_NOT_ACTIVE", (h.processor.process(h.consentRequested()) as ProcessingResult.Rejected).reasonCode)
        assertEquals("TRIP_NOT_ACTIVE", (h.processor.process(h.milestone(Milestone.ETA_TO_LOADING_POINT)) as ProcessingResult.Rejected).reasonCode)
        assertEquals("TRIP_ALREADY_ENDED", (h.processor.process(h.assigned()) as ProcessingResult.Rejected).reasonCode)
    }

    @Test
    fun `arrival milestone is skipped after driver reported arrival`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        h.coordinator.confirmArrival(TRIP, true, CaptureMethod.TAP)
        val r = h.processor.process(h.milestone(Milestone.ARRIVAL_AT_LOADING_POINT))
        assertEquals("ARRIVAL_ALREADY_REPORTED", (r as ProcessingResult.Ignored).reasonCode)
    }

    @Test
    fun `repeated milestone request while prompt open is suppressed`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        h.processor.process(h.milestone(Milestone.ETA_TO_LOADING_POINT))
        val second = h.processor.process(h.milestone(Milestone.ETA_TO_LOADING_POINT)) as ProcessingResult.Applied
        assertNull(second.prompt)
        assertEquals("DUPLICATE_PROMPT", second.promptSuppressedReason)
        assertEquals(1, h.prompts.all().count { it.type == PromptType.ETA && it.status == PromptStatus.PENDING })
    }

    @Test
    fun `no session means events are rejected`() = runTest {
        val h = TestHarness()
        h.sessions.clear()
        assertEquals("NO_ACTIVE_SESSION", (h.processor.process(h.assigned()) as ProcessingResult.Rejected).reasonCode)
        assertEquals(DRIVER, "driver-123")
    }
}
