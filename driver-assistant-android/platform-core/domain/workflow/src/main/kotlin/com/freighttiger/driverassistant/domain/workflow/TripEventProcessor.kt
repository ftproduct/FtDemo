package com.freighttiger.driverassistant.domain.workflow

import com.freighttiger.driverassistant.core.model.ActivityKind
import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.ConsentRecord
import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.core.model.EventSource
import com.freighttiger.driverassistant.core.model.InboundEventRecord
import com.freighttiger.driverassistant.core.model.InboundOutcome
import com.freighttiger.driverassistant.core.model.InboundPayload
import com.freighttiger.driverassistant.core.model.InboundTripEvent
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.Milestone
import com.freighttiger.driverassistant.core.model.OutboundEventType
import com.freighttiger.driverassistant.core.model.OutboxStatus
import com.freighttiger.driverassistant.core.model.PromptType
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.core.model.TripStatus
import com.freighttiger.driverassistant.domain.backend.TripStateSnapshot
import com.freighttiger.driverassistant.domain.consent.ConsentEvent
import com.freighttiger.driverassistant.domain.consent.ConsentStateMachine
import com.freighttiger.driverassistant.domain.consent.ConsentTransition
import com.freighttiger.driverassistant.domain.ports.ConsentRepository
import com.freighttiger.driverassistant.domain.ports.InboundEventRepository
import com.freighttiger.driverassistant.domain.ports.OutboxRepository
import com.freighttiger.driverassistant.domain.ports.SessionRepository
import com.freighttiger.driverassistant.domain.ports.TimeSource
import com.freighttiger.driverassistant.domain.ports.TripRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface ProcessingResult {
    val eventId: String

    /** Event applied. [prompt] is set when a new prompt should be presented. */
    data class Applied(
        override val eventId: String,
        val prompt: AssistantPrompt?,
        val promptSuppressedReason: String? = null,
    ) : ProcessingResult

    data class Duplicate(override val eventId: String) : ProcessingResult
    data class Rejected(override val eventId: String, val reasonCode: String) : ProcessingResult
    data class Ignored(override val eventId: String, val reasonCode: String) : ProcessingResult
}

/**
 * Handles every inbound trip event (push, poll or simulation) through one path:
 * validate → de-duplicate → check driver/trip relevance → check validity → apply → schedule prompt.
 */
class TripEventProcessor(
    private val sessions: SessionRepository,
    private val trips: TripRepository,
    private val consents: ConsentRepository,
    private val inbound: InboundEventRepository,
    private val outbox: OutboxRepository,
    private val scheduler: PromptScheduler,
    private val activity: ActivityRecorder,
    private val clock: TimeSource,
    private val validator: EventValidator = EventValidator(),
) {
    private val mutex = Mutex()

    suspend fun process(event: InboundTripEvent): ProcessingResult = mutex.withLock {
        val simulated = event.source == EventSource.SIMULATED
        validator.validate(event)?.let { reason ->
            if (event.eventId.isNotBlank() && !inbound.exists(event.eventId)) record(event, InboundOutcome.REJECTED, reason)
            activity.record(ActivityKind.EVENT_REJECTED, "${event.type} $reason", event.tripId.ifBlank { null }, simulated)
            return@withLock ProcessingResult.Rejected(event.eventId, reason)
        }
        if (inbound.exists(event.eventId)) return@withLock ProcessingResult.Duplicate(event.eventId)

        val session = sessions.current()
        val relevance = when {
            session == null -> "NO_ACTIVE_SESSION"
            session.profile.driverId != event.driverId -> "DRIVER_MISMATCH"
            else -> null
        }
        if (relevance != null) {
            record(event, InboundOutcome.REJECTED, relevance)
            activity.record(ActivityKind.EVENT_REJECTED, "${event.type} $relevance", null, simulated)
            return@withLock ProcessingResult.Rejected(event.eventId, relevance)
        }
        val now = clock.now()
        if (event.expiresAt != null && !now.isBefore(event.expiresAt)) {
            record(event, InboundOutcome.IGNORED, "EVENT_EXPIRED")
            activity.record(ActivityKind.EVENT_REJECTED, "${event.type} EVENT_EXPIRED", event.tripId, simulated)
            return@withLock ProcessingResult.Ignored(event.eventId, "EVENT_EXPIRED")
        }

        val result = when (val payload = event.payload) {
            is InboundPayload.TripAssigned -> onTripAssigned(event, payload)
            is InboundPayload.ConsentRequested -> onConsentRequested(event, payload)
            is InboundPayload.TrackingStatusUpdated -> onTrackingUpdate(event, payload)
            is InboundPayload.MilestoneUpdateRequested -> onMilestone(event, payload)
            is InboundPayload.TripCancelled -> onTripEnded(event, TripStatus.CANCELLED, payload.reasonCode ?: "TRIP_CANCELLED")
            InboundPayload.TripCompleted -> onTripEnded(event, TripStatus.COMPLETED, "TRIP_COMPLETED")
        }
        val (outcome, reason) = when (result) {
            is ProcessingResult.Applied -> InboundOutcome.APPLIED to result.promptSuppressedReason
            is ProcessingResult.Duplicate -> InboundOutcome.DUPLICATE to null
            is ProcessingResult.Ignored -> InboundOutcome.IGNORED to result.reasonCode
            is ProcessingResult.Rejected -> InboundOutcome.REJECTED to result.reasonCode
        }
        record(event, outcome, reason)
        val kind = if (outcome == InboundOutcome.APPLIED) ActivityKind.EVENT_RECEIVED else ActivityKind.EVENT_REJECTED
        activity.record(kind, listOfNotNull(event.type.name, reason).joinToString(" "), event.tripId, simulated)
        result
    }

    /** Reconcile with an authoritative trip-state snapshot (used when push delivery is unavailable). */
    suspend fun applySnapshot(snapshot: TripStateSnapshot, simulated: Boolean): AssistantPrompt? = mutex.withLock {
        val trip = trips.get(snapshot.tripId) ?: return@withLock null
        if (snapshot.status.isTerminal && !trip.status.isTerminal) {
            return@withLock endTrip(trip, snapshot.status, "STATE_RECONCILED_${snapshot.status}", simulated)
        }
        val update = InboundPayload.TrackingStatusUpdated(
            trackingStatus = snapshot.trackingStatus,
            consentRequestId = snapshot.consent?.consentRequestId,
            consentValidation = snapshot.consent?.validation,
        )
        when (val r = applyTracking(trip, update, simulated)) {
            is TrackingApply.Ok -> r.prompt
            is TrackingApply.Refused -> null
        }
    }

    private suspend fun onTripAssigned(event: InboundTripEvent, p: InboundPayload.TripAssigned): ProcessingResult {
        val existing = trips.get(p.trip.tripId)
        if (existing != null && existing.status.isTerminal) {
            return ProcessingResult.Rejected(event.eventId, "TRIP_ALREADY_ENDED")
        }
        val now = clock.now()
        val merged = p.trip.copy(
            driverReportedEta = existing?.driverReportedEta ?: p.trip.driverReportedEta,
            driverReportedArrival = existing?.driverReportedArrival ?: p.trip.driverReportedArrival,
            trackingStatus = existing?.trackingStatus ?: p.trip.trackingStatus,
            updatedAt = now,
            simulated = p.trip.simulated || event.source == EventSource.SIMULATED,
        )
        trips.upsert(merged)
        return schedule(event, PromptRequest(merged.tripId, PromptType.TRIP_BRIEFING, simulated = merged.simulated))
    }

    private suspend fun onConsentRequested(event: InboundTripEvent, p: InboundPayload.ConsentRequested): ProcessingResult {
        val trip = trips.get(event.tripId) ?: return ProcessingResult.Rejected(event.eventId, "UNKNOWN_TRIP")
        if (trip.status.isTerminal) return ProcessingResult.Rejected(event.eventId, "TRIP_NOT_ACTIVE")
        if (trip.driverId != event.driverId) return ProcessingResult.Rejected(event.eventId, "TRIP_DRIVER_MISMATCH")
        if (consents.get(p.consentRequestId) != null) {
            return ProcessingResult.Ignored(event.eventId, "CONSENT_REQUEST_ALREADY_KNOWN")
        }
        val now = clock.now()
        if (!now.isBefore(p.expiresAt)) return ProcessingResult.Ignored(event.eventId, "CONSENT_REQUEST_EXPIRED")

        // A newer request for the same trip and purpose supersedes unanswered older ones.
        consents.forTrip(trip.tripId)
            .filter { it.purpose == p.purpose && it.state.acceptsResponse }
            .forEach { old ->
                (ConsentStateMachine.apply(old, ConsentEvent.Expired("SUPERSEDED"), now) as? ConsentTransition.Applied)
                    ?.let { consents.upsert(it.record) }
                scheduler.cancelForConsentRequest(old.consentRequestId)
            }

        val simulated = event.source == EventSource.SIMULATED
        consents.upsert(
            ConsentRecord(
                consentRequestId = p.consentRequestId,
                tripId = trip.tripId,
                driverId = event.driverId,
                purpose = p.purpose,
                requestedAt = event.issuedAt,
                expiresAt = p.expiresAt,
                state = ConsentState.REQUESTED,
                updatedAt = now,
                simulated = simulated,
            ),
        )
        activity.record(ActivityKind.CONSENT_UPDATE, "${p.consentRequestId} REQUESTED", trip.tripId, simulated)
        return schedule(
            event,
            PromptRequest(trip.tripId, PromptType.CONSENT, p.consentRequestId, p.expiresAt, simulated = simulated),
        )
    }

    private suspend fun onTrackingUpdate(event: InboundTripEvent, p: InboundPayload.TrackingStatusUpdated): ProcessingResult {
        val trip = trips.get(event.tripId) ?: return ProcessingResult.Rejected(event.eventId, "UNKNOWN_TRIP")
        return when (val r = applyTracking(trip, p, event.source == EventSource.SIMULATED)) {
            is TrackingApply.Refused -> ProcessingResult.Rejected(event.eventId, r.reasonCode)
            is TrackingApply.Ok -> ProcessingResult.Applied(event.eventId, r.prompt)
        }
    }

    private sealed interface TrackingApply {
        data class Ok(val prompt: AssistantPrompt?) : TrackingApply
        data class Refused(val reasonCode: String) : TrackingApply
    }

    private suspend fun applyTracking(trip: Trip, p: InboundPayload.TrackingStatusUpdated, simulated: Boolean): TrackingApply {
        val now = clock.now()
        if (trip.status.isTerminal && p.trackingStatus == TrackingStatus.ACTIVE) {
            activity.record(ActivityKind.CONFLICT, "ACTIVATION_FOR_ENDED_TRIP ${trip.status}", trip.tripId, simulated)
            return TrackingApply.Refused("TRIP_NOT_ACTIVE")
        }
        var consent: ConsentRecord? = null
        var consentChanged = false
        val requestId = p.consentRequestId
        val validation = p.consentValidation
        if (requestId != null) {
            consent = consents.get(requestId) ?: return TrackingApply.Refused("UNKNOWN_CONSENT_REQUEST")
            if (consent.tripId != trip.tripId) return TrackingApply.Refused("CONSENT_TRIP_MISMATCH")
            if (validation != null) {
                when (val t = ConsentStateMachine.apply(consent, ConsentEvent.BackendValidation(validation, p.reasonCode), now)) {
                    is ConsentTransition.Applied -> {
                        consent = t.record
                        consentChanged = t.changed
                        if (t.changed) {
                            consents.upsert(t.record)
                            activity.record(ActivityKind.CONSENT_UPDATE, "${t.record.consentRequestId} ${t.record.state}", trip.tripId, simulated)
                        }
                    }
                    is ConsentTransition.Rejected -> {
                        activity.record(ActivityKind.CONFLICT, "CONSENT_VALIDATION ${t.reasonCode}", trip.tripId, simulated)
                        return TrackingApply.Refused("CONSENT_CONFLICT")
                    }
                }
            }
        }
        if (p.trackingStatus == TrackingStatus.ACTIVE) {
            // Never show tracking as active unless a backend-validated consent exists for THIS trip.
            val validConsent = consent?.takeIf { it.state == ConsentState.GRANTED }
                ?: consents.forTrip(trip.tripId).firstOrNull { it.state == ConsentState.GRANTED }
            if (validConsent == null) {
                activity.record(ActivityKind.CONFLICT, "ACTIVATION_WITHOUT_VALIDATED_CONSENT", trip.tripId, simulated)
                return TrackingApply.Refused("CONSENT_NOT_VALIDATED")
            }
        }
        val trackingChanged = trip.trackingStatus != p.trackingStatus
        if (trackingChanged) {
            trips.upsert(trip.copy(trackingStatus = p.trackingStatus, updatedAt = now))
            activity.record(ActivityKind.TRACKING_UPDATE, "${trip.trackingStatus} -> ${p.trackingStatus}", trip.tripId, simulated)
        }
        val detail = when {
            trackingChanged && p.trackingStatus == TrackingStatus.ACTIVE -> "ACTIVE"
            trackingChanged && p.trackingStatus == TrackingStatus.FAILED -> "FAILED"
            trackingChanged && p.trackingStatus == TrackingStatus.STOPPED -> "STOPPED"
            consentChanged && consent?.state == ConsentState.VALIDATION_FAILED -> "FAILED"
            consentChanged && consent?.state == ConsentState.EXPIRED -> "EXPIRED"
            else -> null
        }
        val prompt = detail?.let {
            val decision = scheduler.schedule(
                PromptRequest(trip.tripId, PromptType.TRACKING_RESULT, consent?.consentRequestId, detail = it, simulated = simulated, bypassCooldown = true),
            )
            (decision as? ScheduleDecision.Scheduled)?.prompt
        }
        return TrackingApply.Ok(prompt)
    }

    private suspend fun onMilestone(event: InboundTripEvent, p: InboundPayload.MilestoneUpdateRequested): ProcessingResult {
        val trip = trips.get(event.tripId) ?: return ProcessingResult.Rejected(event.eventId, "UNKNOWN_TRIP")
        if (trip.status.isTerminal) return ProcessingResult.Rejected(event.eventId, "TRIP_NOT_ACTIVE")
        val type = when (p.milestone) {
            Milestone.ETA_TO_LOADING_POINT -> {
                if (trip.loadingStatus != LoadingStatus.NOT_REACHED || trip.driverReportedArrival?.arrived == true) {
                    return ProcessingResult.Ignored(event.eventId, "ALREADY_AT_LOADING_POINT")
                }
                PromptType.ETA
            }
            Milestone.ARRIVAL_AT_LOADING_POINT -> {
                if (trip.driverReportedArrival?.arrived == true || trip.verifiedArrival != null) {
                    return ProcessingResult.Ignored(event.eventId, "ARRIVAL_ALREADY_REPORTED")
                }
                PromptType.ARRIVAL
            }
            Milestone.LOADING_STATUS -> {
                if (trip.loadingStatus == LoadingStatus.LOADING_COMPLETED) {
                    return ProcessingResult.Ignored(event.eventId, "LOADING_ALREADY_COMPLETED")
                }
                PromptType.LOADING_STATUS
            }
        }
        return schedule(event, PromptRequest(trip.tripId, type, expiresAt = event.expiresAt, simulated = event.source == EventSource.SIMULATED))
    }

    private suspend fun onTripEnded(event: InboundTripEvent, status: TripStatus, reason: String): ProcessingResult {
        val trip = trips.get(event.tripId) ?: return ProcessingResult.Ignored(event.eventId, "UNKNOWN_TRIP")
        if (trip.status.isTerminal) return ProcessingResult.Ignored(event.eventId, "TRIP_ALREADY_ENDED")
        val prompt = endTrip(trip, status, reason, event.source == EventSource.SIMULATED)
        return ProcessingResult.Applied(event.eventId, prompt)
    }

    private suspend fun endTrip(trip: Trip, status: TripStatus, reason: String, simulated: Boolean): AssistantPrompt? {
        val now = clock.now()
        val tracking = when (trip.trackingStatus) {
            TrackingStatus.NOT_STARTED, TrackingStatus.FAILED -> trip.trackingStatus
            else -> TrackingStatus.STOPPED
        }
        trips.upsert(trip.copy(status = status, trackingStatus = tracking, updatedAt = now))
        activity.record(ActivityKind.TRIP_UPDATE, "${trip.status} -> $status", trip.tripId, simulated)

        consents.forTrip(trip.tripId).forEach { c ->
            val t = ConsentStateMachine.apply(c, ConsentEvent.TripEnded(reason), now)
            if (t is ConsentTransition.Applied && t.changed) consents.upsert(t.record)
        }
        scheduler.cancelForTrip(trip.tripId)

        if (status == TripStatus.CANCELLED) {
            // Conflict handling: queued updates for a cancelled trip are not sent.
            outbox.forTrip(trip.tripId)
                .filter { it.status.isOpen && it.payload.eventType != OutboundEventType.SUPPORT_REQUESTED }
                .forEach {
                    outbox.update(it.copy(status = OutboxStatus.CANCELLED_CONFLICT, lastErrorCode = "TRIP_CANCELLED"))
                    activity.record(ActivityKind.CONFLICT, "${it.payload.eventType} CANCELLED_TRIP_CANCELLED", trip.tripId, simulated)
                }
        }
        val type = if (status == TripStatus.CANCELLED) PromptType.TRIP_CANCELLED else PromptType.TRIP_COMPLETED
        val decision = scheduler.schedule(PromptRequest(trip.tripId, type, simulated = simulated, bypassCooldown = true))
        return (decision as? ScheduleDecision.Scheduled)?.prompt
    }

    private suspend fun schedule(event: InboundTripEvent, request: PromptRequest): ProcessingResult =
        when (val d = scheduler.schedule(request)) {
            is ScheduleDecision.Scheduled -> {
                activity.record(ActivityKind.PROMPT_SCHEDULED, d.prompt.type.name, request.tripId, request.simulated)
                ProcessingResult.Applied(event.eventId, d.prompt)
            }
            is ScheduleDecision.Suppressed -> {
                activity.record(ActivityKind.PROMPT_SUPPRESSED, "${request.type} ${d.reasonCode}", request.tripId, request.simulated)
                ProcessingResult.Applied(event.eventId, null, d.reasonCode)
            }
        }

    private suspend fun record(event: InboundTripEvent, outcome: InboundOutcome, reason: String?) {
        inbound.record(
            InboundEventRecord(
                eventId = event.eventId,
                type = event.type,
                tripId = event.tripId,
                driverId = event.driverId,
                receivedAt = clock.now(),
                source = event.source,
                outcome = outcome,
                reasonCode = reason,
            ),
        )
    }
}
