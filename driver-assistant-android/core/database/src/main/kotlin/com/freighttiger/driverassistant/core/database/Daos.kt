package com.freighttiger.driverassistant.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Upsert suspend fun upsert(entity: TripEntity)
    @Query("SELECT * FROM trips WHERE tripId = :tripId") suspend fun get(tripId: String): TripEntity?
    @Query("SELECT * FROM trips ORDER BY assignedAt DESC") suspend fun all(): List<TripEntity>
    @Query("SELECT * FROM trips ORDER BY assignedAt DESC") fun observeAll(): Flow<List<TripEntity>>
}

@Dao
interface ConsentDao {
    @Upsert suspend fun upsert(entity: ConsentEntity)
    @Query("SELECT * FROM consents WHERE consentRequestId = :id") suspend fun get(id: String): ConsentEntity?
    @Query("SELECT * FROM consents WHERE tripId = :tripId ORDER BY requestedAt DESC") suspend fun forTrip(tripId: String): List<ConsentEntity>
    @Query("SELECT * FROM consents WHERE tripId = :tripId ORDER BY requestedAt DESC") fun observeForTrip(tripId: String): Flow<List<ConsentEntity>>
    @Query("SELECT * FROM consents ORDER BY requestedAt DESC") fun observeAll(): Flow<List<ConsentEntity>>
}

@Dao
interface InboundEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: InboundEventEntity)
    @Query("SELECT COUNT(*) FROM inbound_events WHERE eventId = :eventId") suspend fun count(eventId: String): Int
    @Query("SELECT * FROM inbound_events ORDER BY receivedAt DESC LIMIT :limit") fun observeRecent(limit: Int): Flow<List<InboundEventEntity>>
}

@Dao
interface OutboxDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(entity: OutboxEntity): Long
    @Upsert suspend fun upsert(entity: OutboxEntity)
    @Query("SELECT * FROM outbox WHERE eventId = :eventId") suspend fun get(eventId: String): OutboxEntity?
    @Query("SELECT * FROM outbox WHERE idempotencyKey = :key") suspend fun findByKey(key: String): OutboxEntity?

    @Query(
        """
        SELECT * FROM outbox
        WHERE status IN ('PENDING', 'IN_FLIGHT')
           OR (status = 'RETRY_SCHEDULED' AND (nextAttemptAt IS NULL OR nextAttemptAt <= :now))
        ORDER BY createdAt ASC LIMIT :limit
        """,
    )
    suspend fun due(now: Long, limit: Int): List<OutboxEntity>

    @Query("SELECT * FROM outbox ORDER BY createdAt ASC") suspend fun all(): List<OutboxEntity>
    @Query("SELECT * FROM outbox WHERE tripId = :tripId ORDER BY createdAt ASC") suspend fun forTrip(tripId: String): List<OutboxEntity>
    @Query("SELECT * FROM outbox ORDER BY createdAt DESC") fun observeAll(): Flow<List<OutboxEntity>>
}

@Dao
interface PromptDao {
    @Upsert suspend fun upsert(entity: PromptEntity)
    @Query("SELECT * FROM prompts WHERE promptId = :id") suspend fun get(id: String): PromptEntity?
    @Query("SELECT * FROM prompts ORDER BY createdAt ASC") suspend fun all(): List<PromptEntity>
    @Query("SELECT * FROM prompts ORDER BY createdAt ASC") fun observeAll(): Flow<List<PromptEntity>>
}

@Dao
interface ActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(entity: ActivityEntity)
    @Query("SELECT * FROM activity ORDER BY timestamp DESC LIMIT :limit") fun observeRecent(limit: Int): Flow<List<ActivityEntity>>
    @Query("DELETE FROM activity WHERE timestamp < :before") suspend fun deleteOlderThan(before: Long)
}

@Dao
interface SessionDao {
    @Upsert suspend fun upsert(entity: SessionEntity)
    @Query("SELECT * FROM session WHERE id = 0") suspend fun get(): SessionEntity?
    @Query("SELECT * FROM session WHERE id = 0") fun observe(): Flow<SessionEntity?>
    @Query("DELETE FROM session") suspend fun clear()
}
