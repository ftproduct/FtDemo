package com.freighttiger.driverassistant.domain.workflow

import com.freighttiger.driverassistant.core.model.ActivityKind
import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.ConsentDecision
import com.freighttiger.driverassistant.core.model.ConsentRecord
import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.core.model.DriverReportedArrival
import com.freighttiger.driverassistant.core.model.DriverReportedEta
import com.freighttiger.driverassistant.core.model.InteractionKind
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.OutboundPayload
import com.freighttiger.driverassistant.core.model.PromptType
import com.freighttiger.driverassistant.core.model.SupportReason
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.domain.consent.ConsentEvent
import com.freighttiger.driverassistant.domain.consent.ConsentStateMachine
import com.freighttiger.driverassistant.domain.consent.ConsentTransition
import com.freighttiger.driverassistant.domain.conversation.DialogueOutcome
import com.freighttiger.driverassistant.domain.ports.ConsentRepository
import com.freighttiger.driverassistant.domain.ports.IdGenerator
import com.freighttiger.driverassistant.domain.ports.OutboxRepository
import com.freighttiger.driverassistant.domain.ports.SessionRepository
import com.freighttiger.driverassistant.domain.ports.TimeSource
import com.freighttiger.driverassistant.domain.ports.TripRepository
import com.freighttiger.driverassistant.domain.voice.NextAction
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant

sealed interface CoordinatorResult {
    /** Captured locally and queued for delivery. NOT yet confirmed by the backend. */
    data class Queued(val event: OutboundEvent) : CoordinatorResult
    data object NothingToSend : CoordinatorResult
    data class Refused(val reasonCode: String) : CoordinatorResult
}

/** Called after something was queued so the platform can attempt delivery promptly. */
fun interface SyncTrigger {
    fun requestSync()
}

/**
 * Applies driver answers (voice or tap) to local trip state and the durable outbox.
 * It validates against current trip/consent state; the backend remains authoritative.
 * It never marks tracking as active — only backend confirmations do (see TripEventProcessor).
 */
class TripCoordinator(
    private val sessions: SessionRepository,
    private val trips: TripRepository,
    private val consents: ConsentRepository,
    private val outbox: OutboxRepository,
    private val scheduler: PromptScheduler,
    private val activity: ActivityRecorder,
    private val ids: IdGenerator,
    private val clock: TimeSource,
    private val syncTrigger: SyncTrigger = SyncTrigger { },
) {
    private val mutex = Mutex()

    suspend fun applyOutcome(prompt: AssistantPrompt, outcome: DialogueOutcome): CoordinatorResult {
        val result = when (outcome) {
            is DialogueOutcome.Consent -> {
                val requestId = prompt.consentRequestId ?: return CoordinatorResult.Refused("MISSING_CONSENT_REQUEST")
                submitConsentDecision(requestId, outcome.decision, outcome.method, outcome.transcript)
            }
            is DialogueOutcome.Eta -> reportEta(prompt.tripId, outcome.minutes, outcome.approximate, outcome.method)
            is DialogueOutcome.Arrival -> {
                val r = confirmArrival(prompt.tripId, outcome.arrived, outcome.method)
                if (!outcome.arrived && outcome.etaMinutes != null && r is CoordinatorResult.Queued) {
                    reportEta(prompt.tripId, outcome.etaMinutes, approximate = true, method = outcome.method)
                }
                r
            }
            is DialogueOutcome.Loading -> reportLoadingStatus(prompt.tripId, outcome.status, outcome.method)
            DialogueOutcome.SupportRequested -> requestSupport(prompt.tripId, SupportReason.DRIVER_REQUESTED)
            DialogueOutcome.Acknowledged -> CoordinatorResult.NothingToSend
        }
        // A support request leaves the original question open for later.
        if (outcome is DialogueOutcome.SupportRequested) {
            scheduler.defer(prompt.promptId)
        } else {
            scheduler.complete(prompt.promptId)
        }
        return result
    }

    suspend fun submitConsentDecision(
        consentRequestId: String,
        decision: ConsentDecision,
        method: CaptureMethod,
        transcript: String?,
        capturedAt: Instant = clock.now(),
    ): CoordinatorResult = mutex.withLock {
        val record = consents.get(consentRequestId) ?: return@withLock CoordinatorResult.Refused("UNKNOWN_CONSENT_REQUEST")
        val trip = trips.get(record.tripId) ?: return@withLock CoordinatorResult.Refused("UNKNOWN_TRIP")
        val session = sessions.current() ?: return@withLock CoordinatorResult.Refused("NO_ACTIVE_SESSION")
        if (record.driverId != session.profile.driverId || trip.driverId != session.profile.driverId) {
            return@withLock CoordinatorResult.Refused("DRIVER_MISMATCH")
        }
        val now = clock.now()
        if (trip.status.isTerminal) {
            applyConsent(record, ConsentEvent.TripEnded("TRIP_${trip.status}"), now)
            return@withLock CoordinatorResult.Refused("TRIP_NOT_ACTIVE")
        }
        if (record.isExpiredAt(now) || record.isExpiredAt(capturedAt)) {
            applyConsent(record, ConsentEvent.Expired(), now)
            scheduler.cancelForConsentRequest(consentRequestId)
            activity.record(ActivityKind.CONSENT_UPDATE, "$consentRequestId EXPIRED", trip.tripId, record.simulated)
            return@withLock CoordinatorResult.Refused("REQUEST_EXPIRED")
        }
        // Make sure the record reflects that the question was asked.
        val asked = when (val t = ConsentStateMachine.apply(record, ConsentEvent.PromptDelivered, now)) {
            is ConsentTransition.Applied -> t.record
            is ConsentTransition.Rejected -> record
        }
        when (val t = ConsentStateMachine.apply(asked, ConsentEvent.DriverDecided(decision, method, capturedAt, transcript), now)) {
            is ConsentTransition.Rejected -> CoordinatorResult.Refused(t.reasonCode)
            is ConsentTransition.Applied -> {
                consents.upsert(t.record)
                val responseNumber = 1
                val event = newEvent(
                    tripId = trip.tripId,
                    driverId = session.profile.driverId,
                    idempotencyKey = "$consentRequestId-response-$responseNumber",
                    payload = OutboundPayload.ConsentResponse(
                        consentRequestId = consentRequestId,
                        purpose = record.purpose,
                        decision = decision,
                        captureMethod = method,
                        language = record.language,
                        capturedAt = capturedAt,
                        transcript = transcript,
                    ),
                )
                enqueue(event)
                activity.record(ActivityKind.DRIVER_RESPONSE, "CONSENT $decision via $method", trip.tripId, record.simulated)
                activity.record(ActivityKind.CONSENT_UPDATE, "$consentRequestId ${t.record.state}", trip.tripId, record.simulated)
                CoordinatorResult.Queued(event)
            }
        }
    }

    suspend fun withdrawConsent(tripId: String, method: CaptureMethod = CaptureMethod.TAP): CoordinatorResult = mutex.withLock {
        val session = sessions.current() ?: return@withLock CoordinatorResult.Refused("NO_ACTIVE_SESSION")
        val trip = trips.get(tripId) ?: return@withLock CoordinatorResult.Refused("UNKNOWN_TRIP")
        val record = consents.forTrip(tripId).firstOrNull {
            it.state == ConsentState.GRANTED || it.state == ConsentState.GRANTED_PENDING_VALIDATION
        } ?: return@withLock CoordinatorResult.Refused("NO_GRANTED_CONSENT")
        val now = clock.now()
        when (val t = ConsentStateMachine.apply(record, ConsentEvent.Withdraw, now)) {
            is ConsentTransition.Rejected -> CoordinatorResult.Refused(t.reasonCode)
            is ConsentTransition.Applied -> {
                consents.upsert(t.record)
                if (trip.trackingStatus == TrackingStatus.ACTIVE || trip.trackingStatus == TrackingStatus.PENDING_ACTIVATION) {
                    // Backend confirms the stop with TRACKING_STATUS_UPDATED(STOPPED).
                    trips.upsert(trip.copy(trackingStatus = TrackingStatus.STOP_REQUESTED, updatedAt = now))
                }
                val event = newEvent(
                    tripId, session.profile.driverId, "${record.consentRequestId}-withdrawal-1",
                    OutboundPayload.ConsentWithdrawn(record.consentRequestId, record.purpose, method, record.language, now),
                )
                enqueue(event)
                activity.record(ActivityKind.CONSENT_UPDATE, "${record.consentRequestId} WITHDRAWN", tripId, record.simulated)
                CoordinatorResult.Queued(event)
            }
        }
    }

    suspend fun reportEta(tripId: String, minutes: Int, approximate: Boolean, method: CaptureMethod): CoordinatorResult = mutex.withLock {
        val (driverId, trip) = activeTripFor(tripId).getOrElse { return@withLock CoordinatorResult.Refused(it.message!!) }
        if (minutes <= 0 || minutes > 72 * 60) return@withLock CoordinatorResult.Refused("INVALID_ETA")
        if (trip.loadingStatus != LoadingStatus.NOT_REACHED) return@withLock CoordinatorResult.Refused("ALREADY_AT_LOADING_POINT")
        val now = clock.now()
        val eta = DriverReportedEta(minutes, approximate, now, now.plus(Duration.ofMinutes(minutes.toLong())))
        trips.upsert(trip.copy(driverReportedEta = eta, updatedAt = now))
        val event = newEvent(
            tripId, driverId, null,
            OutboundPayload.EtaReported(minutes, approximate, eta.estimatedArrivalAt, method, now),
        )
        enqueue(event)
        activity.record(ActivityKind.DRIVER_RESPONSE, "ETA ${minutes}min${if (approximate) " approx" else ""} via $method", tripId, trip.simulated)
        CoordinatorResult.Queued(event)
    }

    suspend fun confirmArrival(tripId: String, arrived: Boolean, method: CaptureMethod): CoordinatorResult = mutex.withLock {
        val (driverId, trip) = activeTripFor(tripId).getOrElse { return@withLock CoordinatorResult.Refused(it.message!!) }
        val now = clock.now()
        if (arrived && trip.driverReportedArrival?.arrived == true) return@withLock CoordinatorResult.Refused("ARRIVAL_ALREADY_REPORTED")
        val loading = if (arrived && trip.loadingStatus.canTransitionTo(LoadingStatus.REACHED_LOADING_POINT)) {
            LoadingStatus.REACHED_LOADING_POINT
        } else {
            trip.loadingStatus
        }
        trips.upsert(trip.copy(driverReportedArrival = DriverReportedArrival(arrived, now), loadingStatus = loading, updatedAt = now))
        val event = newEvent(tripId, driverId, null, OutboundPayload.ArrivalReported(arrived, method, now))
        enqueue(event)
        activity.record(ActivityKind.DRIVER_RESPONSE, "ARRIVAL ${if (arrived) "YES" else "NO"} via $method", tripId, trip.simulated)
        if (arrived) scheduler.cancelForTrip(tripId, setOf(PromptType.ETA, PromptType.ARRIVAL))
        CoordinatorResult.Queued(event)
    }

    suspend fun reportLoadingStatus(tripId: String, status: LoadingStatus, method: CaptureMethod): CoordinatorResult = mutex.withLock {
        val (driverId, trip) = activeTripFor(tripId).getOrElse { return@withLock CoordinatorResult.Refused(it.message!!) }
        if (trip.loadingStatus == status) return@withLock CoordinatorResult.Refused("STATUS_UNCHANGED")
        if (!trip.loadingStatus.canTransitionTo(status)) return@withLock CoordinatorResult.Refused("INVALID_LOADING_TRANSITION")
        val now = clock.now()
        val arrival = if (status == LoadingStatus.REACHED_LOADING_POINT) DriverReportedArrival(true, now) else trip.driverReportedArrival
        trips.upsert(trip.copy(loadingStatus = status, driverReportedArrival = arrival, updatedAt = now))
        val event = newEvent(tripId, driverId, null, OutboundPayload.LoadingStatusReported(status, method, now))
        enqueue(event)
        activity.record(ActivityKind.DRIVER_RESPONSE, "LOADING $status via $method", tripId, trip.simulated)
        CoordinatorResult.Queued(event)
    }

    suspend fun requestSupport(tripId: String?, reason: SupportReason): CoordinatorResult = mutex.withLock {
        val session = sessions.current() ?: return@withLock CoordinatorResult.Refused("NO_ACTIVE_SESSION")
        val now = clock.now()
        val event = newEvent(tripId, session.profile.driverId, null, OutboundPayload.SupportRequested(reason, now))
        enqueue(event)
        activity.record(ActivityKind.SUPPORT, "SUPPORT_REQUESTED $reason", tripId, trips.get(tripId ?: "")?.simulated ?: false)
        CoordinatorResult.Queued(event)
    }

    /** Driver chose "ask me later". Reported to the backend so coordinators know not to call. */
    suspend fun deferPrompt(prompt: AssistantPrompt, duration: Duration? = null): AssistantPrompt? {
        val updated = if (duration != null) scheduler.defer(prompt.promptId, duration) else scheduler.defer(prompt.promptId)
        activity.record(ActivityKind.PROMPT_DEFERRED, prompt.type.name, prompt.tripId, prompt.simulated)
        recordInteraction(prompt, InteractionKind.PROMPT_DEFERRED)
        return updated
    }

    suspend fun recordInteraction(prompt: AssistantPrompt, kind: InteractionKind) {
        val session = sessions.current() ?: return
        enqueue(newEvent(prompt.tripId, session.profile.driverId, null, OutboundPayload.AssistantInteraction(kind, prompt.type, clock.now())))
    }

    /** Marks a consent prompt as delivered (REQUESTED → AWAITING_RESPONSE). */
    suspend fun onConsentPromptDelivered(consentRequestId: String) = mutex.withLock {
        val record = consents.get(consentRequestId) ?: return@withLock
        applyConsent(record, ConsentEvent.PromptDelivered, clock.now())
    }

    suspend fun nextActionFor(trip: Trip): NextAction {
        if (trip.status.isTerminal) return NextAction.NONE
        val pendingConsent = consents.forTrip(trip.tripId).any { it.state.acceptsResponse }
        return when {
            pendingConsent -> NextAction.GIVE_TRACKING_CONSENT
            trip.loadingStatus == LoadingStatus.NOT_REACHED && trip.driverReportedEta == null -> NextAction.SHARE_ETA
            trip.loadingStatus == LoadingStatus.NOT_REACHED -> NextAction.REACH_LOADING_POINT
            trip.loadingStatus != LoadingStatus.LOADING_COMPLETED -> NextAction.UPDATE_LOADING_STATUS
            else -> NextAction.NONE
        }
    }

    private suspend fun applyConsent(record: ConsentRecord, event: ConsentEvent, now: Instant): ConsentRecord {
        val t = ConsentStateMachine.apply(record, event, now)
        return if (t is ConsentTransition.Applied) {
            if (t.changed) consents.upsert(t.record)
            t.record
        } else {
            record
        }
    }

    private suspend fun activeTripFor(tripId: String): Result<Pair<String, Trip>> {
        val session = sessions.current() ?: return Result.failure(IllegalStateException("NO_ACTIVE_SESSION"))
        val trip = trips.get(tripId) ?: return Result.failure(IllegalStateException("UNKNOWN_TRIP"))
        if (trip.driverId != session.profile.driverId) return Result.failure(IllegalStateException("DRIVER_MISMATCH"))
        if (trip.status.isTerminal) return Result.failure(IllegalStateException("TRIP_NOT_ACTIVE"))
        return Result.success(session.profile.driverId to trip)
    }

    private fun newEvent(tripId: String?, driverId: String, idempotencyKey: String?, payload: OutboundPayload): OutboundEvent {
        val eventId = ids.newId()
        return OutboundEvent(
            eventId = eventId,
            idempotencyKey = idempotencyKey ?: "${payload.eventType.name.lowercase()}-$eventId",
            driverId = driverId,
            tripId = tripId,
            payload = payload,
            createdAt = clock.now(),
        )
    }

    private suspend fun enqueue(event: OutboundEvent) {
        if (outbox.findByIdempotencyKey(event.idempotencyKey) != null) return
        outbox.enqueue(event)
        syncTrigger.requestSync()
    }
}
