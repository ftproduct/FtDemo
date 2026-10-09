# API integration guide

> These are **proposed** contracts for Freight Tiger's backend team. None of these endpoints exist today.
> The Android client (`platform-core/core/network`) implements them exactly, and
> `ApiContractTest` asserts the request bodies and headers it sends. Change both together.

## Conventions

| Topic | Contract |
|---|---|
| Base URL | Per environment, injected at build time (`ftda.apiBaseUrl` / `FTDA_API_BASE_URL`). HTTPS only. |
| Auth | `Authorization: Bearer <access_token>`, issued by the OTP flow. The token is stored with Android Keystore AES-GCM encryption. |
| Headers on every call | `X-Schema-Version: 1.0`, `X-Client-Platform: android`, `X-Client-Version`, `X-Request-Id` (UUID), `Accept-Language: hi-IN` |
| Idempotency | Every POST carries `Idempotency-Key` (header) = `idempotency_key` (body). Same key + same payload → `200` with `status: "DUPLICATE"` and **no side effects**. Same key + different payload → `409`. |
| Timestamps | ISO 8601 UTC (`2026-10-09T12:30:00Z`). `captured_at` is the moment the driver answered (preserved across offline periods), not the send time. |
| Schema versioning | `schema_version: "1.0"`. The client rejects inbound events whose major version ≠ 1. |
| Errors | JSON `{ "error_code": "…", "message": "…", "field_errors": [{ "field": "…", "code": "…" }] }` |

### Status codes → client behaviour

| HTTP | Client mapping | Retry? |
|---|---|---|
| 2xx + valid body | Success → outbox `ACKNOWLEDGED` | — |
| 400 / 422 | `Validation(error_code)` → `FAILED_PERMANENT`; consent → `VALIDATION_FAILED` | No |
| 401 / 403 | `Unauthorized` → batch stops, events kept, re-login required | After login |
| 404 | `NotFound` | No |
| 409 | `Validation(IDEMPOTENCY_CONFLICT)` | No |
| 429 | `RateLimited(Retry-After)` | Yes, after `Retry-After` |
| 5xx, network error, malformed body | Retryable | Exponential backoff (5 s × 2ⁿ, ≤ 15 min, jitter, 8 attempts) |

## Endpoints

### `POST /api/v1/assistant/consent-responses`

Consent decisions (`CONSENT_RESPONSE_CAPTURED`) and withdrawals (`CONSENT_WITHDRAWN`).

```json
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
```

- `decision`: `GRANTED` | `DECLINED` | `WITHDRAWN` (with `event_type: CONSENT_WITHDRAWN`, key
  `<consent_request_id>-withdrawal-1`).
- `capture_method`: `VOICE` | `TAP`. `evidence.transcript` is the recogniser text (null for taps). It can
  be disabled with `ApiConfig.sendTranscriptEvidence = false`. **Raw audio is never uploaded**, so
  `recording_reference` is always null.
- Response: an acknowledgement with **`consent_validation_status`**:
  - `PENDING`: stored, checks still running (the normal case). The app shows "पुष्टि बाकी".
  - `VALIDATED`: checks passed. Consent becomes `GRANTED` (tracking still waits for `TRACKING_STATUS_UPDATED`).
  - `REJECTED`: → `VALIDATION_FAILED`.
  - `EXPIRED_NEEDS_REVIEW`: captured before expiry but received after it. Must **not** activate tracking
    without human review.

**The backend must validate:** that the driver ↔ trip ↔ consent request association is correct, the
purpose, that the request is unexpired at `captured_at`, that the trip is active, and any regulatory or
telecom-operator checks for SIM-based tracking. It must also store evidence with access control and a
retention policy.

### `POST /api/v1/assistant/trip-updates`

```json
{
  "event_type": "ETA_REPORTED",
  "event_id": "…", "schema_version": "1.0",
  "driver_id": "driver-123", "trip_id": "trip-456",
  "capture_method": "VOICE",
  "captured_at": "2026-10-09T12:30:00Z",
  "eta": { "eta_minutes": 60, "approximate": false, "estimated_arrival_at": "2026-10-09T13:30:00Z", "source": "DRIVER_REPORTED" },
  "arrival": null,
  "loading_status": null,
  "idempotency_key": "eta_reported-…"
}
```

- `event_type`: `ETA_REPORTED` (with `eta`), `ARRIVAL_REPORTED` (with `arrival: { "arrived": true, "source": "DRIVER_REPORTED" }`),
  or `LOADING_STATUS_REPORTED` (with `loading_status`: `REACHED_LOADING_POINT | WAITING_FOR_LOADING | LOADING_STARTED | LOADING_COMPLETED | ISSUE_REPORTED`).
- Driver-reported values are labelled `DRIVER_REPORTED` and must be stored **separately** from GPS or
  geofence data. They are never predictions.
- The backend validates loading-status transitions and trip state, and returns 422 with `error_code` if
  they are invalid.

### `POST /api/v1/assistant/support-requests`

```json
{ "event_id": "…", "schema_version": "1.0", "driver_id": "driver-123", "trip_id": "trip-456",
  "reason": "DRIVER_REQUESTED", "captured_at": "…", "idempotency_key": "support_requested-…" }
```

`reason`: `DRIVER_REQUESTED | RECOGNITION_FAILED | SYNC_FAILED | TRACKING_FAILED`. The ack may include
`callback_scheduled: true`, and only then does the app say a callback is scheduled.

### `POST /api/v1/assistant/events`

Assistant interaction telemetry (no personal content), e.g. deferrals and escalations, so coordinators
know not to call a driver who is driving:

```json
{ "event_type": "ASSISTANT_INTERACTION", "event_id": "…", "schema_version": "1.0",
  "driver_id": "…", "trip_id": "…", "interaction": "PROMPT_DEFERRED", "prompt_type": "ETA",
  "captured_at": "…", "idempotency_key": "assistant_interaction-…" }
```

### Acknowledgement (all POSTs)

```json
{ "event_id": "…", "status": "ACCEPTED", "received_at": "2026-10-09T12:30:05Z",
  "server_reference": "…", "consent_validation_status": "PENDING", "callback_scheduled": null }
```

### `GET /api/v1/assistant/trips/active`

Returns `{ "trip": TripDto | null }` for the authenticated driver. The client refuses a trip whose
`driver_id` differs from the session.

```json
{ "trip": { "trip_id": "trip-456", "driver_id": "driver-123", "vehicle_registration": "MH12AB1234",
  "origin": { "name": "Chakan MIDC", "city": "Pune" }, "destination": { "name": "Narela", "city": "Delhi" },
  "loading_point": { "name": "Gate 4", "address": "Plot 12…", "city": "Chakan", "reporting_time": "…" },
  "status": "ASSIGNED", "tracking_status": "NOT_STARTED", "loading_status": "NOT_REACHED",
  "assigned_at": "…", "geofence_arrival_verified_at": null } }
```

### `GET /api/v1/assistant/trips/{tripId}/state`

The authoritative state, used for reconciliation when push is unavailable or late:

```json
{ "trip_id": "trip-456", "status": "ASSIGNED", "tracking_status": "ACTIVE", "loading_status": "NOT_REACHED",
  "consent": { "consent_request_id": "consent-789", "validation_status": "VALIDATED" }, "as_of": "…" }
```

### Additional endpoints proposed by this MVP

| Endpoint | Purpose |
|---|---|
| `POST /api/v1/assistant/auth/otp/request` `{ "phone_number" }` → `{ "challenge_id", "expires_at" }` | Mobile verification |
| `POST /api/v1/assistant/auth/otp/verify` `{ "challenge_id", "phone_number", "otp" }` → `{ "access_token", "expires_at", "driver": { "driver_id", "display_name", "masked_phone", "preferred_language" } }` | Login + driver identity |
| `PUT /api/v1/assistant/devices/push-token` `{ "driver_id", "push_token", "platform", "app_version" }` → 2xx | FCM token registration |

## Push events (FCM data messages)

Send **data-only** messages (no `notification` block) with high priority. The key `ft_event` holds the
JSON envelope:

```json
{
  "event_id": "evt-…", "schema_version": "1.0", "event_type": "CONSENT_REQUESTED",
  "driver_id": "driver-123", "trip_id": "trip-456",
  "issued_at": "2026-10-09T12:00:00Z", "expires_at": null,
  "payload": { "consent_request_id": "consent-789", "purpose": "SIM_BASED_TRIP_TRACKING", "expires_at": "2026-10-09T12:15:00Z" }
}
```

| `event_type` | `payload` |
|---|---|
| `TRIP_ASSIGNED` | `{ "trip": TripDto }` |
| `CONSENT_REQUESTED` | `{ "consent_request_id", "purpose", "expires_at" }` |
| `TRACKING_STATUS_UPDATED` | `{ "tracking_status", "consent_request_id"?, "consent_validation_status"?, "reason_code"? }` |
| `MILESTONE_UPDATE_REQUESTED` | `{ "milestone": "ETA_TO_LOADING_POINT" \| "ARRIVAL_AT_LOADING_POINT" \| "LOADING_STATUS" }` |
| `TRIP_CANCELLED` | `{ "reason_code"? }` |
| `TRIP_COMPLETED` | `{}` |

Rules the client applies: `event_id` must be unique and stable (it is used for de-duplication), `driver_id`
must match the logged-in driver, and the trip must already be known (except for `TRIP_ASSIGNED`).
Activation (`tracking_status: ACTIVE`) is accepted only together with, or after, `VALIDATED` for a
consent request of the same trip, and never for a cancelled or completed trip.

## Replacing the mock with real integrations

1. **Backend:** build with `-Pftda.demoMode=false -Pftda.apiBaseUrl=https://…`. `BackendSelection` (in
   `app/.../di`) then wires `RemoteAssistantBackend` and `RemoteAuthGateway`. No code changes are needed if
   the backend implements the contracts above. If the backend differs, adapt only
   `core-network/dto/Mappers.kt`, `Dtos.kt` and `remote/FreightTigerAssistantApi.kt`, and update
   `ApiContractTest`.
2. **Auth:** if Freight Tiger already has a driver-auth service, implement `AuthGateway` against it. The
   onboarding flow is unchanged.
3. **Push:** add the environment's `app/google-services.json`. The Google Services plugin is applied
   automatically when the file exists. Configure the backend to send the data messages above to the token
   registered through `PUT /devices/push-token`.
4. **Speech:** to use a cloud or on-device ASR, implement `SpeechInput`. To use an AI intent model,
   implement `IntentClassifier` and keep its contract: never return consent intents outside the consent
   context, and never return consent for empty, hedged, deferring or conflicting input. Run
   `RuleBasedIntentClassifierTest` cases against it.
5. **Support number:** set `ftda.supportPhone` to a verified number. Otherwise the call button is hidden.
6. **Demo mode** must be off in release builds (it is by default). The simulated backend is never used in
   any other mode.
