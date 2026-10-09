package com.freighttiger.driverassistant.core.network

import com.freighttiger.driverassistant.core.model.ConsentValidationStatus
import com.freighttiger.driverassistant.core.model.EventSource
import com.freighttiger.driverassistant.core.model.InboundPayload
import com.freighttiger.driverassistant.core.model.Milestone
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.core.model.TripEventType
import com.freighttiger.driverassistant.core.network.push.InboundEventParser
import com.freighttiger.driverassistant.core.network.push.ParseResult
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class InboundEventParserTest {
    private val parser = InboundEventParser { Instant.parse("2026-10-09T12:00:00Z") }

    private fun envelope(type: String, payload: String) =
        """{"event_id":"evt-1","schema_version":"1.0","event_type":"$type","driver_id":"driver-123","trip_id":"trip-456","issued_at":"2026-10-09T12:00:00Z","payload":$payload}"""

    @Test
    fun `consent request from push data`() {
        val r = parser.fromPushData(mapOf("ft_event" to envelope("CONSENT_REQUESTED", """{"consent_request_id":"consent-789","purpose":"SIM_BASED_TRIP_TRACKING","expires_at":"2026-10-09T12:15:00Z"}""")))
        val e = (r as ParseResult.Parsed).event
        assertEquals(TripEventType.CONSENT_REQUESTED, e.type)
        assertEquals(EventSource.PUSH, e.source)
        assertEquals("consent-789", (e.payload as InboundPayload.ConsentRequested).consentRequestId)
    }

    @Test
    fun `tracking update and milestone`() {
        val t = (parser.parse(envelope("TRACKING_STATUS_UPDATED", """{"tracking_status":"ACTIVE","consent_request_id":"c","consent_validation_status":"VALIDATED"}"""), EventSource.POLL) as ParseResult.Parsed).event
        val p = t.payload as InboundPayload.TrackingStatusUpdated
        assertEquals(TrackingStatus.ACTIVE, p.trackingStatus)
        assertEquals(ConsentValidationStatus.VALIDATED, p.consentValidation)
        val m = (parser.parse(envelope("MILESTONE_UPDATE_REQUESTED", """{"milestone":"ARRIVAL_AT_LOADING_POINT"}"""), EventSource.PUSH) as ParseResult.Parsed).event
        assertEquals(Milestone.ARRIVAL_AT_LOADING_POINT, (m.payload as InboundPayload.MilestoneUpdateRequested).milestone)
        val c = (parser.parse(envelope("TRIP_COMPLETED", "{}"), EventSource.PUSH) as ParseResult.Parsed).event
        assertEquals(InboundPayload.TripCompleted, c.payload)
    }

    @Test
    fun `trip assignment`() {
        val json = envelope(
            "TRIP_ASSIGNED",
            """{"trip":{"trip_id":"trip-456","driver_id":"driver-123","vehicle_registration":"MH12AB1234","origin":{"name":"a","city":"Pune"},"destination":{"name":"b","city":"Delhi"},"loading_point":{"name":"Gate 4","address":"x","city":"Chakan"},"status":"ASSIGNED","assigned_at":"2026-10-09T12:00:00Z"}}""",
        )
        val e = (parser.parse(json, EventSource.PUSH) as ParseResult.Parsed).event
        assertEquals("Delhi", (e.payload as InboundPayload.TripAssigned).trip.destination.city)
    }

    @Test
    fun `invalid inputs are rejected with reason`() {
        assertEquals(ParseResult.Invalid("MISSING_EVENT_PAYLOAD"), parser.fromPushData(emptyMap()))
        assertEquals(ParseResult.Invalid("MALFORMED_ENVELOPE"), parser.parse("{nope", EventSource.PUSH))
        assertEquals(ParseResult.Invalid("CONTRACT_VIOLATION"), parser.parse(envelope("SOMETHING_NEW", "{}"), EventSource.PUSH))
        assertEquals(
            ParseResult.Invalid("CONTRACT_VIOLATION"),
            parser.parse(envelope("CONSENT_REQUESTED", """{"consent_request_id":"c","purpose":"MARKETING","expires_at":"2026-10-09T12:15:00Z"}"""), EventSource.PUSH),
        )
        assertEquals(
            ParseResult.Invalid("CONTRACT_VIOLATION"),
            parser.parse(envelope("CONSENT_REQUESTED", """{"consent_request_id":"c","purpose":"SIM_BASED_TRIP_TRACKING","expires_at":"tomorrow"}"""), EventSource.PUSH),
        )
        assertEquals(ParseResult.Invalid("MALFORMED_PAYLOAD"), parser.parse(envelope("MILESTONE_UPDATE_REQUESTED", "{}"), EventSource.PUSH))
    }
}
