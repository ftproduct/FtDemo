package com.freighttiger.driverassistant.core.network.remote

import com.freighttiger.driverassistant.core.network.dto.AckResponse
import com.freighttiger.driverassistant.core.network.dto.ActiveTripResponse
import com.freighttiger.driverassistant.core.network.dto.AssistantEventRequest
import com.freighttiger.driverassistant.core.network.dto.AuthResponse
import com.freighttiger.driverassistant.core.network.dto.ConsentResponseRequest
import com.freighttiger.driverassistant.core.network.dto.OtpChallengeResponse
import com.freighttiger.driverassistant.core.network.dto.OtpRequest
import com.freighttiger.driverassistant.core.network.dto.OtpVerifyRequest
import com.freighttiger.driverassistant.core.network.dto.SupportRequestRequest
import com.freighttiger.driverassistant.core.network.dto.TripStateResponse
import com.freighttiger.driverassistant.core.network.dto.TripUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Proposed Freight Tiger assistant API (v1). Authentication: `Authorization: Bearer <token>`
 * added by [AuthHeaderInterceptor]. See docs/API_INTEGRATION.md for the full contract.
 */
interface FreightTigerAssistantApi {

    @POST("api/v1/assistant/events")
    suspend fun postEvent(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: AssistantEventRequest,
    ): Response<AckResponse>

    @POST("api/v1/assistant/consent-responses")
    suspend fun postConsentResponse(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: ConsentResponseRequest,
    ): Response<AckResponse>

    @POST("api/v1/assistant/trip-updates")
    suspend fun postTripUpdate(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: TripUpdateRequest,
    ): Response<AckResponse>

    @GET("api/v1/assistant/trips/active")
    suspend fun getActiveTrip(): Response<ActiveTripResponse>

    @GET("api/v1/assistant/trips/{tripId}/state")
    suspend fun getTripState(@Path("tripId") tripId: String): Response<TripStateResponse>

    @POST("api/v1/assistant/support-requests")
    suspend fun postSupportRequest(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: SupportRequestRequest,
    ): Response<AckResponse>

    @POST("api/v1/assistant/auth/otp/request")
    suspend fun requestOtp(@Body body: OtpRequest): Response<OtpChallengeResponse>

    @POST("api/v1/assistant/auth/otp/verify")
    suspend fun verifyOtp(@Body body: OtpVerifyRequest): Response<AuthResponse>
}
