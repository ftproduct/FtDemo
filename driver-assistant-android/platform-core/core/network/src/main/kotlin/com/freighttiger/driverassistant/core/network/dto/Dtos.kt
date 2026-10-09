package com.freighttiger.driverassistant.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

// Proposed Freight Tiger assistant API contracts (v1). These do NOT exist in Freight Tiger yet.
// All timestamps are ISO 8601 UTC strings. All POSTs carry an `Idempotency-Key` header equal to
// `idempotency_key` in the body.

// ---------- POST /api/v1/assistant/consent-responses ----------

@Serializable
data class ConsentEvidenceDto(
    @SerialName("transcript") val transcript: String?,
    @SerialName("recording_reference") val recordingReference: String? = null,
)

@Serializable
data class ConsentResponseRequest(
    @SerialName("event_type") val eventType: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("schema_version") val schemaVersion: String,
    @SerialName("driver_id") val driverId: String,
    @SerialName("trip_id") val tripId: String,
    @SerialName("consent_request_id") val consentRequestId: String,
    @SerialName("purpose") val purpose: String,
    @SerialName("decision") val decision: String,
    @SerialName("capture_method") val captureMethod: String,
    @SerialName("language") val language: String,
    @SerialName("captured_at") val capturedAt: String,
    @SerialName("evidence") val evidence: ConsentEvidenceDto,
    @SerialName("idempotency_key") val idempotencyKey: String,
)

// ---------- POST /api/v1/assistant/trip-updates ----------

@Serializable
data class EtaDto(
    @SerialName("eta_minutes") val etaMinutes: Int,
    @SerialName("approximate") val approximate: Boolean,
    @SerialName("estimated_arrival_at") val estimatedArrivalAt: String,
    /** Always DRIVER_REPORTED from this app. Never a GPS prediction. */
    @SerialName("source") val source: String = "DRIVER_REPORTED",
)

@Serializable
data class ArrivalDto(
    @SerialName("arrived") val arrived: Boolean,
    @SerialName("source") val source: String = "DRIVER_REPORTED",
)

@Serializable
data class TripUpdateRequest(
    @SerialName("event_type") val eventType: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("schema_version") val schemaVersion: String,
    @SerialName("driver_id") val driverId: String,
    @SerialName("trip_id") val tripId: String,
    @SerialName("capture_method") val captureMethod: String,
    @SerialName("captured_at") val capturedAt: String,
    @SerialName("eta") val eta: EtaDto? = null,
    @SerialName("arrival") val arrival: ArrivalDto? = null,
    @SerialName("loading_status") val loadingStatus: String? = null,
    @SerialName("idempotency_key") val idempotencyKey: String,
)

// ---------- POST /api/v1/assistant/support-requests ----------

@Serializable
data class SupportRequestRequest(
    @SerialName("event_id") val eventId: String,
    @SerialName("schema_version") val schemaVersion: String,
    @SerialName("driver_id") val driverId: String,
    @SerialName("trip_id") val tripId: String?,
    @SerialName("reason") val reason: String,
    @SerialName("captured_at") val capturedAt: String,
    @SerialName("idempotency_key") val idempotencyKey: String,
)

// ---------- POST /api/v1/assistant/events ----------

@Serializable
data class AssistantEventRequest(
    @SerialName("event_type") val eventType: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("schema_version") val schemaVersion: String,
    @SerialName("driver_id") val driverId: String,
    @SerialName("trip_id") val tripId: String?,
    @SerialName("interaction") val interaction: String,
    @SerialName("prompt_type") val promptType: String,
    @SerialName("captured_at") val capturedAt: String,
    @SerialName("idempotency_key") val idempotencyKey: String,
)

// ---------- Common acknowledgement / error ----------

@Serializable
data class AckResponse(
    @SerialName("event_id") val eventId: String,
    /** ACCEPTED or DUPLICATE (same idempotency key and payload seen before). */
    @SerialName("status") val status: String,
    @SerialName("received_at") val receivedAt: String,
    @SerialName("server_reference") val serverReference: String? = null,
    /** Consent only: PENDING | VALIDATED | REJECTED | EXPIRED_NEEDS_REVIEW. */
    @SerialName("consent_validation_status") val consentValidationStatus: String? = null,
    /** Support only: true only when a callback has actually been scheduled. */
    @SerialName("callback_scheduled") val callbackScheduled: Boolean? = null,
)

@Serializable
data class FieldErrorDto(
    @SerialName("field") val field: String,
    @SerialName("code") val code: String,
)

@Serializable
data class ApiErrorResponse(
    @SerialName("error_code") val errorCode: String,
    @SerialName("message") val message: String? = null,
    @SerialName("field_errors") val fieldErrors: List<FieldErrorDto> = emptyList(),
)

// ---------- GET /api/v1/assistant/trips/active and trip payloads ----------

@Serializable
data class PlaceDto(
    @SerialName("name") val name: String,
    @SerialName("city") val city: String,
)

@Serializable
data class LoadingPointDto(
    @SerialName("name") val name: String,
    @SerialName("address") val address: String,
    @SerialName("city") val city: String,
    @SerialName("reporting_time") val reportingTime: String? = null,
)

@Serializable
data class TripDto(
    @SerialName("trip_id") val tripId: String,
    @SerialName("driver_id") val driverId: String,
    @SerialName("vehicle_registration") val vehicleRegistration: String,
    @SerialName("origin") val origin: PlaceDto,
    @SerialName("destination") val destination: PlaceDto,
    @SerialName("loading_point") val loadingPoint: LoadingPointDto,
    @SerialName("status") val status: String,
    @SerialName("tracking_status") val trackingStatus: String = "NOT_STARTED",
    @SerialName("loading_status") val loadingStatus: String = "NOT_REACHED",
    @SerialName("assigned_at") val assignedAt: String,
    @SerialName("geofence_arrival_verified_at") val geofenceArrivalVerifiedAt: String? = null,
)

@Serializable
data class ActiveTripResponse(
    @SerialName("trip") val trip: TripDto? = null,
)

// ---------- GET /api/v1/assistant/trips/{tripId}/state ----------

@Serializable
data class ConsentStateDto(
    @SerialName("consent_request_id") val consentRequestId: String,
    @SerialName("validation_status") val validationStatus: String,
)

@Serializable
data class TripStateResponse(
    @SerialName("trip_id") val tripId: String,
    @SerialName("status") val status: String,
    @SerialName("tracking_status") val trackingStatus: String,
    @SerialName("loading_status") val loadingStatus: String,
    @SerialName("consent") val consent: ConsentStateDto? = null,
    @SerialName("as_of") val asOf: String,
)

// ---------- Proposed auth (mobile OTP) ----------

@Serializable
data class OtpRequest(@SerialName("phone_number") val phoneNumber: String)

@Serializable
data class OtpChallengeResponse(
    @SerialName("challenge_id") val challengeId: String,
    @SerialName("expires_at") val expiresAt: String,
)

@Serializable
data class OtpVerifyRequest(
    @SerialName("challenge_id") val challengeId: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("otp") val otp: String,
)

@Serializable
data class DriverProfileDto(
    @SerialName("driver_id") val driverId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("masked_phone") val maskedPhone: String,
    @SerialName("preferred_language") val preferredLanguage: String = "hi-IN",
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("driver") val driver: DriverProfileDto,
)

// ---------- Push (FCM data message) envelope ----------

@Serializable
data class InboundEventEnvelopeDto(
    @SerialName("event_id") val eventId: String,
    @SerialName("schema_version") val schemaVersion: String,
    @SerialName("event_type") val eventType: String,
    @SerialName("driver_id") val driverId: String,
    @SerialName("trip_id") val tripId: String,
    @SerialName("issued_at") val issuedAt: String,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("payload") val payload: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class TripAssignedPayloadDto(@SerialName("trip") val trip: TripDto)

@Serializable
data class ConsentRequestedPayloadDto(
    @SerialName("consent_request_id") val consentRequestId: String,
    @SerialName("purpose") val purpose: String,
    @SerialName("expires_at") val expiresAt: String,
)

@Serializable
data class TrackingStatusPayloadDto(
    @SerialName("tracking_status") val trackingStatus: String,
    @SerialName("consent_request_id") val consentRequestId: String? = null,
    @SerialName("consent_validation_status") val consentValidationStatus: String? = null,
    @SerialName("reason_code") val reasonCode: String? = null,
)

@Serializable
data class MilestonePayloadDto(@SerialName("milestone") val milestone: String)

@Serializable
data class TripCancelledPayloadDto(@SerialName("reason_code") val reasonCode: String? = null)
