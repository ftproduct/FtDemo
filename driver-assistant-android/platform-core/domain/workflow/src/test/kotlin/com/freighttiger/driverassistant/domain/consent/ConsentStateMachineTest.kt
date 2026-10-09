package com.freighttiger.driverassistant.domain.consent

import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.ConsentDecision
import com.freighttiger.driverassistant.core.model.ConsentPurpose
import com.freighttiger.driverassistant.core.model.ConsentRecord
import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.core.model.ConsentState.AWAITING_RESPONSE
import com.freighttiger.driverassistant.core.model.ConsentState.DECLINED
import com.freighttiger.driverassistant.core.model.ConsentState.EXPIRED
import com.freighttiger.driverassistant.core.model.ConsentState.GRANTED
import com.freighttiger.driverassistant.core.model.ConsentState.GRANTED_PENDING_VALIDATION
import com.freighttiger.driverassistant.core.model.ConsentState.REQUESTED
import com.freighttiger.driverassistant.core.model.ConsentState.VALIDATION_FAILED
import com.freighttiger.driverassistant.core.model.ConsentState.WITHDRAWN
import com.freighttiger.driverassistant.core.model.ConsentValidationStatus
import com.freighttiger.driverassistant.core.model.SubmissionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

class ConsentStateMachineTest {
    private val t0 = Instant.parse("2026-10-09T12:00:00Z")
    private val record = ConsentRecord(
        consentRequestId = "consent-789", tripId = "trip-456", driverId = "driver-123",
        purpose = ConsentPurpose.SIM_BASED_TRIP_TRACKING, requestedAt = t0, expiresAt = t0.plus(Duration.ofMinutes(15)),
        state = REQUESTED, updatedAt = t0,
    )

    private fun apply(r: ConsentRecord, e: ConsentEvent, now: Instant = t0.plusSeconds(60)) = ConsentStateMachine.apply(r, e, now)
    private fun ConsentTransition.record() = (this as ConsentTransition.Applied).record
    private fun stateOf(s: ConsentState) = record.copy(state = s)
    private fun decide(d: ConsentDecision, at: Instant = t0.plusSeconds(60)) = ConsentEvent.DriverDecided(d, CaptureMethod.VOICE, at, "haan")

    @Test
    fun `prompt delivery moves REQUESTED to AWAITING_RESPONSE`() {
        assertEquals(AWAITING_RESPONSE, apply(record, ConsentEvent.PromptDelivered).record().state)
    }

    @Test
    fun `yes only reaches GRANTED_PENDING_VALIDATION, never GRANTED`() {
        val r = apply(stateOf(AWAITING_RESPONSE), decide(ConsentDecision.GRANTED)).record()
        assertEquals(GRANTED_PENDING_VALIDATION, r.state)
        assertEquals(SubmissionState.CAPTURED_LOCALLY, r.submissionState)
        assertEquals(t0.plusSeconds(60), r.capturedAt)
    }

    @Test
    fun `no is DECLINED`() = assertEquals(DECLINED, apply(stateOf(AWAITING_RESPONSE), decide(ConsentDecision.DECLINED)).record().state)

    @Test
    fun `backend VALIDATED is the only way to GRANTED`() {
        val pending = stateOf(GRANTED_PENDING_VALIDATION)
        assertEquals(GRANTED, apply(pending, ConsentEvent.BackendValidation(ConsentValidationStatus.VALIDATED)).record().state)
        assertEquals(GRANTED_PENDING_VALIDATION, apply(pending, ConsentEvent.BackendValidation(ConsentValidationStatus.PENDING)).record().state)
        assertEquals(GRANTED_PENDING_VALIDATION, apply(pending, ConsentEvent.BackendAccepted(ConsentValidationStatus.PENDING)).record().state)
    }

    @Test
    fun `backend validation failure`() {
        val r = apply(stateOf(GRANTED_PENDING_VALIDATION), ConsentEvent.BackendValidation(ConsentValidationStatus.REJECTED, "OPERATOR_DENIED")).record()
        assertEquals(VALIDATION_FAILED, r.state)
        assertEquals("OPERATOR_DENIED", r.reasonCode)
        val rejected = apply(stateOf(GRANTED_PENDING_VALIDATION), ConsentEvent.BackendRejected("INVALID_ASSOCIATION")).record()
        assertEquals(VALIDATION_FAILED, rejected.state)
        assertEquals(SubmissionState.REJECTED, rejected.submissionState)
    }

    @Test
    fun `a declined, withdrawn, expired or failed record can never become GRANTED`() {
        for (s in listOf(DECLINED, WITHDRAWN, EXPIRED, VALIDATION_FAILED)) {
            val t = apply(stateOf(s), ConsentEvent.BackendValidation(ConsentValidationStatus.VALIDATED))
            assertTrue("$s accepted validation", t is ConsentTransition.Rejected)
            assertTrue(apply(stateOf(s), decide(ConsentDecision.GRANTED)) is ConsentTransition.Rejected)
        }
    }

    @Test
    fun `answer after expiry is refused`() {
        val late = t0.plus(Duration.ofMinutes(16))
        val t = apply(stateOf(AWAITING_RESPONSE), decide(ConsentDecision.GRANTED, late), late)
        assertEquals(ConsentTransition.Rejected("REQUEST_EXPIRED"), t)
    }

    @Test
    fun `unanswered request expires`() {
        assertEquals(EXPIRED, apply(stateOf(AWAITING_RESPONSE), ConsentEvent.Expired()).record().state)
    }

    @Test
    fun `an answer captured in time is not expired locally - backend decides`() {
        val pending = stateOf(GRANTED_PENDING_VALIDATION)
        val t = apply(pending, ConsentEvent.Expired(), t0.plus(Duration.ofHours(1))) as ConsentTransition.Applied
        assertEquals(GRANTED_PENDING_VALIDATION, t.record.state)
        val review = apply(pending, ConsentEvent.BackendValidation(ConsentValidationStatus.EXPIRED_NEEDS_REVIEW)).record()
        assertEquals(EXPIRED, review.state)
    }

    @Test
    fun `withdrawal from GRANTED or pending`() {
        assertEquals(WITHDRAWN, apply(stateOf(GRANTED), ConsentEvent.Withdraw).record().state)
        assertEquals(WITHDRAWN, apply(stateOf(GRANTED_PENDING_VALIDATION), ConsentEvent.Withdraw).record().state)
        assertTrue(apply(stateOf(DECLINED), ConsentEvent.Withdraw) is ConsentTransition.Rejected)
    }

    @Test
    fun `trip end expires open and pending requests`() {
        for (s in listOf(REQUESTED, AWAITING_RESPONSE, GRANTED_PENDING_VALIDATION)) {
            assertEquals(EXPIRED, apply(stateOf(s), ConsentEvent.TripEnded("TRIP_CANCELLED")).record().state)
        }
    }

    @Test
    fun `double answer is refused`() {
        val first = apply(stateOf(AWAITING_RESPONSE), decide(ConsentDecision.GRANTED)).record()
        assertTrue(apply(first, decide(ConsentDecision.GRANTED)) is ConsentTransition.Rejected)
        assertTrue(apply(first, decide(ConsentDecision.DECLINED)) is ConsentTransition.Rejected)
    }
}
