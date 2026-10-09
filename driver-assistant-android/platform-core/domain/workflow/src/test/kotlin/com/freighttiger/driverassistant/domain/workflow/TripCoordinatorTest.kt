package com.freighttiger.driverassistant.domain.workflow

import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.ConsentDecision
import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.OutboundPayload
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.domain.support.TRIP
import com.freighttiger.driverassistant.domain.support.TestHarness
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class TripCoordinatorTest {

    @Test
    fun `consent yes is queued with original capture time and idempotency key`() = runTest {
        val h = TestHarness()
        h.withConsentRequest()
        val capturedAt = h.clock.now()
        val r = h.coordinator.submitConsentDecision("consent-789", ConsentDecision.GRANTED, CaptureMethod.VOICE, "Haan, main sahmat hoon")
        val event = (r as CoordinatorResult.Queued).event
        assertEquals("consent-789-response-1", event.idempotencyKey)
        val p = event.payload as OutboundPayload.ConsentResponse
        assertEquals(capturedAt, p.capturedAt)
        assertEquals(ConsentState.GRANTED_PENDING_VALIDATION, h.consents.get("consent-789")?.state)
        // Positive speech alone never activates tracking.
        assertEquals(TrackingStatus.NOT_STARTED, h.trips.get(TRIP)?.trackingStatus)
        assertEquals(1, h.syncRequests)
    }

    @Test
    fun `expired consent request is not silently reused`() = runTest {
        val h = TestHarness()
        h.withConsentRequest(validFor = Duration.ofMinutes(5))
        h.clock.advance(Duration.ofMinutes(6))
        val r = h.coordinator.submitConsentDecision("consent-789", ConsentDecision.GRANTED, CaptureMethod.VOICE, "haan")
        assertEquals(CoordinatorResult.Refused("REQUEST_EXPIRED"), r)
        assertEquals(ConsentState.EXPIRED, h.consents.get("consent-789")?.state)
        assertTrue(h.outbox.all().isEmpty())
    }

    @Test
    fun `consent for a trip of another driver is refused`() = runTest {
        val h = TestHarness()
        h.withConsentRequest()
        h.sessions.save(h.sessions.current()!!.let { it.copy(profile = it.profile.copy(driverId = "other")) })
        assertEquals(CoordinatorResult.Refused("DRIVER_MISMATCH"), h.coordinator.submitConsentDecision("consent-789", ConsentDecision.GRANTED, CaptureMethod.TAP, null))
    }

    @Test
    fun `double tap yes produces a single outbound event`() = runTest {
        val h = TestHarness()
        h.withConsentRequest()
        h.coordinator.submitConsentDecision("consent-789", ConsentDecision.GRANTED, CaptureMethod.TAP, null)
        val second = h.coordinator.submitConsentDecision("consent-789", ConsentDecision.GRANTED, CaptureMethod.TAP, null)
        assertTrue(second is CoordinatorResult.Refused)
        assertEquals(1, h.outbox.all().size)
    }

    @Test
    fun `withdrawal requests stop and queues withdrawal`() = runTest {
        val h = TestHarness()
        h.withConsentRequest()
        h.coordinator.submitConsentDecision("consent-789", ConsentDecision.GRANTED, CaptureMethod.VOICE, "haan")
        val r = h.coordinator.withdrawConsent(TRIP)
        assertTrue(r is CoordinatorResult.Queued)
        assertEquals(ConsentState.WITHDRAWN, h.consents.get("consent-789")?.state)
        assertEquals("consent-789-withdrawal-1", (r as CoordinatorResult.Queued).event.idempotencyKey)
    }

    @Test
    fun `eta is stored as driver reported`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        h.coordinator.reportEta(TRIP, 90, approximate = false, method = CaptureMethod.VOICE)
        val eta = h.trips.get(TRIP)!!.driverReportedEta!!
        assertEquals(90, eta.minutes)
        assertEquals(h.clock.now().plus(Duration.ofMinutes(90)), eta.estimatedArrivalAt)
        assertEquals(CoordinatorResult.Refused("INVALID_ETA"), h.coordinator.reportEta(TRIP, 0, false, CaptureMethod.TAP))
    }

    @Test
    fun `arrival is kept separate from verified arrival`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        h.coordinator.confirmArrival(TRIP, true, CaptureMethod.VOICE)
        val trip = h.trips.get(TRIP)!!
        assertEquals(true, trip.driverReportedArrival?.arrived)
        assertEquals(null, trip.verifiedArrival)
        assertEquals(LoadingStatus.REACHED_LOADING_POINT, trip.loadingStatus)
        assertEquals(CoordinatorResult.Refused("ALREADY_AT_LOADING_POINT"), h.coordinator.reportEta(TRIP, 30, false, CaptureMethod.TAP))
    }

    @Test
    fun `only valid loading transitions are accepted`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        assertEquals(CoordinatorResult.Refused("INVALID_LOADING_TRANSITION"), h.coordinator.reportLoadingStatus(TRIP, LoadingStatus.LOADING_COMPLETED, CaptureMethod.TAP))
        assertTrue(h.coordinator.reportLoadingStatus(TRIP, LoadingStatus.REACHED_LOADING_POINT, CaptureMethod.TAP) is CoordinatorResult.Queued)
        assertTrue(h.coordinator.reportLoadingStatus(TRIP, LoadingStatus.LOADING_STARTED, CaptureMethod.TAP) is CoordinatorResult.Queued)
        assertTrue(h.coordinator.reportLoadingStatus(TRIP, LoadingStatus.LOADING_COMPLETED, CaptureMethod.TAP) is CoordinatorResult.Queued)
    }

    @Test
    fun `updates for cancelled trips are refused`() = runTest {
        val h = TestHarness()
        h.processor.process(h.assigned())
        h.processor.process(h.cancelled())
        assertEquals(CoordinatorResult.Refused("TRIP_NOT_ACTIVE"), h.coordinator.reportEta(TRIP, 30, false, CaptureMethod.TAP))
        assertEquals(CoordinatorResult.Refused("TRIP_NOT_ACTIVE"), h.coordinator.confirmArrival(TRIP, true, CaptureMethod.TAP))
    }
}
