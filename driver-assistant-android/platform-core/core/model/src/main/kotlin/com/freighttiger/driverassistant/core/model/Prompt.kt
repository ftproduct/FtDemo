@file:UseSerializers(InstantIsoSerializer::class)

package com.freighttiger.driverassistant.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

/** Prompt types, highest priority first. */
@Serializable
enum class PromptType(val priority: Int, val expectsResponse: Boolean) {
    TRIP_CANCELLED(100, false),
    TRACKING_RESULT(90, false),
    TRIP_BRIEFING(80, false),
    CONSENT(70, true),
    ARRIVAL(60, true),
    ETA(50, true),
    LOADING_STATUS(40, true),
    TRIP_COMPLETED(30, false);

    val questionContext: QuestionContext
        get() = when (this) {
            CONSENT -> QuestionContext.CONSENT
            ARRIVAL -> QuestionContext.ARRIVAL
            ETA -> QuestionContext.ETA
            LOADING_STATUS -> QuestionContext.LOADING_STATUS
            else -> QuestionContext.GENERAL
        }
}

@Serializable
enum class PromptStatus {
    PENDING,
    DEFERRED,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    EXPIRED;

    val isOpen: Boolean get() = this == PENDING || this == DEFERRED || this == DELIVERED
}

@Serializable
data class AssistantPrompt(
    val promptId: String,
    val tripId: String,
    val type: PromptType,
    val dedupeKey: String,
    val createdAt: Instant,
    val status: PromptStatus = PromptStatus.PENDING,
    val consentRequestId: String? = null,
    val expiresAt: Instant? = null,
    val deferredUntil: Instant? = null,
    val deliveryCount: Int = 0,
    val lastDeliveredAt: Instant? = null,
    /** Extra detail for informational prompts, e.g. the tracking outcome. */
    val detail: String? = null,
    val simulated: Boolean = false,
)
