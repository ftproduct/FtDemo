# Freight Tiger Driver Assistant (Android MVP)

A Hindi-first, voice-led **trip assistant** for truck drivers. It receives structured trip events from
Freight Tiger, speaks short Hindi prompts, understands spoken (or tapped) answers, and sends structured,
idempotent updates back: tracking consent, ETA to the loading point, loading arrival and loading status,
and support requests.

It is deliberately **not** a general-purpose assistant. It never reads SMS, calls or contacts, never listens
continuously, never answers calls, and never treats a "yes" as tracking consent until Freight Tiger's
backend has validated it.

> **Status:** MVP. Freight Tiger APIs are *proposed contracts* (they do not exist yet). The app ships with a
> clearly labelled **simulated backend (demo mode)** behind the same interfaces as the real HTTP client.
> See [What is real vs. mocked](#what-is-real-vs-mocked).

## Documentation

| Document | Contents |
|---|---|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Modules, layers, diagrams, key flows, state machines |
| [docs/API_INTEGRATION.md](docs/API_INTEGRATION.md) | Proposed REST/push contracts, auth, idempotency, retries, replacing the mock |
| [docs/PRIVACY_AND_PERMISSIONS.md](docs/PRIVACY_AND_PERMISSIONS.md) | Permissions, consent model, data handling, driver safety |
| [docs/PRODUCTION_READINESS.md](docs/PRODUCTION_READINESS.md) | Outstanding production dependencies and known limitations |

## Project layout

```
driver-assistant-android/
├── platform-core/                 Pure Kotlin/JVM build (no Android SDK needed) — included build
│   ├── core/model                 Trip, consent, prompt, outbox, event models (kotlinx.serialization)
│   ├── domain/workflow            Business rules: consent state machine, event processor, prompt
│   │                              scheduler, dialogue engine, trip coordinator, outbox sync,
│   │                              Hindi normaliser + intent classifier + ETA parser, onboarding
│   └── core/network               Proposed API contracts (Retrofit), error mapping, push parser,
│                                  simulated backend (demo mode), "not configured" guard
├── core/database                  Room persistence implementing the domain ports
├── core/security                  Android Keystore-backed token storage
└── app                            Compose UI (14 screens), Hilt DI, TTS/SpeechRecognizer adapters,
                                   FCM, notifications, WorkManager sync, runtime orchestration
```

Feature packages inside `app`: `feature/onboarding`, `home`, `trips`, `consent`, `voice`, `activity`,
`settings`, `sync`, `support`, `demo`.

## Requirements

- JDK 17+ (JDK 21 works). Android Studio Ladybug/Meerkat or newer recommended.
- Android SDK with platform 35 (compileSdk 35, minSdk 26).
- Network access to `dl.google.com` (Google Maven + Android SDK) and Maven Central.

## Build and run

```bash
cd driver-assistant-android

# Platform-independent core: unit + scenario tests (no Android SDK required)
./gradlew -p platform-core test            # or: ./gradlew platformCoreTest

# Android app (demo mode is ON by default in debug builds)
./gradlew :app:assembleDebug
./gradlew :app:installDebug                # with a device/emulator connected

# Instrumented UI tests (device/emulator; debug build, demo mode)
./gradlew :app:connectedDebugAndroidTest :core:database:connectedDebugAndroidTest
```

Open `driver-assistant-android/` in Android Studio; the `platform-core` included build is picked up
automatically.

### Environment configuration

Nothing environment-specific is hard-coded. Values come from Gradle properties (`-P…` or
`~/.gradle/gradle.properties`) or environment variables:

| Gradle property | Env var | Meaning | Default |
|---|---|---|---|
| `ftda.demoMode` | `FTDA_DEMO_MODE` | Use the in-app **simulated** backend | debug `true`, release `false` |
| `ftda.apiBaseUrl` | `FTDA_API_BASE_URL` | Freight Tiger assistant API base URL (`https://` only) | empty |
| `ftda.apiEnvironment` | `FTDA_API_ENVIRONMENT` | Label shown in Settings (e.g. `staging`) | `unset` |
| `ftda.supportPhone` | `FTDA_SUPPORT_PHONE` | Verified support number for the "Call support" button | empty → button hidden |
| `ftda.allowCleartext` | `FTDA_ALLOW_CLEARTEXT` | Debug only: allow `http://10.0.2.2` dev server | `false` |

Backend selection at start-up: **demo mode → simulated backend**; otherwise **base URL configured →
real HTTP client**; otherwise **"not configured"**, where every call fails visibly and nothing is faked.

Push (FCM) is enabled only when an environment-specific `app/google-services.json` exists (git-ignored).
Without it, the app reconciles trip state by polling `GET /trips/{id}/state` while visible.

Example against a staging backend:

```bash
./gradlew :app:assembleRelease -Pftda.demoMode=false \
  -Pftda.apiBaseUrl=https://assistant-staging.example.freighttiger.com/ -Pftda.apiEnvironment=staging
```

## Demo mode walkthrough

Debug builds start in demo mode. A yellow **"डेमो मोड — सिम्युलेटेड डेटा"** banner is shown on every screen
and every simulated trip, prompt and consent is badged **सिम्युलेटेड**.

1. Onboard: any valid Indian mobile number, demo OTP **`123456`** (shown on screen; no SMS is sent).
2. Home → **डेमो पैनल** (Demo panel):
   1. **Assign trip.** Hear the short spoken briefing (origin, destination, loading point, next action).
   2. **Open assistant.** The briefing is played.
   3. **Request tracking consent.** The Hindi consent question is spoken.
   4. Answer **by voice** ("हाँ" / "नहीं" / "अभी नहीं" / "दोबारा बताओ") or tap **हाँ / नहीं**.
   5. Watch the consent move to **"आपने हाँ कहा — फ्रेट टाइगर की पुष्टि बाकी"** (`GRANTED_PENDING_VALIDATION`)
      and the submission go *captured → submitted → accepted*.
   6. **6a/6b.** Simulate backend validation success (tracking becomes **चालू**) or failure. You can
      also switch the validation mode to auto-approve or auto-reject.
   7. **Ask ETA.** Answer "एक घंटा लगेगा" / "do ghante", or tap a duration.
   8. **Ask arrival.** Answer "मैं पहुँच गया" / "अभी रास्ते में हूँ".
   9. **Network simulation.** Toggle *phone offline* or *server unreachable*, or fail the next 3 requests
      with HTTP 503. Watch updates queue, back off and recover.
   10. **Event history.** Received events (applied/duplicate/rejected), sent updates with server
       acknowledgement, and the activity log. You can also resend the last event to see duplicate
       suppression.

## Tests

| Suite | Where | What | Runs without Android SDK |
|---|---|---|---|
| Domain unit tests (10 classes, 143 tests) | `platform-core/domain/workflow/src/test` | Normaliser, intent classifier (affirmative/negative/ambiguous/context), ETA parser, consent state machine, event processor (duplicates, invalid driver/trip, expiry, cancellation), prompt scheduler, dialogue engine, coordinator, outbox sync (offline, backoff, escalation, conflicts, validation failure), onboarding | ✅ |
| Network/contract + scenario tests (27 tests) | `platform-core/core/network/src/test` | Request bodies match the proposed JSON contract, headers, error mapping, push parsing, and end-to-end scenarios against the simulated backend (assignment, consent grant/decline/clarify, silence, ETA, arrival, network loss, duplicates, expiry while offline, cancellation) | ✅ |
| Room tests | `core/database/src/androidTest` | Idempotency-key uniqueness, due-event query, payload round-trip | device |
| UI / integration tests | `app/src/androidTest` | First-time onboarding, trip assignment, consent grant (tracking only after backend confirmation), decline, voice clarification, silence, ETA, arrival, network-loss recovery | device |

The critical safety cases are covered explicitly: "haan" only reaches `GRANTED_PENDING_VALIDATION`; "nahi"
never starts tracking; silence never grants; "abhi nahi" is a deferral; "haan" to an ETA question is
never consent; duplicate events do not trigger duplicate backend actions; cancelled trips refuse
activation; expired requests are never silently reused.

CI (`.github/workflows/driver-assistant-android.yml`) runs the platform-core tests, assembles the APK and
test APKs, and runs the instrumented tests on an emulator.

## What is real vs. mocked

| Capability | Status |
|---|---|
| Consent state machine, event processing, dialogue, intent rules, ETA parsing, outbox and retries | **Real** (production code, unit-tested) |
| Android TextToSpeech (hi-IN) and SpeechRecognizer (hi-IN) | **Real** platform integrations; quality depends on the device's installed voices and recogniser |
| Room persistence, Keystore token storage, WorkManager sync, notifications | **Real** |
| Freight Tiger REST API client | **Real client for *proposed* contracts.** No Freight Tiger endpoint exists yet |
| Mobile OTP verification | **Mocked** in demo mode (OTP `123456`); real client targets proposed `/auth/otp/*` endpoints |
| Backend validation of consent and tracking activation | **Simulated** in demo mode; real path waits for backend push or state poll |
| FCM push | Real integration code; **inactive** until `google-services.json` and a backend sender exist |
| Support callback | Never claimed; shown only if the backend returns `callback_scheduled: true` |
