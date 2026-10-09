package com.freighttiger.driverassistant.domain.backend

import com.freighttiger.driverassistant.core.model.ConsentValidationStatus
import com.freighttiger.driverassistant.core.model.DriverProfile
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.core.model.TripStatus
import java.time.Instant

sealed interface BackendResult<out T> {
    data class Success<T>(val value: T) : BackendResult<T>
    data class Failure(val error: BackendError) : BackendResult<Nothing>
}

sealed interface BackendError {
    /** Whether the same request may succeed if retried later. */
    val retryable: Boolean

    data class Network(val message: String?) : BackendError { override val retryable = true }
    data class Server(val httpCode: Int) : BackendError { override val retryable = true }
    data class RateLimited(val retryAfterSeconds: Long?) : BackendError { override val retryable = true }
    data class Validation(val code: String, val message: String? = null) : BackendError { override val retryable = false }
    data object Unauthorized : BackendError { override val retryable = false }
    data class NotFound(val code: String = "NOT_FOUND") : BackendError { override val retryable = false }
    /** Backend not configured for this build (no base URL). Never silently replaced by mock data. */
    data object NotConfigured : BackendError { override val retryable = false }
    data class MalformedResponse(val message: String?) : BackendError { override val retryable = true }
}

val BackendError.code: String
    get() = when (this) {
        is BackendError.Network -> "NETWORK"
        is BackendError.Server -> "HTTP_$httpCode"
        is BackendError.RateLimited -> "RATE_LIMITED"
        is BackendError.Validation -> code
        BackendError.Unauthorized -> "UNAUTHORIZED"
        is BackendError.NotFound -> code
        BackendError.NotConfigured -> "NOT_CONFIGURED"
        is BackendError.MalformedResponse -> "MALFORMED_RESPONSE"
    }

enum class AckStatus { ACCEPTED, DUPLICATE }

/** Server acknowledgement of an outbound event. */
data class SubmissionAck(
    val eventId: String,
    val status: AckStatus,
    val receivedAt: Instant,
    val serverReference: String? = null,
    /** Present for consent responses. PENDING means checks are still running on the backend. */
    val consentValidation: ConsentValidationStatus? = null,
    /** Present for support requests. True only if the backend confirms a callback is scheduled. */
    val callbackScheduled: Boolean? = null,
)

data class ConsentSnapshot(
    val consentRequestId: String,
    val validation: ConsentValidationStatus,
)

/** Authoritative trip state from `GET /trips/{tripId}/state`. */
data class TripStateSnapshot(
    val tripId: String,
    val status: TripStatus,
    val trackingStatus: TrackingStatus,
    val loadingStatus: LoadingStatus,
    val consent: ConsentSnapshot?,
    val asOf: Instant,
)

/**
 * Port to the Freight Tiger assistant backend. Implemented by the real HTTP client and by the
 * clearly-labelled mock backend used in demo mode.
 */
interface AssistantBackend {
    /** True for the in-process mock. UI must label everything coming from it as simulated. */
    val isSimulated: Boolean

    suspend fun submit(event: OutboundEvent): BackendResult<SubmissionAck>
    suspend fun fetchActiveTrip(driverId: String): BackendResult<Trip?>
    suspend fun fetchTripState(tripId: String): BackendResult<TripStateSnapshot>
}

data class OtpChallenge(val challengeId: String, val expiresAt: Instant, val simulated: Boolean, val demoHint: String? = null)

data class AuthResult(val profile: DriverProfile, val accessToken: String, val expiresAt: Instant, val simulated: Boolean)

/** Mobile-number verification. The MVP ships only a clearly labelled demo implementation. */
interface AuthGateway {
    val isSimulated: Boolean
    suspend fun requestOtp(phoneNumber: String): BackendResult<OtpChallenge>
    suspend fun verifyOtp(challengeId: String, phoneNumber: String, otp: String): BackendResult<AuthResult>
}
