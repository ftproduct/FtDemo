package com.freighttiger.driverassistant.domain.workflow

import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.PromptStatus
import com.freighttiger.driverassistant.core.model.PromptType
import com.freighttiger.driverassistant.domain.ports.IdGenerator
import com.freighttiger.driverassistant.domain.ports.PromptRepository
import com.freighttiger.driverassistant.domain.ports.TimeSource
import java.time.Duration
import java.time.Instant

data class PromptPolicy(
    /** Minimum gap before the same prompt type is asked again for the same trip. */
    val cooldown: Duration = Duration.ofMinutes(10),
    /** Default "ask me later" delay. */
    val deferDuration: Duration = Duration.ofMinutes(10),
    /** A prompt is dropped after this many deliveries without an answer. */
    val maxDeliveries: Int = 4,
    /** When true, only urgent/informational prompts are presented (driver paused prompts). */
    val nonUrgentPaused: Boolean = false,
)

data class PromptRequest(
    val tripId: String,
    val type: PromptType,
    val consentRequestId: String? = null,
    val expiresAt: Instant? = null,
    val detail: String? = null,
    val simulated: Boolean = false,
    /** Informational prompts bypass the cooldown (e.g. trip cancelled). */
    val bypassCooldown: Boolean = false,
)

sealed interface ScheduleDecision {
    data class Scheduled(val prompt: AssistantPrompt) : ScheduleDecision
    data class Suppressed(val reasonCode: String) : ScheduleDecision
}

/** Duplicate suppression, cooldowns, deferral and expiry for assistant prompts. */
class PromptScheduler(
    private val repository: PromptRepository,
    private val ids: IdGenerator,
    private val clock: TimeSource,
    @Volatile var policy: PromptPolicy = PromptPolicy(),
) {

    suspend fun schedule(request: PromptRequest): ScheduleDecision {
        val now = clock.now()
        if (request.expiresAt != null && !now.isBefore(request.expiresAt)) {
            return ScheduleDecision.Suppressed("PROMPT_ALREADY_EXPIRED")
        }
        val key = dedupeKey(request)
        val all = repository.all()
        if (all.any { it.dedupeKey == key && it.status.isOpen }) {
            return ScheduleDecision.Suppressed("DUPLICATE_PROMPT")
        }
        if (!request.bypassCooldown) {
            val recent = all.filter { it.tripId == request.tripId && it.type == request.type && it.lastDeliveredAt != null }
                .maxByOrNull { it.lastDeliveredAt!! }
            if (recent != null && Duration.between(recent.lastDeliveredAt, now) < policy.cooldown &&
                recent.consentRequestId == request.consentRequestId
            ) {
                return ScheduleDecision.Suppressed("COOLDOWN")
            }
        }
        val prompt = AssistantPrompt(
            promptId = ids.newId(),
            tripId = request.tripId,
            type = request.type,
            dedupeKey = key,
            createdAt = now,
            consentRequestId = request.consentRequestId,
            expiresAt = request.expiresAt,
            detail = request.detail,
            simulated = request.simulated,
        )
        repository.upsert(prompt)
        return ScheduleDecision.Scheduled(prompt)
    }

    /** Highest-priority prompt that may be presented now, or null. */
    suspend fun nextDue(): AssistantPrompt? {
        expireStale()
        val now = clock.now()
        return repository.all()
            .asSequence()
            .filter { it.status == PromptStatus.PENDING || it.status == PromptStatus.DEFERRED || it.status == PromptStatus.DELIVERED }
            .filter { it.deferredUntil == null || !now.isBefore(it.deferredUntil) }
            .filter { !policy.nonUrgentPaused || !it.type.expectsResponse }
            .sortedWith(compareByDescending<AssistantPrompt> { it.type.priority }.thenBy { it.createdAt })
            .firstOrNull()
    }

    suspend fun openPrompts(): List<AssistantPrompt> = repository.all().filter { it.status.isOpen }

    suspend fun markDelivered(promptId: String): AssistantPrompt? = update(promptId) {
        it.copy(status = PromptStatus.DELIVERED, deliveryCount = it.deliveryCount + 1, lastDeliveredAt = clock.now(), deferredUntil = null)
    }

    suspend fun defer(promptId: String, duration: Duration = policy.deferDuration): AssistantPrompt? = update(promptId) {
        if (it.deliveryCount >= policy.maxDeliveries) {
            it.copy(status = PromptStatus.EXPIRED)
        } else {
            it.copy(status = PromptStatus.DEFERRED, deferredUntil = clock.now().plus(duration))
        }
    }

    suspend fun complete(promptId: String): AssistantPrompt? = update(promptId) { it.copy(status = PromptStatus.COMPLETED) }

    suspend fun cancelForTrip(tripId: String, types: Set<PromptType>? = null) {
        repository.all()
            .filter { it.tripId == tripId && it.status.isOpen && (types == null || it.type in types) }
            .forEach { repository.upsert(it.copy(status = PromptStatus.CANCELLED)) }
    }

    suspend fun cancelForConsentRequest(consentRequestId: String) {
        repository.all()
            .filter { it.consentRequestId == consentRequestId && it.status.isOpen && it.type == PromptType.CONSENT }
            .forEach { repository.upsert(it.copy(status = PromptStatus.CANCELLED)) }
    }

    suspend fun expireStale() {
        val now = clock.now()
        repository.all()
            .filter { it.status.isOpen && it.expiresAt != null && !now.isBefore(it.expiresAt) }
            .forEach { repository.upsert(it.copy(status = PromptStatus.EXPIRED)) }
    }

    private suspend fun update(promptId: String, transform: (AssistantPrompt) -> AssistantPrompt): AssistantPrompt? {
        val current = repository.get(promptId) ?: return null
        if (!current.status.isOpen) return current
        val next = transform(current)
        repository.upsert(next)
        return next
    }

    private fun dedupeKey(r: PromptRequest) =
        listOfNotNull(r.tripId, r.type.name, r.consentRequestId, r.detail).joinToString(":")
}
