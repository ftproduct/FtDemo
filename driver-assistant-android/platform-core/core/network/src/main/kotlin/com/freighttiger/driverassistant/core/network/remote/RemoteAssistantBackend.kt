package com.freighttiger.driverassistant.core.network.remote

import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.OutboundPayload
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.core.network.ApiConfig
import com.freighttiger.driverassistant.core.network.dto.ContractViolation
import com.freighttiger.driverassistant.core.network.dto.OtpRequest
import com.freighttiger.driverassistant.core.network.dto.OtpVerifyRequest
import com.freighttiger.driverassistant.core.network.dto.OutboundMapper
import com.freighttiger.driverassistant.core.network.dto.instant
import com.freighttiger.driverassistant.core.network.dto.toDomain
import com.freighttiger.driverassistant.domain.backend.AssistantBackend
import com.freighttiger.driverassistant.domain.backend.AuthGateway
import com.freighttiger.driverassistant.domain.backend.AuthResult
import com.freighttiger.driverassistant.domain.backend.BackendError
import com.freighttiger.driverassistant.domain.backend.BackendResult
import com.freighttiger.driverassistant.domain.backend.OtpChallenge
import com.freighttiger.driverassistant.domain.backend.SubmissionAck
import com.freighttiger.driverassistant.domain.backend.TripStateSnapshot
import com.freighttiger.driverassistant.domain.ports.TimeSource

/** HTTP implementation of the backend port against the proposed Freight Tiger contracts. */
class RemoteAssistantBackend(
    private val api: FreightTigerAssistantApi,
    private val config: ApiConfig,
    private val clock: TimeSource = TimeSource.System,
) : AssistantBackend {
    override val isSimulated: Boolean = false
    private val json = NetworkFactory.json

    override suspend fun submit(event: OutboundEvent): BackendResult<SubmissionAck> {
        val key = event.idempotencyKey
        return when (event.payload) {
            is OutboundPayload.ConsentResponse, is OutboundPayload.ConsentWithdrawn ->
                apiCall(json, { api.postConsentResponse(key, OutboundMapper.consentRequest(event, config)) }) { it.toDomain() }
            is OutboundPayload.EtaReported, is OutboundPayload.ArrivalReported, is OutboundPayload.LoadingStatusReported ->
                apiCall(json, { api.postTripUpdate(key, OutboundMapper.tripUpdate(event)) }) { it.toDomain() }
            is OutboundPayload.SupportRequested ->
                apiCall(json, { api.postSupportRequest(key, OutboundMapper.supportRequest(event)) }) { it.toDomain() }
            is OutboundPayload.AssistantInteraction ->
                apiCall(json, { api.postEvent(key, OutboundMapper.assistantEvent(event)) }) { it.toDomain() }
        }
    }

    override suspend fun fetchActiveTrip(driverId: String): BackendResult<Trip?> =
        apiCall(json, { api.getActiveTrip() }) { response ->
            val trip = response.trip?.toDomain(clock.now())
            if (trip != null && trip.driverId != driverId) throw ContractViolation("Active trip belongs to another driver")
            trip
        }

    override suspend fun fetchTripState(tripId: String): BackendResult<TripStateSnapshot> =
        apiCall(json, { api.getTripState(tripId) }) { it.toDomain() }
}

/** HTTP implementation of the proposed OTP endpoints. */
class RemoteAuthGateway(private val api: FreightTigerAssistantApi) : AuthGateway {
    override val isSimulated: Boolean = false
    private val json = NetworkFactory.json

    override suspend fun requestOtp(phoneNumber: String): BackendResult<OtpChallenge> =
        apiCall(json, { api.requestOtp(OtpRequest(phoneNumber)) }) {
            OtpChallenge(it.challengeId, instant(it.expiresAt, "expires_at"), simulated = false)
        }

    override suspend fun verifyOtp(challengeId: String, phoneNumber: String, otp: String): BackendResult<AuthResult> =
        apiCall(json, { api.verifyOtp(OtpVerifyRequest(challengeId, phoneNumber, otp)) }) {
            AuthResult(it.driver.toDomain(), it.accessToken, instant(it.expiresAt, "expires_at"), simulated = false)
        }
}

/**
 * Used when no base URL is configured and demo mode is off. Every call fails with
 * [BackendError.NotConfigured] — there are no fake success responses on production paths.
 */
class UnconfiguredBackend : AssistantBackend, AuthGateway {
    override val isSimulated: Boolean = false
    private val failure = BackendResult.Failure(BackendError.NotConfigured)
    override suspend fun submit(event: OutboundEvent) = failure
    override suspend fun fetchActiveTrip(driverId: String) = failure
    override suspend fun fetchTripState(tripId: String) = failure
    override suspend fun requestOtp(phoneNumber: String) = failure
    override suspend fun verifyOtp(challengeId: String, phoneNumber: String, otp: String) = failure
}
