@file:UseSerializers(InstantIsoSerializer::class)

package com.freighttiger.driverassistant.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

@Serializable
enum class TripStatus {
    ASSIGNED,
    IN_PROGRESS,
    CANCELLED,
    COMPLETED;

    val isTerminal: Boolean get() = this == CANCELLED || this == COMPLETED
}

/**
 * Tracking status as last confirmed by the backend. The app never moves this to [ACTIVE] by itself:
 * only a backend confirmation (push event or trip-state response) can.
 */
@Serializable
enum class TrackingStatus {
    NOT_STARTED,
    PENDING_ACTIVATION,
    ACTIVE,
    FAILED,
    STOP_REQUESTED,
    STOPPED,
}

/** Driver-reportable loading progress. Backend validation remains authoritative. */
@Serializable
enum class LoadingStatus {
    NOT_REACHED,
    REACHED_LOADING_POINT,
    WAITING_FOR_LOADING,
    LOADING_STARTED,
    LOADING_COMPLETED,
    ISSUE_REPORTED;

    fun allowedNext(): Set<LoadingStatus> = when (this) {
        NOT_REACHED -> setOf(REACHED_LOADING_POINT, ISSUE_REPORTED)
        REACHED_LOADING_POINT -> setOf(WAITING_FOR_LOADING, LOADING_STARTED, ISSUE_REPORTED)
        WAITING_FOR_LOADING -> setOf(LOADING_STARTED, ISSUE_REPORTED)
        LOADING_STARTED -> setOf(LOADING_COMPLETED, ISSUE_REPORTED)
        LOADING_COMPLETED -> emptySet()
        ISSUE_REPORTED -> setOf(REACHED_LOADING_POINT, WAITING_FOR_LOADING, LOADING_STARTED, LOADING_COMPLETED)
    }

    fun canTransitionTo(next: LoadingStatus): Boolean = next in allowedNext()
}

@Serializable
data class Place(val name: String, val city: String)

@Serializable
data class LoadingPoint(
    val name: String,
    val address: String,
    val city: String,
    val reportingTime: Instant? = null,
)

/** ETA supplied by the driver. It is NOT a GPS-derived prediction and must never be presented as one. */
@Serializable
data class DriverReportedEta(
    val minutes: Int,
    val approximate: Boolean,
    val reportedAt: Instant,
    val estimatedArrivalAt: Instant,
)

/** Arrival as reported by the driver. Kept separate from [VerifiedArrival]. */
@Serializable
data class DriverReportedArrival(val arrived: Boolean, val reportedAt: Instant)

/** Arrival verified by the backend (e.g. GPS/SIM geofence). */
@Serializable
data class VerifiedArrival(val verifiedAt: Instant, val method: String)

@Serializable
data class Trip(
    val tripId: String,
    val driverId: String,
    val vehicleRegistration: String,
    val origin: Place,
    val destination: Place,
    val loadingPoint: LoadingPoint,
    val status: TripStatus,
    val trackingStatus: TrackingStatus = TrackingStatus.NOT_STARTED,
    val loadingStatus: LoadingStatus = LoadingStatus.NOT_REACHED,
    val driverReportedEta: DriverReportedEta? = null,
    val driverReportedArrival: DriverReportedArrival? = null,
    val verifiedArrival: VerifiedArrival? = null,
    val assignedAt: Instant,
    val updatedAt: Instant,
    /** True when this trip came from the demo/mock backend. Never shown as a real Freight Tiger trip. */
    val simulated: Boolean = false,
) {
    val isActive: Boolean get() = !status.isTerminal
}
