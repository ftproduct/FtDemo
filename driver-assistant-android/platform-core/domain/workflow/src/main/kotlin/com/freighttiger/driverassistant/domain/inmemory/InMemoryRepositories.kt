package com.freighttiger.driverassistant.domain.inmemory

import com.freighttiger.driverassistant.core.model.ActivityEntry
import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.ConsentRecord
import com.freighttiger.driverassistant.core.model.DriverSession
import com.freighttiger.driverassistant.core.model.InboundEventRecord
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.OutboxStatus
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.domain.ports.AccessTokenStore
import com.freighttiger.driverassistant.domain.ports.ActivityLog
import com.freighttiger.driverassistant.domain.ports.ConnectivityMonitor
import com.freighttiger.driverassistant.domain.ports.ConsentRepository
import com.freighttiger.driverassistant.domain.ports.InboundEventRepository
import com.freighttiger.driverassistant.domain.ports.OutboxRepository
import com.freighttiger.driverassistant.domain.ports.PromptRepository
import com.freighttiger.driverassistant.domain.ports.SessionRepository
import com.freighttiger.driverassistant.domain.ports.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

// In-memory port implementations: used by unit/integration tests and Compose previews.
// The Android app uses the Room-backed implementations in :core:database.

class InMemorySessionRepository(initial: DriverSession? = null) : SessionRepository {
    private val state = MutableStateFlow(initial)
    override fun observe(): Flow<DriverSession?> = state
    override suspend fun current(): DriverSession? = state.value
    override suspend fun save(session: DriverSession) { state.value = session }
    override suspend fun clear() { state.value = null }
}

class InMemoryTripRepository : TripRepository {
    private val state = MutableStateFlow<Map<String, Trip>>(emptyMap())
    override fun observeActiveTrip(): Flow<Trip?> = state.map { pickActive(it.values) }
    override fun observeAll(): Flow<List<Trip>> = state.map { it.values.sortedByDescending { t -> t.assignedAt } }
    override suspend fun get(tripId: String): Trip? = state.value[tripId]
    override suspend fun activeTrip(): Trip? = pickActive(state.value.values)
    override suspend fun upsert(trip: Trip) = state.update { it + (trip.tripId to trip) }

    companion object {
        /** Most recently assigned non-terminal trip; otherwise the most recently updated trip. */
        fun pickActive(trips: Collection<Trip>): Trip? =
            trips.filter { it.isActive }.maxByOrNull { it.assignedAt } ?: trips.maxByOrNull { it.updatedAt }
    }
}

class InMemoryConsentRepository : ConsentRepository {
    private val state = MutableStateFlow<Map<String, ConsentRecord>>(emptyMap())
    override fun observeForTrip(tripId: String): Flow<List<ConsentRecord>> =
        state.map { m -> m.values.filter { it.tripId == tripId }.sortedByDescending { it.requestedAt } }
    override fun observeAll(): Flow<List<ConsentRecord>> = state.map { m -> m.values.sortedByDescending { it.requestedAt } }
    override suspend fun get(consentRequestId: String): ConsentRecord? = state.value[consentRequestId]
    override suspend fun forTrip(tripId: String): List<ConsentRecord> =
        state.value.values.filter { it.tripId == tripId }.sortedByDescending { it.requestedAt }
    override suspend fun upsert(record: ConsentRecord) = state.update { it + (record.consentRequestId to record) }
}

class InMemoryInboundEventRepository : InboundEventRepository {
    private val state = MutableStateFlow<List<InboundEventRecord>>(emptyList())
    override suspend fun exists(eventId: String): Boolean = state.value.any { it.eventId == eventId }
    override suspend fun record(record: InboundEventRecord) = state.update { list -> list.filterNot { it.eventId == record.eventId } + record }
    override fun observeRecent(limit: Int): Flow<List<InboundEventRecord>> = state.map { it.sortedByDescending { r -> r.receivedAt }.take(limit) }
    val all: List<InboundEventRecord> get() = state.value
}

class InMemoryOutboxRepository : OutboxRepository {
    private val state = MutableStateFlow<Map<String, OutboundEvent>>(emptyMap())
    override suspend fun enqueue(event: OutboundEvent) = state.update { it + (event.eventId to event) }
    override suspend fun update(event: OutboundEvent) = state.update { it + (event.eventId to event) }
    override suspend fun get(eventId: String): OutboundEvent? = state.value[eventId]
    override suspend fun findByIdempotencyKey(key: String): OutboundEvent? = state.value.values.firstOrNull { it.idempotencyKey == key }
    override suspend fun due(now: Instant, limit: Int): List<OutboundEvent> = state.value.values
        .filter {
            it.status == OutboxStatus.PENDING || it.status == OutboxStatus.IN_FLIGHT ||
                (it.status == OutboxStatus.RETRY_SCHEDULED && (it.nextAttemptAt == null || !now.isBefore(it.nextAttemptAt)))
        }
        .sortedBy { it.createdAt }
        .take(limit)
    override suspend fun all(): List<OutboundEvent> = state.value.values.sortedBy { it.createdAt }
    override suspend fun forTrip(tripId: String): List<OutboundEvent> = state.value.values.filter { it.tripId == tripId }.sortedBy { it.createdAt }
    override fun observeAll(): Flow<List<OutboundEvent>> = state.map { it.values.sortedByDescending { e -> e.createdAt } }
}

class InMemoryPromptRepository : PromptRepository {
    private val state = MutableStateFlow<Map<String, AssistantPrompt>>(emptyMap())
    override suspend fun upsert(prompt: AssistantPrompt) = state.update { it + (prompt.promptId to prompt) }
    override suspend fun get(promptId: String): AssistantPrompt? = state.value[promptId]
    override suspend fun all(): List<AssistantPrompt> = state.value.values.sortedBy { it.createdAt }
    override fun observeAll(): Flow<List<AssistantPrompt>> = state.map { it.values.sortedBy { p -> p.createdAt } }
}

class InMemoryActivityLog : ActivityLog {
    private val state = MutableStateFlow<List<ActivityEntry>>(emptyList())
    override suspend fun append(entry: ActivityEntry) = state.update { it + entry }
    override fun observeRecent(limit: Int): Flow<List<ActivityEntry>> = state.map { it.sortedByDescending { e -> e.timestamp }.take(limit) }
    val entries: List<ActivityEntry> get() = state.value
}

class InMemoryAccessTokenStore : AccessTokenStore {
    private var token: String? = null
    override fun save(token: String, expiresAt: Instant) { this.token = token }
    override fun current(): String? = token
    override fun clear() { token = null }
}

class ManualConnectivityMonitor(online: Boolean = true) : ConnectivityMonitor {
    private val state = MutableStateFlow(online)
    override val isOnline: StateFlow<Boolean> = state
    fun set(online: Boolean) { state.value = online }
}

/** Controllable clock for tests and the demo. */
class MutableTimeSource(var current: java.time.Instant) : com.freighttiger.driverassistant.domain.ports.TimeSource {
    override fun now(): java.time.Instant = current
    fun advance(duration: java.time.Duration) { current = current.plus(duration) }
}

/** Deterministic ids for tests. */
class SequentialIdGenerator(private val prefix: String = "id") : com.freighttiger.driverassistant.domain.ports.IdGenerator {
    private var next = 0
    @Synchronized
    override fun newId(): String = "$prefix-${++next}"
}
