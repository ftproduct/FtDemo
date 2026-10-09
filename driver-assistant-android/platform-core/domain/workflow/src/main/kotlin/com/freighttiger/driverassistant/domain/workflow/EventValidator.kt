package com.freighttiger.driverassistant.domain.workflow

import com.freighttiger.driverassistant.core.model.InboundPayload
import com.freighttiger.driverassistant.core.model.InboundTripEvent
import com.freighttiger.driverassistant.core.model.TripEventType

/** Structural validation of inbound events, independent of local state. */
class EventValidator(private val supportedSchemaMajor: Int = 1) {

    fun validate(event: InboundTripEvent): String? {
        if (event.eventId.isBlank()) return "MISSING_EVENT_ID"
        if (event.driverId.isBlank()) return "MISSING_DRIVER_ID"
        if (event.tripId.isBlank()) return "MISSING_TRIP_ID"
        val major = event.schemaVersion.substringBefore('.').toIntOrNull() ?: return "INVALID_SCHEMA_VERSION"
        if (major != supportedSchemaMajor) return "UNSUPPORTED_SCHEMA_VERSION"
        val payloadMatches = when (event.payload) {
            is InboundPayload.TripAssigned -> event.type == TripEventType.TRIP_ASSIGNED
            is InboundPayload.ConsentRequested -> event.type == TripEventType.CONSENT_REQUESTED
            is InboundPayload.TrackingStatusUpdated -> event.type == TripEventType.TRACKING_STATUS_UPDATED
            is InboundPayload.MilestoneUpdateRequested -> event.type == TripEventType.MILESTONE_UPDATE_REQUESTED
            is InboundPayload.TripCancelled -> event.type == TripEventType.TRIP_CANCELLED
            InboundPayload.TripCompleted -> event.type == TripEventType.TRIP_COMPLETED
        }
        if (!payloadMatches) return "PAYLOAD_TYPE_MISMATCH"
        val payload = event.payload
        if (payload is InboundPayload.TripAssigned) {
            if (payload.trip.tripId != event.tripId) return "TRIP_ID_MISMATCH"
            if (payload.trip.driverId != event.driverId) return "DRIVER_ID_MISMATCH"
        }
        if (payload is InboundPayload.ConsentRequested && payload.consentRequestId.isBlank()) {
            return "MISSING_CONSENT_REQUEST_ID"
        }
        return null
    }
}
