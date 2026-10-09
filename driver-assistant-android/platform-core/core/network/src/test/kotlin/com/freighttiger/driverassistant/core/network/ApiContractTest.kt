package com.freighttiger.driverassistant.core.network

import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.ConsentDecision
import com.freighttiger.driverassistant.core.model.ConsentPurpose
import com.freighttiger.driverassistant.core.model.ConsentValidationStatus
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.OutboundPayload
import com.freighttiger.driverassistant.core.network.remote.NetworkFactory
import com.freighttiger.driverassistant.core.network.remote.RemoteAssistantBackend
import com.freighttiger.driverassistant.domain.backend.AckStatus
import com.freighttiger.driverassistant.domain.backend.BackendError
import com.freighttiger.driverassistant.domain.backend.BackendResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class ApiContractTest {
    private lateinit var server: MockWebServer
    private lateinit var backend: RemoteAssistantBackend

    private val consentEvent = OutboundEvent(
        eventId = "unique-event-id",
        idempotencyKey = "consent-789-response-1",
        driverId = "driver-123",
        tripId = "trip-456",
        payload = OutboundPayload.ConsentResponse(
            consentRequestId = "consent-789",
            purpose = ConsentPurpose.SIM_BASED_TRIP_TRACKING,
            decision = ConsentDecision.GRANTED,
            captureMethod = CaptureMethod.VOICE,
            language = "hi-IN",
            capturedAt = Instant.parse("2026-10-09T12:30:00Z"),
            transcript = "Haan, main sahmat hoon",
        ),
        createdAt = Instant.parse("2026-10-09T12:30:00Z"),
    )

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val config = ApiConfig(baseUrl = server.url("/").toString(), environment = "test", clientVersion = "0.1.0-test", allowCleartext = true)
        val client = NetworkFactory.okHttpClient(config, { "test-token" })
        backend = RemoteAssistantBackend(NetworkFactory.api(config, client), config)
    }

    @After
    fun tearDown() = server.shutdown()

    private fun ok(body: String) = MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(body)

    @Test
    fun `consent response body matches the proposed contract example`() = runTest {
        server.enqueue(ok("""{"event_id":"unique-event-id","status":"ACCEPTED","received_at":"2026-10-09T12:30:05Z","consent_validation_status":"PENDING"}"""))
        val result = backend.submit(consentEvent)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/assistant/consent-responses", request.path)
        assertEquals("consent-789-response-1", request.getHeader("Idempotency-Key"))
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
        assertEquals("1.0", request.getHeader("X-Schema-Version"))

        val expected = Json.parseToJsonElement(
            """
            {
              "event_type": "CONSENT_RESPONSE_CAPTURED",
              "event_id": "unique-event-id",
              "schema_version": "1.0",
              "driver_id": "driver-123",
              "trip_id": "trip-456",
              "consent_request_id": "consent-789",
              "purpose": "SIM_BASED_TRIP_TRACKING",
              "decision": "GRANTED",
              "capture_method": "VOICE",
              "language": "hi-IN",
              "captured_at": "2026-10-09T12:30:00Z",
              "evidence": { "transcript": "Haan, main sahmat hoon", "recording_reference": null },
              "idempotency_key": "consent-789-response-1"
            }
            """.trimIndent(),
        )
        assertEquals(expected, Json.parseToJsonElement(request.body.readUtf8()))

        val ack = (result as BackendResult.Success).value
        assertEquals(AckStatus.ACCEPTED, ack.status)
        assertEquals(ConsentValidationStatus.PENDING, ack.consentValidation)
    }

    @Test
    fun `transcript evidence can be disabled by configuration`() = runTest {
        val config = ApiConfig(server.url("/").toString(), "test", "t", sendTranscriptEvidence = false, allowCleartext = true)
        val b = RemoteAssistantBackend(NetworkFactory.api(config, NetworkFactory.okHttpClient(config, { null })), config)
        server.enqueue(ok("""{"event_id":"e","status":"ACCEPTED","received_at":"2026-10-09T12:30:05Z"}"""))
        b.submit(consentEvent)
        val body = Json.parseToJsonElement(server.takeRequest().body.readUtf8()) as JsonObject
        assertEquals("""{"transcript":null,"recording_reference":null}""", body["evidence"].toString())
    }

    @Test
    fun `eta is posted to trip-updates as driver reported`() = runTest {
        server.enqueue(ok("""{"event_id":"e","status":"ACCEPTED","received_at":"2026-10-09T12:30:05Z"}"""))
        backend.submit(
            consentEvent.copy(
                idempotencyKey = "eta-1",
                payload = OutboundPayload.EtaReported(60, false, Instant.parse("2026-10-09T13:30:00Z"), CaptureMethod.VOICE, Instant.parse("2026-10-09T12:30:00Z")),
            ),
        )
        val request = server.takeRequest()
        assertEquals("/api/v1/assistant/trip-updates", request.path)
        val body = Json.parseToJsonElement(request.body.readUtf8()) as JsonObject
        assertEquals(
            Json.parseToJsonElement("""{"eta_minutes":60,"approximate":false,"estimated_arrival_at":"2026-10-09T13:30:00Z","source":"DRIVER_REPORTED"}"""),
            body["eta"],
        )
    }

    @Test
    fun `validation errors are permanent with server error code`() = runTest {
        server.enqueue(MockResponse().setResponseCode(422).setBody("""{"error_code":"CONSENT_REQUEST_EXPIRED","message":"expired"}"""))
        val r = backend.submit(consentEvent) as BackendResult.Failure
        assertEquals(BackendError.Validation("CONSENT_REQUEST_EXPIRED"), r.error)
        assertTrue(!r.error.retryable)
    }

    @Test
    fun `server errors, rate limits and auth are mapped`() = runTest {
        server.enqueue(MockResponse().setResponseCode(503))
        assertEquals(BackendError.Server(503), (backend.submit(consentEvent) as BackendResult.Failure).error)
        server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "30"))
        assertEquals(BackendError.RateLimited(30), (backend.submit(consentEvent) as BackendResult.Failure).error)
        server.enqueue(MockResponse().setResponseCode(401))
        assertEquals(BackendError.Unauthorized, (backend.submit(consentEvent) as BackendResult.Failure).error)
        server.enqueue(MockResponse().setResponseCode(409))
        assertEquals(BackendError.Validation("IDEMPOTENCY_CONFLICT"), (backend.submit(consentEvent) as BackendResult.Failure).error)
    }

    @Test
    fun `malformed responses are retryable failures, never fake success`() = runTest {
        server.enqueue(ok("""{"event_id":"e","status":"SOMETHING_ELSE","received_at":"2026-10-09T12:30:05Z"}"""))
        val r = backend.submit(consentEvent) as BackendResult.Failure
        assertTrue(r.error is BackendError.MalformedResponse)
        server.enqueue(ok("not json"))
        assertTrue((backend.submit(consentEvent) as BackendResult.Failure).error is BackendError.MalformedResponse)
    }

    @Test
    fun `network failure is retryable`() = runTest {
        server.shutdown()
        val r = backend.submit(consentEvent) as BackendResult.Failure
        assertTrue(r.error is BackendError.Network)
        assertTrue(r.error.retryable)
    }

    @Test
    fun `active trip belonging to another driver is rejected`() = runTest {
        server.enqueue(ok(tripJson(driver = "someone-else")))
        assertTrue((backend.fetchActiveTrip("driver-123") as BackendResult.Failure).error is BackendError.MalformedResponse)
        server.enqueue(ok(tripJson(driver = "driver-123")))
        val trip = (backend.fetchActiveTrip("driver-123") as BackendResult.Success).value
        assertEquals("trip-456", trip?.tripId)
        assertEquals("/api/v1/assistant/trips/active", server.takeRequest().path)
    }

    @Test
    fun `trip state is parsed`() = runTest {
        server.enqueue(
            ok("""{"trip_id":"trip-456","status":"ASSIGNED","tracking_status":"ACTIVE","loading_status":"NOT_REACHED","consent":{"consent_request_id":"consent-789","validation_status":"VALIDATED"},"as_of":"2026-10-09T12:40:00Z"}"""),
        )
        val s = (backend.fetchTripState("trip-456") as BackendResult.Success).value
        assertEquals(ConsentValidationStatus.VALIDATED, s.consent?.validation)
        assertEquals("/api/v1/assistant/trips/trip-456/state", server.takeRequest().path)
    }

    @Test
    fun `unconfigured base url is refused`() {
        val config = ApiConfig("", "prod", "1")
        assertTrue(!config.isConfigured)
        assertTrue(!ApiConfig("http://insecure.example", "prod", "1").isConfigured)
        assertTrue(runCatching { NetworkFactory.api(config, NetworkFactory.okHttpClient(config, { null })) }.isFailure)
    }

    private fun tripJson(driver: String) = """
        {"trip":{"trip_id":"trip-456","driver_id":"$driver","vehicle_registration":"MH12AB1234",
        "origin":{"name":"Chakan","city":"Pune"},"destination":{"name":"Narela","city":"Delhi"},
        "loading_point":{"name":"Gate 4","address":"Plot 12","city":"Chakan"},
        "status":"ASSIGNED","assigned_at":"2026-10-09T12:00:00Z"}}
    """.trimIndent()
}
