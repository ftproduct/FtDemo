package com.freighttiger.driverassistant.core.network.dto

import com.freighttiger.driverassistant.core.model.ConsentValidationStatus
import com.freighttiger.driverassistant.core.model.DriverProfile
import com.freighttiger.driverassistant.core.model.LoadingPoint
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.OutboundPayload
import com.freighttiger.driverassistant.core.model.Place
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.core.model.TripStatus
import com.freighttiger.driverassistant.core.model.VerifiedArrival
import com.freighttiger.driverassistant.core.network.ApiConfig
import com.freighttiger.driverassistant.domain.backend.AckStatus
import com.freighttiger.driverassistant.domain.backend.ConsentSnapshot
import com.freighttiger.driverassistant.domain.backend.SubmissionAck
import com.freighttiger.driverassistant.domain.backend.TripStateSnapshot
import java.time.Instant

/** Thrown when a server payload cannot be mapped to the domain. Mapped to MalformedResponse. */
class ContractViolation(message: String) : IllegalArgumentException(message)

internal inline fun <reified E : Enum<E>> enumOf(value: String, field: String): E =
    enumValues<E>().firstOrNull { it.name == value } ?: throw ContractViolation("Unknown $field '$value'")

internal fun instant(value: String, field: String): Instant =
    try {
        Instant.parse(value)
    } catch (e: Exception) {
        throw ContractViolation("Invalid timestamp in $field")
    }

object OutboundMapper {

    fun consentRequest(event: OutboundEvent, config: ApiConfig): ConsentResponseRequest {
        val tripId = requireNotNull(event.tripId) { "Consent events require a trip" }
        return when (val p = event.payload) {
            is OutboundPayload.ConsentResponse -> ConsentResponseRequest(
                eventType = p.eventType.name,
                eventId = event.eventId,
                schemaVersion = ApiConfig.SCHEMA_VERSION,
                driverId = event.driverId,
                tripId = tripId,
                consentRequestId = p.consentRequestId,
                purpose = p.purpose.name,
                decision = p.decision.name,
                captureMethod = p.captureMethod.name,
                language = p.language,
                capturedAt = p.capturedAt.toString(),
                evidence = ConsentEvidenceDto(
                    transcript = if (config.sendTranscriptEvidence) p.transcript else null,
                    recordingReference = null, // Raw audio is never uploaded by default.
                ),
                idempotencyKey = event.idempotencyKey,
            )
            is OutboundPayload.ConsentWithdrawn -> ConsentResponseRequest(
                eventType = p.eventType.name,
                eventId = event.eventId,
                schemaVersion = ApiConfig.SCHEMA_VERSION,
                driverId = event.driverId,
                tripId = tripId,
                consentRequestId = p.consentRequestId,
                purpose = p.purpose.name,
                decision = "WITHDRAWN",
                captureMethod = p.captureMethod.name,
                language = p.language,
                capturedAt = p.capturedAt.toString(),
                evidence = ConsentEvidenceDto(transcript = null, recordingReference = null),
                idempotencyKey = event.idempotencyKey,
            )
            else -> throw IllegalArgumentException("Not a consent payload: ${p.eventType}")
        }
    }

    fun tripUpdate(event: OutboundEvent): TripUpdateRequest {
        val tripId = requireNotNull(event.tripId) { "Trip updates require a trip" }
        val base = TripUpdateRequest(
            eventType = event.payload.eventType.name,
            eventId = event.eventId,
            schemaVersion = ApiConfig.SCHEMA_VERSION,
            driverId = event.driverId,
            tripId = tripId,
            captureMethod = "",
            capturedAt = event.payload.capturedAt.toString(),
            idempotencyKey = event.idempotencyKey,
        )
        return when (val p = event.payload) {
            is OutboundPayload.EtaReported -> base.copy(
                captureMethod = p.captureMethod.name,
                eta = EtaDto(p.etaMinutes, p.approximate, p.estimatedArrivalAt.toString()),
            )
            is OutboundPayload.ArrivalReported -> base.copy(captureMethod = p.captureMethod.name, arrival = ArrivalDto(p.arrived))
            is OutboundPayload.LoadingStatusReported -> base.copy(captureMethod = p.captureMethod.name, loadingStatus = p.status.name)
            else -> throw IllegalArgumentException("Not a trip update payload: ${p.eventType}")
        }
    }

    fun supportRequest(event: OutboundEvent): SupportRequestRequest {
        val p = event.payload as OutboundPayload.SupportRequested
        return SupportRequestRequest(
            eventId = event.eventId,
            schemaVersion = ApiConfig.SCHEMA_VERSION,
            driverId = event.driverId,
            tripId = event.tripId,
            reason = p.reason.name,
            capturedAt = p.capturedAt.toString(),
            idempotencyKey = event.idempotencyKey,
        )
    }

    fun assistantEvent(event: OutboundEvent): AssistantEventRequest {
        val p = event.payload as OutboundPayload.AssistantInteraction
        return AssistantEventRequest(
            eventType = p.eventType.name,
            eventId = event.eventId,
            schemaVersion = ApiConfig.SCHEMA_VERSION,
            driverId = event.driverId,
            tripId = event.tripId,
            interaction = p.kind.name,
            promptType = p.promptType.name,
            capturedAt = p.capturedAt.toString(),
            idempotencyKey = event.idempotencyKey,
        )
    }
}

fun AckResponse.toDomain(): SubmissionAck = SubmissionAck(
    eventId = eventId,
    status = enumOf<AckStatus>(status, "status"),
    receivedAt = instant(receivedAt, "received_at"),
    serverReference = serverReference,
    consentValidation = consentValidationStatus?.let { enumOf<ConsentValidationStatus>(it, "consent_validation_status") },
    callbackScheduled = callbackScheduled,
)

fun TripDto.toDomain(now: Instant, simulated: Boolean = false): Trip = Trip(
    tripId = tripId,
    driverId = driverId,
    vehicleRegistration = vehicleRegistration,
    origin = Place(origin.name, origin.city),
    destination = Place(destination.name, destination.city),
    loadingPoint = LoadingPoint(
        loadingPoint.name,
        loadingPoint.address,
        loadingPoint.city,
        loadingPoint.reportingTime?.let { instant(it, "reporting_time") },
    ),
    status = enumOf<TripStatus>(status, "status"),
    trackingStatus = enumOf<TrackingStatus>(trackingStatus, "tracking_status"),
    loadingStatus = enumOf<LoadingStatus>(loadingStatus, "loading_status"),
    verifiedArrival = geofenceArrivalVerifiedAt?.let { VerifiedArrival(instant(it, "geofence_arrival_verified_at"), "GEOFENCE") },
    assignedAt = instant(assignedAt, "assigned_at"),
    updatedAt = now,
    simulated = simulated,
)

fun Trip.toDto(): TripDto = TripDto(
    tripId = tripId,
    driverId = driverId,
    vehicleRegistration = vehicleRegistration,
    origin = PlaceDto(origin.name, origin.city),
    destination = PlaceDto(destination.name, destination.city),
    loadingPoint = LoadingPointDto(loadingPoint.name, loadingPoint.address, loadingPoint.city, loadingPoint.reportingTime?.toString()),
    status = status.name,
    trackingStatus = trackingStatus.name,
    loadingStatus = loadingStatus.name,
    assignedAt = assignedAt.toString(),
    geofenceArrivalVerifiedAt = verifiedArrival?.verifiedAt?.toString(),
)

fun TripStateResponse.toDomain(): TripStateSnapshot = TripStateSnapshot(
    tripId = tripId,
    status = enumOf<TripStatus>(status, "status"),
    trackingStatus = enumOf<TrackingStatus>(trackingStatus, "tracking_status"),
    loadingStatus = enumOf<LoadingStatus>(loadingStatus, "loading_status"),
    consent = consent?.let { ConsentSnapshot(it.consentRequestId, enumOf<ConsentValidationStatus>(it.validationStatus, "validation_status")) },
    asOf = instant(asOf, "as_of"),
)

fun DriverProfileDto.toDomain() = DriverProfile(driverId, displayName, maskedPhone, preferredLanguage)
