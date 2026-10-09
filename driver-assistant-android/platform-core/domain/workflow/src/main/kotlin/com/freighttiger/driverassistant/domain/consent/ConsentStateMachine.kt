package com.freighttiger.driverassistant.domain.consent

import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.ConsentDecision
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
import java.time.Instant

sealed interface ConsentEvent {
    data object PromptDelivered : ConsentEvent

    data class DriverDecided(
        val decision: ConsentDecision,
        val method: CaptureMethod,
        val capturedAt: Instant,
        val transcript: String?,
    ) : ConsentEvent

    data object Submitted : ConsentEvent

    /** Server stored the response. [validation] is present when the ack already carries a verdict. */
    data class BackendAccepted(val validation: ConsentValidationStatus?) : ConsentEvent

    /** Authoritative verdict from the backend (ack, push event or trip-state poll). */
    data class BackendValidation(val status: ConsentValidationStatus, val reasonCode: String? = null) : ConsentEvent

    /** Server refused the submission (validation error). */
    data class BackendRejected(val reasonCode: String) : ConsentEvent

    /** Request validity window passed before the driver answered. */
    data class Expired(val reasonCode: String = "REQUEST_EXPIRED") : ConsentEvent

    data object Withdraw : ConsentEvent

    /** Trip was cancelled or completed. */
    data class TripEnded(val reasonCode: String) : ConsentEvent
}

sealed interface ConsentTransition {
    data class Applied(val record: ConsentRecord, val changed: Boolean) : ConsentTransition
    data class Rejected(val reasonCode: String) : ConsentTransition
}

/**
 * Explicit consent state machine. Pure function of (record, event, now).
 *
 * Invariants enforced here:
 *  - Only an explicit [ConsentEvent.DriverDecided] can lead towards GRANTED; silence/unknown
 *    never produce that event (see DialogueEngine).
 *  - A driver "yes" only reaches GRANTED_PENDING_VALIDATION. GRANTED requires a backend VALIDATED verdict.
 *  - A DECLINED/WITHDRAWN/EXPIRED/VALIDATION_FAILED record can never become GRANTED.
 *  - Answers after expiry are refused; a new request (new id) is needed.
 */
object ConsentStateMachine {

    fun apply(record: ConsentRecord, event: ConsentEvent, now: Instant): ConsentTransition {
        val s = record.state
        return when (event) {
            ConsentEvent.PromptDelivered -> when (s) {
                REQUESTED -> {
                    if (record.isExpiredAt(now)) expire(record, "REQUEST_EXPIRED", now)
                    else applied(record.copy(state = AWAITING_RESPONSE, updatedAt = now))
                }
                AWAITING_RESPONSE -> unchanged(record)
                else -> rejected("INVALID_STATE_$s")
            }

            is ConsentEvent.DriverDecided -> when {
                !s.acceptsResponse -> rejected("INVALID_STATE_$s")
                record.isExpiredAt(event.capturedAt) || record.isExpiredAt(now) -> rejected("REQUEST_EXPIRED")
                else -> applied(
                    record.copy(
                        state = if (event.decision == ConsentDecision.GRANTED) GRANTED_PENDING_VALIDATION else DECLINED,
                        decision = event.decision,
                        captureMethod = event.method,
                        capturedAt = event.capturedAt,
                        transcript = event.transcript,
                        submissionState = SubmissionState.CAPTURED_LOCALLY,
                        updatedAt = now,
                    ),
                )
            }

            ConsentEvent.Submitted -> when (record.submissionState) {
                SubmissionState.CAPTURED_LOCALLY, SubmissionState.SUBMITTED ->
                    applied(record.copy(submissionState = SubmissionState.SUBMITTED, updatedAt = now))
                else -> unchanged(record)
            }

            is ConsentEvent.BackendAccepted -> {
                val accepted = record.copy(submissionState = SubmissionState.ACCEPTED, updatedAt = now)
                val validation = event.validation
                if (validation == null || validation == ConsentValidationStatus.PENDING || s != GRANTED_PENDING_VALIDATION) {
                    applied(accepted)
                } else {
                    apply(accepted, ConsentEvent.BackendValidation(validation), now)
                }
            }

            is ConsentEvent.BackendValidation -> when (s) {
                GRANTED_PENDING_VALIDATION, GRANTED -> when (event.status) {
                    ConsentValidationStatus.PENDING -> unchanged(record)
                    ConsentValidationStatus.VALIDATED ->
                        if (s == GRANTED) unchanged(record)
                        else applied(record.copy(state = GRANTED, reasonCode = null, updatedAt = now))
                    ConsentValidationStatus.REJECTED ->
                        applied(record.copy(state = VALIDATION_FAILED, reasonCode = event.reasonCode ?: "VALIDATION_REJECTED", updatedAt = now))
                    ConsentValidationStatus.EXPIRED_NEEDS_REVIEW ->
                        applied(record.copy(state = EXPIRED, reasonCode = event.reasonCode ?: "EXPIRED_NEEDS_REVIEW", updatedAt = now))
                }
                else -> rejected("INVALID_STATE_$s")
            }

            is ConsentEvent.BackendRejected -> when (s) {
                GRANTED_PENDING_VALIDATION -> applied(
                    record.copy(
                        state = VALIDATION_FAILED,
                        submissionState = SubmissionState.REJECTED,
                        reasonCode = event.reasonCode,
                        updatedAt = now,
                    ),
                )
                else -> applied(record.copy(submissionState = SubmissionState.REJECTED, reasonCode = event.reasonCode, updatedAt = now))
            }

            is ConsentEvent.Expired -> when (s) {
                REQUESTED, AWAITING_RESPONSE, ConsentState.NOT_REQUESTED -> expire(record, event.reasonCode, now)
                // An answer captured in time is judged by the backend, never expired locally.
                else -> unchanged(record)
            }

            ConsentEvent.Withdraw -> when (s) {
                GRANTED, GRANTED_PENDING_VALIDATION -> applied(record.copy(state = WITHDRAWN, updatedAt = now))
                else -> rejected("INVALID_STATE_$s")
            }

            is ConsentEvent.TripEnded -> when (s) {
                REQUESTED, AWAITING_RESPONSE, GRANTED_PENDING_VALIDATION, ConsentState.NOT_REQUESTED ->
                    expire(record, event.reasonCode, now)
                else -> unchanged(record)
            }
        }
    }

    private fun expire(record: ConsentRecord, reason: String, now: Instant) =
        applied(record.copy(state = EXPIRED, reasonCode = reason, updatedAt = now))

    private fun applied(record: ConsentRecord) = ConsentTransition.Applied(record, changed = true)
    private fun unchanged(record: ConsentRecord) = ConsentTransition.Applied(record, changed = false)
    private fun rejected(reason: String) = ConsentTransition.Rejected(reason)

    /** Defensive helper for UI: only a backend-validated record counts as granted. */
    fun isTrackingConsentConfirmed(record: ConsentRecord?): Boolean = record?.state == GRANTED

    @Suppress("unused")
    private val terminal = setOf(DECLINED, EXPIRED, WITHDRAWN, VALIDATION_FAILED)
}
