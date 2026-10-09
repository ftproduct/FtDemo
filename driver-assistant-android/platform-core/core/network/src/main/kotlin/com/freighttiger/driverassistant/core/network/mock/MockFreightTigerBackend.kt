package com.freighttiger.driverassistant.core.network.mock

import com.freighttiger.driverassistant.core.model.ConsentDecision
import com.freighttiger.driverassistant.core.model.ConsentPurpose
import com.freighttiger.driverassistant.core.model.ConsentValidationStatus
import com.freighttiger.driverassistant.core.model.DriverProfile
import com.freighttiger.driverassistant.core.model.EventSource
import com.freighttiger.driverassistant.core.model.InboundPayload
import com.freighttiger.driverassistant.core.model.InboundTripEvent
import com.freighttiger.driverassistant.core.model.LoadingPoint
import com.freighttiger.driverassistant.core.model.Milestone
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.OutboundPayload
import com.freighttiger.driverassistant.core.model.Place
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.core.model.TripEventType
import com.freighttiger.driverassistant.core.model.TripStatus
import com.freighttiger.driverassistant.core.network.ApiConfig
import com.freighttiger.driverassistant.domain.backend.AckStatus
import com.freighttiger.driverassistant.domain.backend.AssistantBackend
import com.freighttiger.driverassistant.domain.backend.AuthGateway
import com.freighttiger.driverassistant.domain.backend.AuthResult
import com.freighttiger.driverassistant.domain.backend.BackendError
import com.freighttiger.driverassistant.domain.backend.BackendResult
import com.freighttiger.driverassistant.domain.backend.ConsentSnapshot
import com.freighttiger.driverassistant.domain.backend.OtpChallenge
import com.freighttiger.driverassistant.domain.backend.SubmissionAck
import com.freighttiger.driverassistant.domain.backend.TripStateSnapshot
import com.freighttiger.driverassistant.domain.ports.IdGenerator
import com.freighttiger.driverassistant.domain.ports.TimeSource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant

/** How the simulated backend answers consent validation. */
enum class ConsentValidationMode { MANUAL, AUTO_APPROVE, AUTO_REJECT }

data class SimulationControls(
    /** False simulates the Freight Tiger server being unreachable (network errors → retries). */
    val serverReachable: Boolean = true,
    val consentValidationMode: ConsentValidationMode = ConsentValidationMode.MANUAL,
    /** Next N submissions fail with HTTP 503 (to demonstrate backoff). */
    val failNextSubmissions: Int = 0,
)

data class MockSubmission(
    val eventType: String,
    val idempotencyKey: String,
    val receivedAt: Instant,
    val outcome: String,
)

/**
 * SIMULATED Freight Tiger backend for demo mode and tests.
 *
 * It implements the same ports as the HTTP client, validates requests the way the real backend is
 * expected to (driver/trip association, expiry, cancelled trips, idempotency), and emits trip
 * events on [pushEvents] in place of FCM. Everything it produces is flagged simulated and must be
 * labelled as such in the UI. It is never used when demo mode is off.
 */
class MockFreightTigerBackend(
    private val clock: TimeSource,
    private val ids: IdGenerator,
) : AssistantBackend, AuthGateway {

    override val isSimulated: Boolean = true

    private val mutex = Mutex()
    private val trips = mutableMapOf<String, Trip>()
    private val consentRequests = mutableMapOf<String, MockConsentRequest>()
    private val acks = mutableMapOf<String, SubmissionAck>()
    private var lastEmitted: InboundTripEvent? = null

    private val _controls = MutableStateFlow(SimulationControls())
    val controls: StateFlow<SimulationControls> = _controls.asStateFlow()

    private val _submissions = MutableStateFlow<List<MockSubmission>>(emptyList())
    val submissions: StateFlow<List<MockSubmission>> = _submissions.asStateFlow()

    private val _pushEvents = MutableSharedFlow<InboundTripEvent>(extraBufferCapacity = 64)
    /** Simulated push channel (stands in for FCM in demo mode). */
    val pushEvents: SharedFlow<InboundTripEvent> = _pushEvents.asSharedFlow()

    private val _emitted = MutableStateFlow<List<InboundTripEvent>>(emptyList())
    val emitted: StateFlow<List<InboundTripEvent>> = _emitted.asStateFlow()

    private data class MockConsentRequest(
        val id: String,
        val tripId: String,
        val driverId: String,
        val purpose: ConsentPurpose,
        val expiresAt: Instant,
        var validation: ConsentValidationStatus? = null,
        var granted: Boolean = false,
    )

    fun updateControls(transform: (SimulationControls) -> SimulationControls) = _controls.update(transform)

    // ------------------------------------------------------------------ demo triggers

    suspend fun simulateTripAssigned(driverId: String = DEMO_DRIVER_ID): InboundTripEvent {
        val now = clock.now()
        val tripId = "SIM-TRIP-" + ids.newId().take(8).uppercase()
        val trip = Trip(
            tripId = tripId,
            driverId = driverId,
            vehicleRegistration = "MH12 AB 1234",
            origin = Place("Chakan MIDC", "पुणे"),
            destination = Place("Narela Industrial Area", "दिल्ली"),
            loadingPoint = LoadingPoint("चाकण वेयरहाउस गेट 4", "Plot 12, Chakan MIDC Phase II", "चाकण, पुणे", now.plus(Duration.ofHours(3))),
            status = TripStatus.ASSIGNED,
            assignedAt = now,
            updatedAt = now,
            simulated = true,
        )
        mutex.withLock { trips[tripId] = trip }
        return emit(TripEventType.TRIP_ASSIGNED, driverId, tripId, InboundPayload.TripAssigned(trip))
    }

    suspend fun simulateConsentRequest(tripId: String, validFor: Duration = Duration.ofMinutes(15)): InboundTripEvent? {
        val trip = mutex.withLock { trips[tripId] } ?: return null
        val id = "SIM-CONSENT-" + ids.newId().take(8).uppercase()
        val expiresAt = clock.now().plus(validFor)
        mutex.withLock {
            consentRequests[id] = MockConsentRequest(id, tripId, trip.driverId, ConsentPurpose.SIM_BASED_TRIP_TRACKING, expiresAt)
        }
        return emit(
            TripEventType.CONSENT_REQUESTED, trip.driverId, tripId,
            InboundPayload.ConsentRequested(id, ConsentPurpose.SIM_BASED_TRIP_TRACKING, expiresAt),
        )
    }

    suspend fun simulateMilestone(tripId: String, milestone: Milestone): InboundTripEvent? {
        val trip = mutex.withLock { trips[tripId] } ?: return null
        return emit(TripEventType.MILESTONE_UPDATE_REQUESTED, trip.driverId, tripId, InboundPayload.MilestoneUpdateRequested(milestone))
    }

    /** Manual-mode completion of the backend consent checks (e.g. SIM operator confirmation). */
    suspend fun simulateActivationResult(consentRequestId: String, success: Boolean): InboundTripEvent? {
        val request = mutex.withLock { consentRequests[consentRequestId] } ?: return null
        val trip = mutex.withLock { trips[request.tripId] } ?: return null
        if (!request.granted) return null // Never activate without a granted response.
        if (success && trip.status.isTerminal) return null // Cancelled trips never get activated.
        return emitValidation(request, trip, success)
    }

    suspend fun simulateTripCancelled(tripId: String): InboundTripEvent? = endTrip(tripId, TripStatus.CANCELLED)

    suspend fun simulateTripCompleted(tripId: String): InboundTripEvent? = endTrip(tripId, TripStatus.COMPLETED)

    /** Re-emits the last event with the SAME event id, to demonstrate duplicate suppression. */
    suspend fun resendLastEvent(): InboundTripEvent? {
        val last = lastEmitted ?: return null
        _pushEvents.emit(last)
        _emitted.update { it + last }
        return last
    }

    // ------------------------------------------------------------------ AssistantBackend

    override suspend fun submit(event: OutboundEvent): BackendResult<SubmissionAck> {
        val controls = _controls.value
        if (!controls.serverReachable) return BackendResult.Failure(BackendError.Network("SIMULATED_UNREACHABLE"))
        if (controls.failNextSubmissions > 0) {
            _controls.update { it.copy(failNextSubmissions = it.failNextSubmissions - 1) }
            record(event, "HTTP_503")
            return BackendResult.Failure(BackendError.Server(503))
        }
        val toEmit = mutableListOf<suspend () -> Unit>()
        val result = mutex.withLock {
            acks[event.idempotencyKey]?.let { previous ->
                record(event, "DUPLICATE")
                return@withLock BackendResult.Success(previous.copy(status = AckStatus.DUPLICATE))
            }
            val outcome = validateAndApply(event, toEmit)
            if (outcome is BackendResult.Success) acks[event.idempotencyKey] = outcome.value
            record(event, if (outcome is BackendResult.Failure) "REJECTED" else "ACCEPTED")
            outcome
        }
        toEmit.forEach { it() }
        return result
    }

    override suspend fun fetchActiveTrip(driverId: String): BackendResult<Trip?> = mutex.withLock {
        BackendResult.Success(trips.values.filter { it.driverId == driverId && it.isActive }.maxByOrNull { it.assignedAt })
    }

    override suspend fun fetchTripState(tripId: String): BackendResult<TripStateSnapshot> = mutex.withLock {
        val trip = trips[tripId] ?: return@withLock BackendResult.Failure(BackendError.NotFound("UNKNOWN_TRIP"))
        val consent = consentRequests.values.filter { it.tripId == tripId && it.validation != null }
            .maxByOrNull { it.expiresAt }
        BackendResult.Success(
            TripStateSnapshot(
                tripId = tripId,
                status = trip.status,
                trackingStatus = trip.trackingStatus,
                loadingStatus = trip.loadingStatus,
                consent = consent?.let { ConsentSnapshot(it.id, it.validation!!) },
                asOf = clock.now(),
            ),
        )
    }

    // ------------------------------------------------------------------ AuthGateway (demo OTP)

    override suspend fun requestOtp(phoneNumber: String): BackendResult<OtpChallenge> {
        if (!_controls.value.serverReachable) return BackendResult.Failure(BackendError.Network("SIMULATED_UNREACHABLE"))
        return BackendResult.Success(
            OtpChallenge("SIM-OTP-" + ids.newId().take(8), clock.now().plus(Duration.ofMinutes(5)), simulated = true, demoHint = DEMO_OTP),
        )
    }

    override suspend fun verifyOtp(challengeId: String, phoneNumber: String, otp: String): BackendResult<AuthResult> {
        if (!_controls.value.serverReachable) return BackendResult.Failure(BackendError.Network("SIMULATED_UNREACHABLE"))
        if (otp != DEMO_OTP) return BackendResult.Failure(BackendError.Validation("INVALID_OTP"))
        val profile = DriverProfile(DEMO_DRIVER_ID, "डेमो ड्राइवर", "******" + phoneNumber.takeLast(4))
        return BackendResult.Success(
            AuthResult(profile, "demo-token-" + ids.newId(), clock.now().plus(Duration.ofDays(1)), simulated = true),
        )
    }

    // ------------------------------------------------------------------ internals

    private suspend fun validateAndApply(event: OutboundEvent, toEmit: MutableList<suspend () -> Unit>): BackendResult<SubmissionAck> {
        val now = clock.now()
        fun ack(validation: ConsentValidationStatus? = null, callback: Boolean? = null) = BackendResult.Success(
            SubmissionAck(event.eventId, AckStatus.ACCEPTED, now, "SIM-" + ids.newId().take(8).uppercase(), validation, callback),
        )
        fun invalid(code: String) = BackendResult.Failure(BackendError.Validation(code))

        val trip = event.tripId?.let { trips[it] }
        return when (val p = event.payload) {
            is OutboundPayload.ConsentResponse -> {
                val request = consentRequests[p.consentRequestId] ?: return invalid("UNKNOWN_CONSENT_REQUEST")
                if (request.driverId != event.driverId || request.tripId != event.tripId) return invalid("INVALID_ASSOCIATION")
                if (request.purpose != p.purpose) return invalid("PURPOSE_MISMATCH")
                if (trip == null || trip.status.isTerminal) return invalid("TRIP_NOT_ACTIVE")
                if (!p.capturedAt.isBefore(request.expiresAt)) return invalid("CONSENT_REQUEST_EXPIRED")
                if (p.decision == ConsentDecision.DECLINED) {
                    request.validation = null
                    return ack()
                }
                request.granted = true
                if (!now.isBefore(request.expiresAt)) {
                    // Captured in time but synced late: needs human review, no activation.
                    request.validation = ConsentValidationStatus.EXPIRED_NEEDS_REVIEW
                    return ack(ConsentValidationStatus.EXPIRED_NEEDS_REVIEW)
                }
                request.validation = ConsentValidationStatus.PENDING
                trips[trip.tripId] = trip.copy(trackingStatus = TrackingStatus.PENDING_ACTIVATION)
                when (_controls.value.consentValidationMode) {
                    ConsentValidationMode.AUTO_APPROVE -> toEmit += { emitValidation(request, trip, true) }
                    ConsentValidationMode.AUTO_REJECT -> toEmit += { emitValidation(request, trip, false) }
                    ConsentValidationMode.MANUAL -> Unit
                }
                ack(ConsentValidationStatus.PENDING)
            }
            is OutboundPayload.ConsentWithdrawn -> {
                val request = consentRequests[p.consentRequestId] ?: return invalid("UNKNOWN_CONSENT_REQUEST")
                if (request.driverId != event.driverId || request.tripId != event.tripId) return invalid("INVALID_ASSOCIATION")
                request.granted = false
                if (trip != null && trip.trackingStatus != TrackingStatus.NOT_STARTED) {
                    trips[trip.tripId] = trip.copy(trackingStatus = TrackingStatus.STOPPED)
                    toEmit += {
                        emit(
                            TripEventType.TRACKING_STATUS_UPDATED, trip.driverId, trip.tripId,
                            InboundPayload.TrackingStatusUpdated(TrackingStatus.STOPPED, null, null, "CONSENT_WITHDRAWN"),
                        )
                    }
                }
                ack()
            }
            is OutboundPayload.EtaReported, is OutboundPayload.ArrivalReported, is OutboundPayload.LoadingStatusReported -> {
                if (trip == null) return invalid("UNKNOWN_TRIP")
                if (trip.driverId != event.driverId) return invalid("INVALID_ASSOCIATION")
                if (trip.status.isTerminal) return invalid("TRIP_NOT_ACTIVE")
                if (p is OutboundPayload.LoadingStatusReported) {
                    if (!trip.loadingStatus.canTransitionTo(p.status)) return invalid("INVALID_LOADING_TRANSITION")
                    trips[trip.tripId] = trip.copy(loadingStatus = p.status)
                }
                if (p is OutboundPayload.ArrivalReported && p.arrived && trip.loadingStatus == com.freighttiger.driverassistant.core.model.LoadingStatus.NOT_REACHED) {
                    trips[trip.tripId] = trip.copy(loadingStatus = com.freighttiger.driverassistant.core.model.LoadingStatus.REACHED_LOADING_POINT)
                }
                ack()
            }
            // The simulated backend never schedules a real callback.
            is OutboundPayload.SupportRequested -> ack(callback = false)
            is OutboundPayload.AssistantInteraction -> ack()
        }
    }

    private suspend fun emitValidation(request: MockConsentRequest, trip: Trip, success: Boolean): InboundTripEvent {
        val status = if (success) TrackingStatus.ACTIVE else TrackingStatus.FAILED
        val validation = if (success) ConsentValidationStatus.VALIDATED else ConsentValidationStatus.REJECTED
        mutex.withLock {
            request.validation = validation
            trips[trip.tripId]?.let { trips[trip.tripId] = it.copy(trackingStatus = status) }
        }
        return emit(
            TripEventType.TRACKING_STATUS_UPDATED, trip.driverId, trip.tripId,
            InboundPayload.TrackingStatusUpdated(status, request.id, validation, if (success) null else "SIM_OPERATOR_CONSENT_NOT_CONFIRMED"),
        )
    }

    private suspend fun endTrip(tripId: String, status: TripStatus): InboundTripEvent? {
        val trip = mutex.withLock {
            val t = trips[tripId] ?: return@withLock null
            trips[tripId] = t.copy(status = status, trackingStatus = if (t.trackingStatus == TrackingStatus.NOT_STARTED) t.trackingStatus else TrackingStatus.STOPPED)
            t
        } ?: return null
        val type = if (status == TripStatus.CANCELLED) TripEventType.TRIP_CANCELLED else TripEventType.TRIP_COMPLETED
        val payload = if (status == TripStatus.CANCELLED) InboundPayload.TripCancelled("SIMULATED_CANCELLATION") else InboundPayload.TripCompleted
        return emit(type, trip.driverId, tripId, payload)
    }

    private suspend fun emit(type: TripEventType, driverId: String, tripId: String, payload: InboundPayload): InboundTripEvent {
        val event = InboundTripEvent(
            eventId = "SIM-EVT-" + ids.newId(),
            schemaVersion = ApiConfig.SCHEMA_VERSION,
            type = type,
            driverId = driverId,
            tripId = tripId,
            issuedAt = clock.now(),
            source = EventSource.SIMULATED,
            payload = payload,
        )
        lastEmitted = event
        _emitted.update { it + event }
        _pushEvents.emit(event)
        return event
    }

    private fun record(event: OutboundEvent, outcome: String) {
        _submissions.update { it + MockSubmission(event.payload.eventType.name, event.idempotencyKey, clock.now(), outcome) }
    }

    companion object {
        const val DEMO_DRIVER_ID = "DRV-DEMO-001"
        const val DEMO_OTP = "123456"
    }
}
