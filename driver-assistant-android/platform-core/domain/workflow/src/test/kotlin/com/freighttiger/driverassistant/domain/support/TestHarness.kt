package com.freighttiger.driverassistant.domain.support

import com.freighttiger.driverassistant.core.model.ConsentPurpose
import com.freighttiger.driverassistant.core.model.DriverProfile
import com.freighttiger.driverassistant.core.model.DriverSession
import com.freighttiger.driverassistant.core.model.EventSource
import com.freighttiger.driverassistant.core.model.InboundPayload
import com.freighttiger.driverassistant.core.model.InboundTripEvent
import com.freighttiger.driverassistant.core.model.LoadingPoint
import com.freighttiger.driverassistant.core.model.Milestone
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.Place
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.core.model.TripEventType
import com.freighttiger.driverassistant.core.model.TripStatus
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.core.model.ConsentValidationStatus
import com.freighttiger.driverassistant.domain.backend.AckStatus
import com.freighttiger.driverassistant.domain.backend.AssistantBackend
import com.freighttiger.driverassistant.domain.backend.BackendError
import com.freighttiger.driverassistant.domain.backend.BackendResult
import com.freighttiger.driverassistant.domain.backend.SubmissionAck
import com.freighttiger.driverassistant.domain.backend.TripStateSnapshot
import com.freighttiger.driverassistant.domain.inmemory.InMemoryActivityLog
import com.freighttiger.driverassistant.domain.inmemory.InMemoryConsentRepository
import com.freighttiger.driverassistant.domain.inmemory.InMemoryInboundEventRepository
import com.freighttiger.driverassistant.domain.inmemory.InMemoryOutboxRepository
import com.freighttiger.driverassistant.domain.inmemory.InMemoryPromptRepository
import com.freighttiger.driverassistant.domain.inmemory.InMemorySessionRepository
import com.freighttiger.driverassistant.domain.inmemory.InMemoryTripRepository
import com.freighttiger.driverassistant.domain.inmemory.ManualConnectivityMonitor
import com.freighttiger.driverassistant.domain.inmemory.MutableTimeSource
import com.freighttiger.driverassistant.domain.inmemory.SequentialIdGenerator
import com.freighttiger.driverassistant.domain.sync.BackoffPolicy
import com.freighttiger.driverassistant.domain.sync.OutboxSyncEngine
import com.freighttiger.driverassistant.domain.workflow.ActivityRecorder
import com.freighttiger.driverassistant.domain.workflow.PromptScheduler
import com.freighttiger.driverassistant.domain.workflow.TripCoordinator
import com.freighttiger.driverassistant.domain.workflow.TripEventProcessor
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

const val DRIVER = "driver-123"
const val TRIP = "trip-456"
val T0: Instant = Instant.parse("2026-10-09T12:00:00Z")

/** Scriptable backend fake: records submissions, answers from a queue or a default. */
class FakeBackend : AssistantBackend {
    override val isSimulated = false
    val submitted = mutableListOf<OutboundEvent>()
    val responses = ArrayDeque<BackendResult<SubmissionAck>>()
    var defaultValidation: ConsentValidationStatus? = ConsentValidationStatus.PENDING
    private val seenKeys = mutableSetOf<String>()

    override suspend fun submit(event: OutboundEvent): BackendResult<SubmissionAck> {
        submitted += event
        responses.removeFirstOrNull()?.let { return it }
        val duplicate = !seenKeys.add(event.idempotencyKey)
        val isConsent = event.payload is com.freighttiger.driverassistant.core.model.OutboundPayload.ConsentResponse
        return BackendResult.Success(
            SubmissionAck(
                event.eventId,
                if (duplicate) AckStatus.DUPLICATE else AckStatus.ACCEPTED,
                T0,
                "srv-${submitted.size}",
                if (isConsent) defaultValidation else null,
            ),
        )
    }

    override suspend fun fetchActiveTrip(driverId: String): BackendResult<Trip?> = BackendResult.Success(null)
    override suspend fun fetchTripState(tripId: String): BackendResult<TripStateSnapshot> =
        BackendResult.Failure(BackendError.NotFound())
    override suspend fun registerPushToken(driverId: String, token: String): BackendResult<Unit> = BackendResult.Success(Unit)
}

class TestHarness(online: Boolean = true) {
    val clock = MutableTimeSource(T0)
    val ids = SequentialIdGenerator()
    val sessions = InMemorySessionRepository(DriverSession(DriverProfile(DRIVER, "Test", "******3210"), T0, simulated = false))
    val trips = InMemoryTripRepository()
    val consents = InMemoryConsentRepository()
    val inbound = InMemoryInboundEventRepository()
    val outbox = InMemoryOutboxRepository()
    val prompts = InMemoryPromptRepository()
    val log = InMemoryActivityLog()
    val activity = ActivityRecorder(log, ids, clock)
    val connectivity = ManualConnectivityMonitor(online)
    val backend = FakeBackend()
    val scheduler = PromptScheduler(prompts, ids, clock)
    val processor = TripEventProcessor(sessions, trips, consents, inbound, outbox, scheduler, activity, clock)
    var syncRequests = 0
    val coordinator = TripCoordinator(sessions, trips, consents, outbox, scheduler, activity, ids, clock) { syncRequests++ }
    val sync = OutboxSyncEngine(
        outbox, backend, connectivity, trips, consents, scheduler, activity, clock,
        BackoffPolicy(jitter = 0.0, maxAttempts = 4), Random(1),
    )

    private var eventSeq = 0

    fun trip(id: String = TRIP, driver: String = DRIVER) = Trip(
        tripId = id,
        driverId = driver,
        vehicleRegistration = "MH12 AB 1234",
        origin = Place("Chakan", "Pune"),
        destination = Place("Narela", "Delhi"),
        loadingPoint = LoadingPoint("Gate 4", "Plot 12", "Chakan"),
        status = TripStatus.ASSIGNED,
        assignedAt = clock.now(),
        updatedAt = clock.now(),
    )

    fun event(type: TripEventType, payload: InboundPayload, tripId: String = TRIP, driver: String = DRIVER, eventId: String? = null, expiresAt: Instant? = null) =
        InboundTripEvent(
            eventId = eventId ?: "evt-${++eventSeq}",
            schemaVersion = "1.0",
            type = type,
            driverId = driver,
            tripId = tripId,
            issuedAt = clock.now(),
            expiresAt = expiresAt,
            source = EventSource.PUSH,
            payload = payload,
        )

    fun assigned(tripId: String = TRIP, driver: String = DRIVER) =
        event(TripEventType.TRIP_ASSIGNED, InboundPayload.TripAssigned(trip(tripId, driver)), tripId, driver)

    fun consentRequested(requestId: String = "consent-789", validFor: Duration = Duration.ofMinutes(15), tripId: String = TRIP) =
        event(
            TripEventType.CONSENT_REQUESTED,
            InboundPayload.ConsentRequested(requestId, ConsentPurpose.SIM_BASED_TRIP_TRACKING, clock.now().plus(validFor)),
            tripId,
        )

    fun tracking(status: TrackingStatus, requestId: String?, validation: ConsentValidationStatus?, tripId: String = TRIP) =
        event(TripEventType.TRACKING_STATUS_UPDATED, InboundPayload.TrackingStatusUpdated(status, requestId, validation), tripId)

    fun milestone(m: Milestone, tripId: String = TRIP) =
        event(TripEventType.MILESTONE_UPDATE_REQUESTED, InboundPayload.MilestoneUpdateRequested(m), tripId)

    fun cancelled(tripId: String = TRIP) = event(TripEventType.TRIP_CANCELLED, InboundPayload.TripCancelled("SHIPPER_CANCELLED"), tripId)

    /** Assign the default trip and open a consent request. */
    suspend fun withConsentRequest(requestId: String = "consent-789", validFor: Duration = Duration.ofMinutes(15)) {
        processor.process(assigned())
        processor.process(consentRequested(requestId, validFor))
    }
}
