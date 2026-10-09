package com.freighttiger.driverassistant.core.network.push

import com.freighttiger.driverassistant.core.model.ConsentPurpose
import com.freighttiger.driverassistant.core.model.ConsentValidationStatus
import com.freighttiger.driverassistant.core.model.EventSource
import com.freighttiger.driverassistant.core.model.InboundPayload
import com.freighttiger.driverassistant.core.model.InboundTripEvent
import com.freighttiger.driverassistant.core.model.Milestone
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.core.model.TripEventType
import com.freighttiger.driverassistant.core.network.dto.ConsentRequestedPayloadDto
import com.freighttiger.driverassistant.core.network.dto.ContractViolation
import com.freighttiger.driverassistant.core.network.dto.InboundEventEnvelopeDto
import com.freighttiger.driverassistant.core.network.dto.MilestonePayloadDto
import com.freighttiger.driverassistant.core.network.dto.TrackingStatusPayloadDto
import com.freighttiger.driverassistant.core.network.dto.TripAssignedPayloadDto
import com.freighttiger.driverassistant.core.network.dto.TripCancelledPayloadDto
import com.freighttiger.driverassistant.core.network.dto.enumOf
import com.freighttiger.driverassistant.core.network.dto.instant
import com.freighttiger.driverassistant.core.network.dto.toDomain
import com.freighttiger.driverassistant.core.network.remote.NetworkFactory
import com.freighttiger.driverassistant.domain.ports.TimeSource
import kotlinx.serialization.SerializationException

sealed interface ParseResult {
    data class Parsed(val event: InboundTripEvent) : ParseResult
    data class Invalid(val reasonCode: String) : ParseResult
}

/**
 * Parses a backend trip event (FCM data message or poll response) into the domain model.
 * FCM data messages carry the JSON envelope in the `ft_event` key.
 */
class InboundEventParser(private val clock: TimeSource = TimeSource.System) {
    private val json = NetworkFactory.json

    fun fromPushData(data: Map<String, String>): ParseResult {
        val raw = data[PUSH_DATA_KEY] ?: return ParseResult.Invalid("MISSING_EVENT_PAYLOAD")
        return parse(raw, EventSource.PUSH)
    }

    fun parse(raw: String, source: EventSource): ParseResult {
        val envelope = try {
            json.decodeFromString(InboundEventEnvelopeDto.serializer(), raw)
        } catch (e: SerializationException) {
            return ParseResult.Invalid("MALFORMED_ENVELOPE")
        } catch (e: IllegalArgumentException) {
            return ParseResult.Invalid("MALFORMED_ENVELOPE")
        }
        return try {
            val type = enumOf<TripEventType>(envelope.eventType, "event_type")
            val p = envelope.payload
            val payload: InboundPayload = when (type) {
                TripEventType.TRIP_ASSIGNED -> InboundPayload.TripAssigned(
                    json.decodeFromJsonElement(TripAssignedPayloadDto.serializer(), p).trip
                        .toDomain(clock.now(), simulated = source == EventSource.SIMULATED),
                )
                TripEventType.CONSENT_REQUESTED -> json.decodeFromJsonElement(ConsentRequestedPayloadDto.serializer(), p).let {
                    InboundPayload.ConsentRequested(
                        it.consentRequestId,
                        enumOf<ConsentPurpose>(it.purpose, "purpose"),
                        instant(it.expiresAt, "expires_at"),
                    )
                }
                TripEventType.TRACKING_STATUS_UPDATED -> json.decodeFromJsonElement(TrackingStatusPayloadDto.serializer(), p).let {
                    InboundPayload.TrackingStatusUpdated(
                        enumOf<TrackingStatus>(it.trackingStatus, "tracking_status"),
                        it.consentRequestId,
                        it.consentValidationStatus?.let { v -> enumOf<ConsentValidationStatus>(v, "consent_validation_status") },
                        it.reasonCode,
                    )
                }
                TripEventType.MILESTONE_UPDATE_REQUESTED -> InboundPayload.MilestoneUpdateRequested(
                    enumOf<Milestone>(json.decodeFromJsonElement(MilestonePayloadDto.serializer(), p).milestone, "milestone"),
                )
                TripEventType.TRIP_CANCELLED -> InboundPayload.TripCancelled(
                    json.decodeFromJsonElement(TripCancelledPayloadDto.serializer(), p).reasonCode,
                )
                TripEventType.TRIP_COMPLETED -> InboundPayload.TripCompleted
            }
            ParseResult.Parsed(
                InboundTripEvent(
                    eventId = envelope.eventId,
                    schemaVersion = envelope.schemaVersion,
                    type = type,
                    driverId = envelope.driverId,
                    tripId = envelope.tripId,
                    issuedAt = instant(envelope.issuedAt, "issued_at"),
                    expiresAt = envelope.expiresAt?.let { instant(it, "expires_at") },
                    source = source,
                    payload = payload,
                ),
            )
        } catch (e: ContractViolation) {
            ParseResult.Invalid("CONTRACT_VIOLATION")
        } catch (e: SerializationException) {
            ParseResult.Invalid("MALFORMED_PAYLOAD")
        } catch (e: IllegalArgumentException) {
            ParseResult.Invalid("MALFORMED_PAYLOAD")
        }
    }

    companion object {
        const val PUSH_DATA_KEY = "ft_event"
    }
}
