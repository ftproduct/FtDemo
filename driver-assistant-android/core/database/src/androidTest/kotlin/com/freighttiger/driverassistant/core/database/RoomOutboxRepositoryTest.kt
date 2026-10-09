package com.freighttiger.driverassistant.core.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.OutboundPayload
import com.freighttiger.driverassistant.core.model.OutboxStatus
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class RoomOutboxRepositoryTest {
    private lateinit var db: AssistantDatabase
    private lateinit var repo: RoomOutboxRepository
    private val t0 = Instant.parse("2026-10-09T12:00:00Z")

    @Before fun setUp() {
        db = AssistantDatabase.inMemory(ApplicationProvider.getApplicationContext())
        repo = RoomOutboxRepository(db.outbox())
    }

    @After fun tearDown() = db.close()

    private fun event(id: String, key: String, status: OutboxStatus = OutboxStatus.PENDING, next: Instant? = null) = OutboundEvent(
        eventId = id, idempotencyKey = key, driverId = "d", tripId = "t",
        payload = OutboundPayload.ArrivalReported(true, CaptureMethod.TAP, t0),
        createdAt = t0, status = status, nextAttemptAt = next,
    )

    @Test fun duplicateIdempotencyKeyIsIgnored() = runTest {
        repo.enqueue(event("e1", "k1"))
        repo.enqueue(event("e2", "k1"))
        assertEquals(1, repo.all().size)
    }

    @Test fun dueRespectsRetrySchedule() = runTest {
        repo.enqueue(event("e1", "k1"))
        repo.enqueue(event("e2", "k2", OutboxStatus.RETRY_SCHEDULED, t0.plusSeconds(60)))
        repo.enqueue(event("e3", "k3", OutboxStatus.ACKNOWLEDGED))
        assertEquals(listOf("e1"), repo.due(t0, 10).map { it.eventId })
        assertEquals(setOf("e1", "e2"), repo.due(t0.plusSeconds(60), 10).map { it.eventId }.toSet())
    }

    @Test fun payloadRoundTrips() = runTest {
        val e = event("e1", "k1")
        repo.enqueue(e)
        assertEquals(e, repo.get("e1"))
    }
}
