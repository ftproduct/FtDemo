@file:UseSerializers(InstantIsoSerializer::class)

package com.freighttiger.driverassistant.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

@Serializable
enum class TripEventType {
    TRIP_ASSIGNED,
    CONSENT_REQUESTED,
    TRACKING_STATUS_UPDATED,
    MILESTONE_UPDATE_REQUESTED,
    TRIP_CANCELLED,
    TRIP_COMPLETED,
}

@Serializable
enum class EventSource { PUSH, POLL, SIMULATED }

@Serializable
enum class Milestone { ETA_TO_LOADING_POINT, ARRIVAL_AT_LOADING_POINT, LOADING_STATUS }

/** A structured trip event received from the Freight Tiger backend (push, poll or demo simulation). */
data class InboundTripEvent(
    val eventId: String,
    val schemaVersion: String,
    val type: TripEventType,
    val driverId: String,
    val tripId: String,
    val issuedAt: Instant,
    val expiresAt: Instant? = null,
    val source: EventSource,
    val payload: InboundPayload,
)

sealed interface InboundPayload {
    data class TripAssigned(val trip: Trip) : InboundPayload

    data class ConsentRequested(
        val consentRequestId: String,
        val purpose: ConsentPurpose,
        val expiresAt: Instant,
    ) : InboundPayload

    data class TrackingStatusUpdated(
        val trackingStatus: TrackingStatus,
        val consentRequestId: String?,
        val consentValidation: ConsentValidationStatus?,
        val reasonCode: String? = null,
    ) : InboundPayload

    data class MilestoneUpdateRequested(val milestone: Milestone) : InboundPayload

    data class TripCancelled(val reasonCode: String?) : InboundPayload

    data object TripCompleted : InboundPayload
}

@Serializable
enum class InboundOutcome { APPLIED, DUPLICATE, REJECTED, IGNORED }

/** Persisted record of a received event. Used for duplicate suppression and event history. */
@Serializable
data class InboundEventRecord(
    val eventId: String,
    val type: TripEventType,
    val tripId: String,
    val driverId: String,
    val receivedAt: Instant,
    val source: EventSource,
    val outcome: InboundOutcome,
    val reasonCode: String? = null,
)
