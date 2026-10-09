package com.freighttiger.driverassistant.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Domain objects are stored as JSON (kotlinx.serialization) next to the few columns needed for
// querying. This keeps the schema small and mapping code trivial while the contracts evolve.

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val tripId: String,
    val driverId: String,
    val status: String,
    val assignedAt: Long,
    val updatedAt: Long,
    val json: String,
)

@Entity(tableName = "consents", indices = [Index("tripId")])
data class ConsentEntity(
    @PrimaryKey val consentRequestId: String,
    val tripId: String,
    val state: String,
    val requestedAt: Long,
    val json: String,
)

@Entity(tableName = "inbound_events", indices = [Index("receivedAt")])
data class InboundEventEntity(
    @PrimaryKey val eventId: String,
    val type: String,
    val tripId: String,
    val driverId: String,
    val receivedAt: Long,
    val source: String,
    val outcome: String,
    val reasonCode: String?,
)

@Entity(
    tableName = "outbox",
    indices = [Index(value = ["idempotencyKey"], unique = true), Index("tripId"), Index("status")],
)
data class OutboxEntity(
    @PrimaryKey val eventId: String,
    val idempotencyKey: String,
    val tripId: String?,
    val status: String,
    val createdAt: Long,
    val nextAttemptAt: Long?,
    val json: String,
)

@Entity(tableName = "prompts", indices = [Index("tripId")])
data class PromptEntity(
    @PrimaryKey val promptId: String,
    val tripId: String,
    val status: String,
    val createdAt: Long,
    val json: String,
)

@Entity(tableName = "activity", indices = [Index("timestamp")])
data class ActivityEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val tripId: String?,
    val json: String,
)

@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = 0,
    val json: String,
)
