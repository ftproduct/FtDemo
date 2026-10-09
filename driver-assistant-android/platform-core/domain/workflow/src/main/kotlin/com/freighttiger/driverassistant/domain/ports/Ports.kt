package com.freighttiger.driverassistant.domain.ports

import com.freighttiger.driverassistant.core.model.ActivityEntry
import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.ConsentRecord
import com.freighttiger.driverassistant.core.model.DriverSession
import com.freighttiger.driverassistant.core.model.InboundEventRecord
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.util.UUID

fun interface TimeSource {
    fun now(): Instant

    companion object {
        val System = TimeSource { Instant.now() }
    }
}

fun interface IdGenerator {
    fun newId(): String

    companion object {
        val Uuid = IdGenerator { UUID.randomUUID().toString() }
    }
}

/** Network reachability as seen by the app (OS connectivity combined with any demo override). */
interface ConnectivityMonitor {
    val isOnline: StateFlow<Boolean>
}

interface SessionRepository {
    fun observe(): Flow<DriverSession?>
    suspend fun current(): DriverSession?
    suspend fun save(session: DriverSession)
    suspend fun clear()
}

interface TripRepository {
    fun observeActiveTrip(): Flow<Trip?>
    fun observeAll(): Flow<List<Trip>>
    suspend fun get(tripId: String): Trip?
    suspend fun activeTrip(): Trip?
    suspend fun upsert(trip: Trip)
}

interface ConsentRepository {
    fun observeForTrip(tripId: String): Flow<List<ConsentRecord>>
    fun observeAll(): Flow<List<ConsentRecord>>
    suspend fun get(consentRequestId: String): ConsentRecord?
    suspend fun forTrip(tripId: String): List<ConsentRecord>
    suspend fun upsert(record: ConsentRecord)
}

interface InboundEventRepository {
    suspend fun exists(eventId: String): Boolean
    suspend fun record(record: InboundEventRecord)
    fun observeRecent(limit: Int): Flow<List<InboundEventRecord>>
}

interface OutboxRepository {
    suspend fun enqueue(event: OutboundEvent)
    suspend fun update(event: OutboundEvent)
    suspend fun get(eventId: String): OutboundEvent?
    suspend fun findByIdempotencyKey(key: String): OutboundEvent?
    /**
     * Events to send now, oldest first: PENDING, RETRY_SCHEDULED with nextAttemptAt <= now, and
     * IN_FLIGHT left over from a previous process (safe to resend thanks to idempotency keys).
     */
    suspend fun due(now: Instant, limit: Int): List<OutboundEvent>
    suspend fun all(): List<OutboundEvent>
    suspend fun forTrip(tripId: String): List<OutboundEvent>
    fun observeAll(): Flow<List<OutboundEvent>>
}

interface PromptRepository {
    suspend fun upsert(prompt: AssistantPrompt)
    suspend fun get(promptId: String): AssistantPrompt?
    suspend fun all(): List<AssistantPrompt>
    fun observeAll(): Flow<List<AssistantPrompt>>
}

interface ActivityLog {
    suspend fun append(entry: ActivityEntry)
    fun observeRecent(limit: Int): Flow<List<ActivityEntry>>
}

/** Stores the backend access token. Android implementation is Keystore-backed; never plain text. */
interface AccessTokenStore {
    fun save(token: String, expiresAt: Instant)
    fun current(): String?
    fun clear()
}
