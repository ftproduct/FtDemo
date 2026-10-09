@file:UseSerializers(InstantIsoSerializer::class)

package com.freighttiger.driverassistant.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

@Serializable
enum class ConsentState {
    NOT_REQUESTED,
    REQUESTED,
    AWAITING_RESPONSE,
    GRANTED_PENDING_VALIDATION,
    GRANTED,
    DECLINED,
    EXPIRED,
    WITHDRAWN,
    VALIDATION_FAILED;

    val isTerminal: Boolean
        get() = this == DECLINED || this == EXPIRED || this == WITHDRAWN || this == VALIDATION_FAILED

    /** States in which the driver can still answer the request. */
    val acceptsResponse: Boolean get() = this == REQUESTED || this == AWAITING_RESPONSE
}

@Serializable
enum class ConsentPurpose { SIM_BASED_TRIP_TRACKING }

@Serializable
enum class ConsentDecision { GRANTED, DECLINED }

@Serializable
enum class CaptureMethod { VOICE, TAP }

/**
 * Where the locally captured response is in its journey to the backend. Distinct from [ConsentState]:
 * a response can be ACCEPTED (stored by the backend) while its validation is still pending.
 */
@Serializable
enum class SubmissionState { NONE, CAPTURED_LOCALLY, SUBMITTED, ACCEPTED, REJECTED }

/** Backend verdict on a consent response. Only the backend can produce [VALIDATED]. */
@Serializable
enum class ConsentValidationStatus { PENDING, VALIDATED, REJECTED, EXPIRED_NEEDS_REVIEW }

@Serializable
data class ConsentRecord(
    val consentRequestId: String,
    val tripId: String,
    val driverId: String,
    val purpose: ConsentPurpose,
    val requestedAt: Instant,
    val expiresAt: Instant,
    val state: ConsentState,
    val decision: ConsentDecision? = null,
    val captureMethod: CaptureMethod? = null,
    /** Original capture time. Preserved through retries and offline periods. */
    val capturedAt: Instant? = null,
    /** Evidence transcript. Stored locally and sent as consent evidence only; never logged. */
    val transcript: String? = null,
    val language: String = "hi-IN",
    val submissionState: SubmissionState = SubmissionState.NONE,
    val reasonCode: String? = null,
    val updatedAt: Instant,
    val simulated: Boolean = false,
) {
    fun isExpiredAt(now: Instant): Boolean = !now.isBefore(expiresAt)
}
