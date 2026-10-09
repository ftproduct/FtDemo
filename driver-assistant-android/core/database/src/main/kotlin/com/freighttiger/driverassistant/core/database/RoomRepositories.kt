package com.freighttiger.driverassistant.core.database

import com.freighttiger.driverassistant.core.model.ActivityEntry
import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.ConsentRecord
import com.freighttiger.driverassistant.core.model.DriverSession
import com.freighttiger.driverassistant.core.model.EventSource
import com.freighttiger.driverassistant.core.model.InboundEventRecord
import com.freighttiger.driverassistant.core.model.InboundOutcome
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.core.model.TripEventType
import com.freighttiger.driverassistant.domain.inmemory.InMemoryTripRepository
import com.freighttiger.driverassistant.domain.ports.ActivityLog
import com.freighttiger.driverassistant.domain.ports.ConsentRepository
import com.freighttiger.driverassistant.domain.ports.InboundEventRepository
import com.freighttiger.driverassistant.domain.ports.OutboxRepository
import com.freighttiger.driverassistant.domain.ports.PromptRepository
import com.freighttiger.driverassistant.domain.ports.SessionRepository
import com.freighttiger.driverassistant.domain.ports.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.Instant

internal val StorageJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

class RoomTripRepository(private val dao: TripDao) : TripRepository {
    private fun TripEntity.toDomain() = StorageJson.decodeFromString(Trip.serializer(), json)

    override fun observeActiveTrip(): Flow<Trip?> =
        dao.observeAll().map { list -> InMemoryTripRepository.pickActive(list.map { it.toDomain() }) }

    override fun observeAll(): Flow<List<Trip>> = dao.observeAll().map { list -> list.map { it.toDomain() } }
    override suspend fun get(tripId: String): Trip? = dao.get(tripId)?.toDomain()
    override suspend fun activeTrip(): Trip? = InMemoryTripRepository.pickActive(dao.all().map { it.toDomain() })

    override suspend fun upsert(trip: Trip) = dao.upsert(
        TripEntity(
            trip.tripId, trip.driverId, trip.status.name, trip.assignedAt.toEpochMilli(), trip.updatedAt.toEpochMilli(),
            StorageJson.encodeToString(Trip.serializer(), trip),
        ),
    )
}

class RoomConsentRepository(private val dao: ConsentDao) : ConsentRepository {
    private fun ConsentEntity.toDomain() = StorageJson.decodeFromString(ConsentRecord.serializer(), json)

    override fun observeForTrip(tripId: String): Flow<List<ConsentRecord>> = dao.observeForTrip(tripId).map { l -> l.map { it.toDomain() } }
    override fun observeAll(): Flow<List<ConsentRecord>> = dao.observeAll().map { l -> l.map { it.toDomain() } }
    override suspend fun get(consentRequestId: String): ConsentRecord? = dao.get(consentRequestId)?.toDomain()
    override suspend fun forTrip(tripId: String): List<ConsentRecord> = dao.forTrip(tripId).map { it.toDomain() }

    override suspend fun upsert(record: ConsentRecord) = dao.upsert(
        ConsentEntity(
            record.consentRequestId, record.tripId, record.state.name, record.requestedAt.toEpochMilli(),
            StorageJson.encodeToString(ConsentRecord.serializer(), record),
        ),
    )
}

class RoomInboundEventRepository(private val dao: InboundEventDao) : InboundEventRepository {
    override suspend fun exists(eventId: String): Boolean = dao.count(eventId) > 0

    override suspend fun record(record: InboundEventRecord) = dao.insert(
        InboundEventEntity(
            record.eventId, record.type.name, record.tripId, record.driverId, record.receivedAt.toEpochMilli(),
            record.source.name, record.outcome.name, record.reasonCode,
        ),
    )

    override fun observeRecent(limit: Int): Flow<List<InboundEventRecord>> = dao.observeRecent(limit).map { list ->
        list.map {
            InboundEventRecord(
                it.eventId, TripEventType.valueOf(it.type), it.tripId, it.driverId, Instant.ofEpochMilli(it.receivedAt),
                EventSource.valueOf(it.source), InboundOutcome.valueOf(it.outcome), it.reasonCode,
            )
        }
    }
}

class RoomOutboxRepository(private val dao: OutboxDao) : OutboxRepository {
    private fun OutboxEntity.toDomain() = StorageJson.decodeFromString(OutboundEvent.serializer(), json)
    private fun OutboundEvent.toEntity() = OutboxEntity(
        eventId, idempotencyKey, tripId, status.name, createdAt.toEpochMilli(), nextAttemptAt?.toEpochMilli(),
        StorageJson.encodeToString(OutboundEvent.serializer(), this),
    )

    /** Insert is ignored if the idempotency key already exists (unique index). */
    override suspend fun enqueue(event: OutboundEvent) {
        dao.insert(event.toEntity())
    }

    override suspend fun update(event: OutboundEvent) = dao.upsert(event.toEntity())
    override suspend fun get(eventId: String): OutboundEvent? = dao.get(eventId)?.toDomain()
    override suspend fun findByIdempotencyKey(key: String): OutboundEvent? = dao.findByKey(key)?.toDomain()
    override suspend fun due(now: Instant, limit: Int): List<OutboundEvent> = dao.due(now.toEpochMilli(), limit).map { it.toDomain() }
    override suspend fun all(): List<OutboundEvent> = dao.all().map { it.toDomain() }
    override suspend fun forTrip(tripId: String): List<OutboundEvent> = dao.forTrip(tripId).map { it.toDomain() }
    override fun observeAll(): Flow<List<OutboundEvent>> = dao.observeAll().map { l -> l.map { it.toDomain() } }
}

class RoomPromptRepository(private val dao: PromptDao) : PromptRepository {
    private fun PromptEntity.toDomain() = StorageJson.decodeFromString(AssistantPrompt.serializer(), json)

    override suspend fun upsert(prompt: AssistantPrompt) = dao.upsert(
        PromptEntity(prompt.promptId, prompt.tripId, prompt.status.name, prompt.createdAt.toEpochMilli(), StorageJson.encodeToString(AssistantPrompt.serializer(), prompt)),
    )

    override suspend fun get(promptId: String): AssistantPrompt? = dao.get(promptId)?.toDomain()
    override suspend fun all(): List<AssistantPrompt> = dao.all().map { it.toDomain() }
    override fun observeAll(): Flow<List<AssistantPrompt>> = dao.observeAll().map { l -> l.map { it.toDomain() } }
}

class RoomActivityLog(private val dao: ActivityDao) : ActivityLog {
    override suspend fun append(entry: ActivityEntry) = dao.insert(
        ActivityEntity(entry.id, entry.timestamp.toEpochMilli(), entry.tripId, StorageJson.encodeToString(ActivityEntry.serializer(), entry)),
    )

    override fun observeRecent(limit: Int): Flow<List<ActivityEntry>> =
        dao.observeRecent(limit).map { l -> l.map { StorageJson.decodeFromString(ActivityEntry.serializer(), it.json) } }

    /** Retention: driver-visible history is kept for a bounded period. */
    suspend fun pruneOlderThan(before: Instant) = dao.deleteOlderThan(before.toEpochMilli())
}

class RoomSessionRepository(private val dao: SessionDao) : SessionRepository {
    override fun observe(): Flow<DriverSession?> = dao.observe().map { e -> e?.let { StorageJson.decodeFromString(DriverSession.serializer(), it.json) } }
    override suspend fun current(): DriverSession? = dao.get()?.let { StorageJson.decodeFromString(DriverSession.serializer(), it.json) }
    override suspend fun save(session: DriverSession) = dao.upsert(SessionEntity(0, StorageJson.encodeToString(DriverSession.serializer(), session)))
    override suspend fun clear() = dao.clear()
}
