@file:UseSerializers(InstantIsoSerializer::class)

package com.freighttiger.driverassistant.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Instant

@Serializable
enum class ActivityKind {
    EVENT_RECEIVED,
    EVENT_REJECTED,
    PROMPT_SCHEDULED,
    PROMPT_SUPPRESSED,
    PROMPT_DEFERRED,
    DRIVER_RESPONSE,
    CONSENT_UPDATE,
    TRACKING_UPDATE,
    TRIP_UPDATE,
    SYNC_SUCCESS,
    SYNC_FAILURE,
    SUPPORT,
    CONFLICT,
}

/**
 * Entry in the driver-visible activity history. [detail] is a short structured description
 * (codes, statuses, numbers). It must never contain raw transcripts or phone numbers.
 */
@Serializable
data class ActivityEntry(
    val id: String,
    val timestamp: Instant,
    val kind: ActivityKind,
    val detail: String,
    val tripId: String? = null,
    val simulated: Boolean = false,
)
