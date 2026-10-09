@file:UseSerializers(InstantIsoSerializer::class)

package com.freighttiger.driverassistant.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

@Serializable
enum class OutboxStatus {
    PENDING,
    IN_FLIGHT,
    RETRY_SCHEDULED,
    /** Server acknowledged receipt. The only state that means "delivered". */
    ACKNOWLEDGED,
    /** Server rejected the event (validation error). Will not be retried. */
    FAILED_PERMANENT,
    /** Retry budget exhausted. Needs driver/support attention. */
    FAILED_MAX_RETRIES,
    /** Dropped because the trip was cancelled/completed or the request expired before sync. */
    CANCELLED_CONFLICT;

    val isOpen: Boolean get() = this == PENDING || this == IN_FLIGHT || this == RETRY_SCHEDULED
}

@Serializable
enum class OutboundEventType {
    CONSENT_RESPONSE_CAPTURED,
    CONSENT_WITHDRAWN,
    ETA_REPORTED,
    ARRIVAL_REPORTED,
    LOADING_STATUS_REPORTED,
    SUPPORT_REQUESTED,
    ASSISTANT_INTERACTION,
}

@Serializable
enum class SupportReason { DRIVER_REQUESTED, RECOGNITION_FAILED, SYNC_FAILED, TRACKING_FAILED }

@Serializable
enum class InteractionKind { PROMPT_DELIVERED, PROMPT_DEFERRED, PROMPT_ESCALATED }

@Serializable
sealed class OutboundPayload {
    abstract val eventType: OutboundEventType
    abstract val capturedAt: Instant

    @Serializable
    @SerialName("consent_response")
    data class ConsentResponse(
        val consentRequestId: String,
        val purpose: ConsentPurpose,
        val decision: ConsentDecision,
        val captureMethod: CaptureMethod,
        val language: String,
        override val capturedAt: Instant,
        val transcript: String?,
    ) : OutboundPayload() {
        override val eventType get() = OutboundEventType.CONSENT_RESPONSE_CAPTURED
    }

    @Serializable
    @SerialName("consent_withdrawn")
    data class ConsentWithdrawn(
        val consentRequestId: String,
        val purpose: ConsentPurpose,
        val captureMethod: CaptureMethod,
        val language: String,
        override val capturedAt: Instant,
    ) : OutboundPayload() {
        override val eventType get() = OutboundEventType.CONSENT_WITHDRAWN
    }

    @Serializable
    @SerialName("eta_reported")
    data class EtaReported(
        val etaMinutes: Int,
        val approximate: Boolean,
        val estimatedArrivalAt: Instant,
        val captureMethod: CaptureMethod,
        override val capturedAt: Instant,
    ) : OutboundPayload() {
        override val eventType get() = OutboundEventType.ETA_REPORTED
    }

    @Serializable
    @SerialName("arrival_reported")
    data class ArrivalReported(
        val arrived: Boolean,
        val captureMethod: CaptureMethod,
        override val capturedAt: Instant,
    ) : OutboundPayload() {
        override val eventType get() = OutboundEventType.ARRIVAL_REPORTED
    }

    @Serializable
    @SerialName("loading_status_reported")
    data class LoadingStatusReported(
        val status: LoadingStatus,
        val captureMethod: CaptureMethod,
        override val capturedAt: Instant,
    ) : OutboundPayload() {
        override val eventType get() = OutboundEventType.LOADING_STATUS_REPORTED
    }

    @Serializable
    @SerialName("support_requested")
    data class SupportRequested(
        val reason: SupportReason,
        override val capturedAt: Instant,
    ) : OutboundPayload() {
        override val eventType get() = OutboundEventType.SUPPORT_REQUESTED
    }

    @Serializable
    @SerialName("assistant_interaction")
    data class AssistantInteraction(
        val kind: InteractionKind,
        val promptType: PromptType,
        override val capturedAt: Instant,
    ) : OutboundPayload() {
        override val eventType get() = OutboundEventType.ASSISTANT_INTERACTION
    }
}

/** A durable outbound event. Adding to the outbox does NOT mean the backend received it. */
@Serializable
data class OutboundEvent(
    val eventId: String,
    val idempotencyKey: String,
    val driverId: String,
    val tripId: String?,
    val payload: OutboundPayload,
    val createdAt: Instant,
    val status: OutboxStatus = OutboxStatus.PENDING,
    val attempts: Int = 0,
    val nextAttemptAt: Instant? = null,
    val lastErrorCode: String? = null,
    val acknowledgedAt: Instant? = null,
    val serverReference: String? = null,
    /** Support requests only: true only when the backend confirmed a callback is scheduled. */
    val callbackScheduled: Boolean? = null,
)
