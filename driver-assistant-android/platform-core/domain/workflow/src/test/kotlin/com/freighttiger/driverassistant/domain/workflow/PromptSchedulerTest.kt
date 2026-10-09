package com.freighttiger.driverassistant.domain.workflow

import com.freighttiger.driverassistant.core.model.PromptStatus
import com.freighttiger.driverassistant.core.model.PromptType
import com.freighttiger.driverassistant.domain.support.TRIP
import com.freighttiger.driverassistant.domain.support.TestHarness
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class PromptSchedulerTest {

    @Test
    fun `cooldown suppresses re-asking the same question too soon`() = runTest {
        val h = TestHarness()
        val p = (h.scheduler.schedule(PromptRequest(TRIP, PromptType.ETA)) as ScheduleDecision.Scheduled).prompt
        h.scheduler.markDelivered(p.promptId)
        h.scheduler.complete(p.promptId)
        assertEquals(ScheduleDecision.Suppressed("COOLDOWN"), h.scheduler.schedule(PromptRequest(TRIP, PromptType.ETA)))
        h.clock.advance(Duration.ofMinutes(11))
        assertTrue(h.scheduler.schedule(PromptRequest(TRIP, PromptType.ETA)) is ScheduleDecision.Scheduled)
    }

    @Test
    fun `deferred prompt is not due until the deferral passes`() = runTest {
        val h = TestHarness()
        val p = (h.scheduler.schedule(PromptRequest(TRIP, PromptType.ARRIVAL)) as ScheduleDecision.Scheduled).prompt
        h.scheduler.defer(p.promptId, Duration.ofMinutes(10))
        assertNull(h.scheduler.nextDue())
        h.clock.advance(Duration.ofMinutes(10))
        assertEquals(p.promptId, h.scheduler.nextDue()?.promptId)
    }

    @Test
    fun `priority orders consent before eta`() = runTest {
        val h = TestHarness()
        h.scheduler.schedule(PromptRequest(TRIP, PromptType.ETA))
        h.scheduler.schedule(PromptRequest(TRIP, PromptType.CONSENT, "c-1"))
        assertEquals(PromptType.CONSENT, h.scheduler.nextDue()?.type)
    }

    @Test
    fun `paused non-urgent prompts still allow informational prompts`() = runTest {
        val h = TestHarness()
        h.scheduler.policy = PromptPolicy(nonUrgentPaused = true)
        h.scheduler.schedule(PromptRequest(TRIP, PromptType.ETA))
        assertNull(h.scheduler.nextDue())
        h.scheduler.schedule(PromptRequest(TRIP, PromptType.TRIP_CANCELLED, bypassCooldown = true))
        assertEquals(PromptType.TRIP_CANCELLED, h.scheduler.nextDue()?.type)
    }

    @Test
    fun `expired prompts are dropped`() = runTest {
        val h = TestHarness()
        val p = (h.scheduler.schedule(PromptRequest(TRIP, PromptType.CONSENT, "c-1", h.clock.now().plusSeconds(60))) as ScheduleDecision.Scheduled).prompt
        h.clock.advance(Duration.ofMinutes(2))
        assertNull(h.scheduler.nextDue())
        assertEquals(PromptStatus.EXPIRED, h.prompts.get(p.promptId)?.status)
    }

    @Test
    fun `prompt is dropped after too many unanswered deliveries`() = runTest {
        val h = TestHarness()
        val p = (h.scheduler.schedule(PromptRequest(TRIP, PromptType.ETA)) as ScheduleDecision.Scheduled).prompt
        repeat(4) {
            h.scheduler.markDelivered(p.promptId)
            h.scheduler.defer(p.promptId)
            h.clock.advance(Duration.ofMinutes(11))
        }
        assertEquals(PromptStatus.EXPIRED, h.prompts.get(p.promptId)?.status)
    }
}
