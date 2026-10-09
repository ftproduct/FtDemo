# Production readiness: outstanding dependencies and known limitations

## What Freight Tiger's backend team must provide before launch

1. **Assistant API** implementing (or mapping to) the contracts in [API_INTEGRATION.md](API_INTEGRATION.md):
   consent responses, trip updates, support requests, interaction events, active trip, trip state, and
   push-token registration. This includes idempotency-key handling (`DUPLICATE` with no side effects) and
   structured `error_code`s.
2. **Driver authentication:** mobile OTP (or an existing Freight Tiger identity service) that issues
   short-lived bearer tokens bound to a driver id, with refresh/expiry semantics. The MVP has no refresh
   flow: on `401` the app keeps events queued and the driver must log in again.
3. **Driver ↔ trip ↔ vehicle mapping:** the source of truth for `GET /trips/active` and for validating
   every inbound or outbound event's association.
4. **Consent validation service** for `SIM_BASED_TRIP_TRACKING`: telecom-operator or regulatory checks
   (e.g. operator-side subscriber confirmation), consent expiry policy, the `EXPIRED_NEEDS_REVIEW`
   human-review path, evidence storage with access control and a retention policy, and the audit trail.
   Legal review of the Hindi consent wording and purpose disclosure (DPDP Act 2023 alignment).
5. **Tracking activation and stop** driven only by validated consent, emitting `TRACKING_STATUS_UPDATED`
   (push) and reflecting it in `GET /trips/{id}/state`. Refuse activation for cancelled or completed trips.
6. **Push delivery:** an FCM project per environment (`google-services.json`), a server-side sender for
   data-only high-priority messages with stable `event_id`s, and expiry (`expires_at`) on time-bound prompts.
7. **Support workflow:** a queue for `support-requests`, a verified support phone number
   (`ftda.supportPhone`), and `callback_scheduled` only when a callback is really booked.
8. **Environments:** staging and production base URLs, TLS certificates, rate limits (`429` +
   `Retry-After`), monitoring of duplicate/expired/conflict rates.

## App-side work remaining

| Area | Gap | Suggested next step |
|---|---|---|
| Build verification | This MVP was developed where Google Maven / the Android SDK were blocked, so the Android modules were syntax-checked and reviewed but **not compiled locally**. CI (`.github/workflows/driver-assistant-android.yml`) compiles them and runs the instrumented tests. | Keep CI green and run a manual QA pass on real devices |
| Speech quality | Recognition quality depends on the device's hi-IN recogniser and installed Hindi TTS voice. Rules cover common answers, not every dialect or code-mix. | Field-test with drivers, collect anonymised failure phrases (with consent), extend the lexicon, or plug in an AI `IntentClassifier` behind the same contract |
| On-device ASR | `SpeechRecognizer` may process audio off-device | Decide on a privacy policy. Prefer on-device recognition where available |
| Database migrations | Schema v1 uses destructive fallback | Add Room migrations before the first schema change |
| Database encryption | Room DB is in app-private storage, not encrypted | Consider SQLCipher if consent evidence must be encrypted at rest |
| Token refresh | Not implemented | Add a refresh-token flow once the auth API is defined |
| Vehicle motion | Not detected. The app cannot tell whether the vehicle is parked. | Optional: Activity Recognition (needs permission + disclosure) to suppress prompts while moving |
| Accessibility | Large targets, content descriptions, high contrast; TalkBack not fully audited | TalkBack audit; font-scale testing at 200 % |
| Localisation | Hindi UI + English fallback; other languages listed as "coming soon" | Add `values-xx` + `PromptCatalog` + lexicons per language |
| Analytics / crash reporting | None (deliberately) | Add privacy-reviewed crash reporting without PII |
| Play policy | Microphone use is foreground-only, which is compatible | Complete the Data safety form from PRIVACY_AND_PERMISSIONS.md |

## Known limitations of the MVP

- One active trip at a time. A newer assignment becomes the active trip, and older non-terminal trips stay
  in storage.
- Prompts resurface after "Later" via WorkManager. Exact timing depends on OS scheduling (Doze).
- Without FCM (no `google-services.json`), backend-initiated prompts arrive only through the demo channel
  or the trip-state poll while the app is visible (every 60 s). That poll reconciles consent and tracking
  state but does not deliver new questions.
- ETA parsing rejects vague durations ("thodi der") and dates ("kal subah") and asks again. It does not
  infer times of day.
- Free-form "Talk" commands (no pending question) understand arrival, ETA, loading status and support.
  They never grant consent.
- Demo OTP is `123456`. The demo backend never schedules callbacks and is never used outside demo mode.
