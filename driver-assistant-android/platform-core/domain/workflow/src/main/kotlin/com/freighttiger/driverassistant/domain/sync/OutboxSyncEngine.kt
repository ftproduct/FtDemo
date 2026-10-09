package com.freighttiger.driverassistant.domain.sync

import com.freighttiger.driverassistant.core.model.ActivityKind
import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.OutboundEventType
import com.freighttiger.driverassistant.core.model.OutboundPayload
import com.freighttiger.driverassistant.core.model.OutboxStatus
import com.freighttiger.driverassistant.core.model.PromptType
import com.freighttiger.driverassistant.core.model.TripStatus
import com.freighttiger.driverassistant.domain.backend.AckStatus
import com.freighttiger.driverassistant.domain.backend.AssistantBackend
import com.freighttiger.driverassistant.domain.backend.BackendError
import com.freighttiger.driverassistant.domain.backend.BackendResult
import com.freighttiger.driverassistant.domain.backend.SubmissionAck
import com.freighttiger.driverassistant.domain.backend.code
import com.freighttiger.driverassistant.domain.consent.ConsentEvent
import com.freighttiger.driverassistant.domain.consent.ConsentStateMachine
import com.freighttiger.driverassistant.domain.consent.ConsentTransition
import com.freighttiger.driverassistant.domain.ports.ConnectivityMonitor
import com.freighttiger.driverassistant.domain.ports.ConsentRepository
import com.freighttiger.driverassistant.domain.ports.OutboxRepository
import com.freighttiger.driverassistant.domain.ports.TimeSource
import com.freighttiger.driverassistant.domain.ports.TripRepository
import com.freighttiger.driverassistant.domain.workflow.ActivityRecorder
import com.freighttiger.driverassistant.domain.workflow.PromptRequest
import com.freighttiger.driverassistant.domain.workflow.PromptScheduler
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import kotlin.random.Random

data class SyncReport(
    val attempted: Int = 0,
    val acknowledged: Int = 0,
    val retryScheduled: Int = 0,
    val failedPermanently: Int = 0,
    val exhausted: Int = 0,
    val cancelledByConflict: Int = 0,
    val offline: Boolean = false,
    val authRequired: Boolean = false,
    /** Earliest time a scheduled retry becomes due, for the platform scheduler. */
    val nextRetryAt: Instant? = null,
)

/**
 * Delivers the durable outbox to the backend.
 *
 * - An event is "delivered" only when the server acknowledges it (ACKNOWLEDGED), never on enqueue.
 * - Retryable failures use bounded exponential backoff; after [BackoffPolicy.maxAttempts] the event
 *   becomes FAILED_MAX_RETRIES and the driver is offered support.
 * - Validation errors are permanent and, for consent, move the record to VALIDATION_FAILED.
 * - Events for cancelled trips are not sent (CANCELLED_CONFLICT).
 * - Consent responses keep their original capture time; if the request expired while the device was
 *   offline the backend decides (EXPIRED_NEEDS_REVIEW) — tracking is never activated locally.
 */
class OutboxSyncEngine(
    private val outbox: OutboxRepository,
    private val backend: AssistantBackend,
    private val connectivity: ConnectivityMonitor,
    private val trips: TripRepository,
    private val consents: ConsentRepository,
    private val scheduler: PromptScheduler,
    private val activity: ActivityRecorder,
    private val clock: TimeSource,
    private val backoff: BackoffPolicy = BackoffPolicy(),
    private val random: Random = Random.Default,
    private val batchSize: Int = 25,
) {
    private val mutex = Mutex()

    suspend fun syncDue(): SyncReport = mutex.withLock {
        var report = SyncReport()
        if (!connectivity.isOnline.value) return@withLock report.copy(offline = true, nextRetryAt = earliestRetry())

        val due = outbox.due(clock.now(), batchSize)
        for (event in due) {
            if (!connectivity.isOnline.value) {
                report = report.copy(offline = true)
                break
            }
            if (conflicts(event)) {
                report = report.copy(cancelledByConflict = report.cancelledByConflict + 1)
                continue
            }
            report = report.copy(attempted = report.attempted + 1)
            val inFlight = event.copy(status = OutboxStatus.IN_FLIGHT, attempts = event.attempts + 1)
            outbox.update(inFlight)
            markConsentSubmitted(inFlight)

            when (val result = backend.submit(inFlight)) {
                is BackendResult.Success -> {
                    onAcknowledged(inFlight, result.value)
                    report = report.copy(acknowledged = report.acknowledged + 1)
                }
                is BackendResult.Failure -> when {
                    result.error is BackendError.Unauthorized -> {
                        // Keep the event; it is retried after the driver re-authenticates.
                        outbox.update(inFlight.copy(status = OutboxStatus.PENDING, attempts = event.attempts, lastErrorCode = result.error.code))
                        report = report.copy(authRequired = true)
                        break
                    }
                    result.error.retryable -> {
                        if (inFlight.attempts >= backoff.maxAttempts) {
                            outbox.update(inFlight.copy(status = OutboxStatus.FAILED_MAX_RETRIES, lastErrorCode = result.error.code, nextAttemptAt = null))
                            activity.record(ActivityKind.SYNC_FAILURE, "${event.payload.eventType} FAILED_MAX_RETRIES ${result.error.code}", event.tripId)
                            report = report.copy(exhausted = report.exhausted + 1)
                        } else {
                            val delay = (result.error as? BackendError.RateLimited)?.retryAfterSeconds
                                ?.let { java.time.Duration.ofSeconds(it) }
                                ?: backoff.delayFor(inFlight.attempts, random)
                            outbox.update(
                                inFlight.copy(
                                    status = OutboxStatus.RETRY_SCHEDULED,
                                    lastErrorCode = result.error.code,
                                    nextAttemptAt = clock.now().plus(delay),
                                ),
                            )
                            report = report.copy(retryScheduled = report.retryScheduled + 1)
                        }
                    }
                    else -> {
                        onPermanentFailure(inFlight, result.error)
                        report = report.copy(failedPermanently = report.failedPermanently + 1)
                    }
                }
            }
        }
        report.copy(nextRetryAt = earliestRetry())
    }

    /** Driver-initiated "retry now" for events that exhausted their retry budget. */
    suspend fun retryFailed(): Int = mutex.withLock {
        val failed = outbox.all().filter { it.status == OutboxStatus.FAILED_MAX_RETRIES }
        failed.forEach { outbox.update(it.copy(status = OutboxStatus.PENDING, attempts = 0, nextAttemptAt = null)) }
        failed.size
    }

    private suspend fun conflicts(event: OutboundEvent): Boolean {
        val tripId = event.tripId ?: return false
        val trip = trips.get(tripId) ?: return false
        if (trip.status == TripStatus.CANCELLED && event.payload.eventType != OutboundEventType.SUPPORT_REQUESTED) {
            outbox.update(event.copy(status = OutboxStatus.CANCELLED_CONFLICT, lastErrorCode = "TRIP_CANCELLED"))
            activity.record(ActivityKind.CONFLICT, "${event.payload.eventType} CANCELLED_TRIP_CANCELLED", tripId)
            return true
        }
        return false
    }

    private suspend fun markConsentSubmitted(event: OutboundEvent) {
        val p = event.payload as? OutboundPayload.ConsentResponse ?: return
        val record = consents.get(p.consentRequestId) ?: return
        val t = ConsentStateMachine.apply(record, ConsentEvent.Submitted, clock.now())
        if (t is ConsentTransition.Applied && t.changed) consents.upsert(t.record)
    }

    private suspend fun onAcknowledged(event: OutboundEvent, ack: SubmissionAck) {
        outbox.update(
            event.copy(
                status = OutboxStatus.ACKNOWLEDGED,
                acknowledgedAt = ack.receivedAt,
                serverReference = ack.serverReference,
                lastErrorCode = if (ack.status == AckStatus.DUPLICATE) "DUPLICATE_ACKNOWLEDGED" else null,
                nextAttemptAt = null,
                callbackScheduled = ack.callbackScheduled,
            ),
        )
        activity.record(ActivityKind.SYNC_SUCCESS, "${event.payload.eventType} ${ack.status}", event.tripId, backend.isSimulated)

        val p = event.payload as? OutboundPayload.ConsentResponse ?: return
        val record = consents.get(p.consentRequestId) ?: return
        when (val t = ConsentStateMachine.apply(record, ConsentEvent.BackendAccepted(ack.consentValidation), clock.now())) {
            is ConsentTransition.Applied -> if (t.changed) {
                consents.upsert(t.record)
                if (t.record.state != record.state) {
                    activity.record(ActivityKind.CONSENT_UPDATE, "${record.consentRequestId} ${t.record.state}", record.tripId, backend.isSimulated)
                    informDriver(t.record.tripId, t.record.consentRequestId, t.record.state)
                }
            }
            is ConsentTransition.Rejected -> activity.record(ActivityKind.CONFLICT, "CONSENT_ACK ${t.reasonCode}", record.tripId)
        }
    }

    private suspend fun onPermanentFailure(event: OutboundEvent, error: BackendError) {
        outbox.update(event.copy(status = OutboxStatus.FAILED_PERMANENT, lastErrorCode = error.code, nextAttemptAt = null))
        activity.record(ActivityKind.SYNC_FAILURE, "${event.payload.eventType} REJECTED ${error.code}", event.tripId, backend.isSimulated)
        val p = event.payload as? OutboundPayload.ConsentResponse ?: return
        val record = consents.get(p.consentRequestId) ?: return
        val t = ConsentStateMachine.apply(record, ConsentEvent.BackendRejected(error.code), clock.now())
        if (t is ConsentTransition.Applied && t.changed) {
            consents.upsert(t.record)
            if (t.record.state != record.state) {
                activity.record(ActivityKind.CONSENT_UPDATE, "${record.consentRequestId} ${t.record.state}", record.tripId, backend.isSimulated)
                informDriver(t.record.tripId, t.record.consentRequestId, t.record.state)
            }
        }
    }

    private suspend fun informDriver(tripId: String, consentRequestId: String, state: ConsentState) {
        val detail = when (state) {
            ConsentState.VALIDATION_FAILED -> "FAILED"
            ConsentState.EXPIRED -> "EXPIRED"
            else -> return // GRANTED is announced when the backend confirms tracking is ACTIVE.
        }
        scheduler.schedule(PromptRequest(tripId, PromptType.TRACKING_RESULT, consentRequestId, detail = detail, simulated = backend.isSimulated, bypassCooldown = true))
    }

    private suspend fun earliestRetry(): Instant? =
        outbox.all().filter { it.status == OutboxStatus.RETRY_SCHEDULED }.mapNotNull { it.nextAttemptAt }.minOrNull()
}
