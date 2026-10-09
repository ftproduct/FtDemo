# Permissions, privacy and driver safety

## Permissions

| Permission | Why | When requested | If denied |
|---|---|---|---|
| `INTERNET`, `ACCESS_NETWORK_STATE` | Talk to Freight Tiger; show offline state; schedule sync | Install-time (normal) | — |
| `RECORD_AUDIO` | One listening turn after the driver taps the mic | Onboarding (with explanation), or at the first mic tap | Every question can be answered with large buttons |
| `POST_NOTIFICATIONS` (Android 13+) | Tell the driver about new trips and questions | Onboarding (with explanation) | Questions appear as "pending" cards when the app is opened |

**Not requested:** SMS, call log, contacts, phone state, location (foreground or background), `CALL_PHONE`,
or foreground-service microphone. The "Call support" button uses `ACTION_DIAL`, so the driver places the
call. The app never auto-answers, intercepts or reads phone calls or messages.

Location tracking is **SIM-based on Freight Tiger's side**. The app collects the driver's explicit consent
for it but does not read device location.

## Microphone use

- No always-on or background listening, no wake word, no foreground service.
- Listening happens only on the visible voice screen, for one bounded turn (up to 15 s), started by an
  explicit driver action ("सहायक से बात करें", "अभी जवाब दें", or the mic button). A prompt the app
  presents on its own is spoken, but listening waits for a tap.
- A labelled microphone indicator ("माइक चालू — सुन रहा है" / "माइक बंद") is always visible on the voice
  screen. The driver can stop audio and listening at any time.
- Raw audio is never stored or uploaded by the app. The recogniser's text is used to interpret the answer,
  shown back to the driver ("आपने कहा: …"), and, **for consent answers only**, sent as evidence
  (configurable).

> Note: Android's `SpeechRecognizer` may use the device vendor's or Google's speech service, which can
> process audio off-device under that service's own terms. Production must decide whether this is
> acceptable or require an on-device recogniser (`EXTRA_PREFER_OFFLINE` / on-device recognisers on
> Android 13+). The `SpeechInput` port allows swapping it.

## Consent model (SIM-based trip tracking)

1. Consent is requested **by the backend**, per trip and purpose (`SIM_BASED_TRIP_TRACKING`), with an
   expiry.
2. The assistant explains the purpose in Hindi and asks for an explicit yes or no. Both answers are
   equally easy (same-size buttons, both accepted by voice).
3. Silence, unclear answers, weak agreement ("ठीक है"), hedges and "अभी नहीं" are **never** consent.
   The assistant clarifies, defers, or offers buttons.
4. A "yes" becomes `GRANTED_PENDING_VALIDATION`. The app says the answer was recorded and that tracking
   starts only after Freight Tiger confirms. It does not say that tracking is active.
5. Only the backend's `VALIDATED` verdict plus `TRACKING_STATUS_UPDATED(ACTIVE)` makes tracking show as
   active.
6. Consent is never reused across trips or purposes, and expired requests are never silently reused.
7. **Withdrawal:** Settings → "ट्रैकिंग अनुमति देखें" → "अनुमति वापस लें" (with confirmation). This sends
   `CONSENT_WITHDRAWN`, and tracking shows "बंद करने का अनुरोध भेजा गया" until the backend confirms `STOPPED`.
8. The full consent history per trip (state, capture method, time, submission status) is visible to the
   driver.

## Data stored on the device

| Data | Where | Protection / retention |
|---|---|---|
| Access token | SharedPreferences, AES-256-GCM encrypted with a non-exportable Android Keystore key | Cleared on logout or key invalidation |
| Session (driver id, display name, **masked** phone) | Room | App-private storage; cleared on logout |
| Trips, prompts, consent records (incl. consent transcript evidence), outbox, event history | Room | App-private storage. Backup and device transfer **disabled** (`allowBackup=false`, data-extraction rules). `RoomActivityLog.pruneOlderThan` is available for retention |
| Settings (auto-speak, pause, delays) | SharedPreferences | Non-sensitive |

The full phone number is not stored: it is sent once to the OTP endpoint and only a masked form is kept.

**Logging:** HTTP logs contain only method, path, status and duration (no bodies, tokens or headers).
Speech text is never logged. Activity-history entries hold codes and statuses, not transcripts.

## Driver safety

- Short spoken prompts. Large (≥ 64 dp), high-contrast buttons and large type. Minimal navigation.
- Every question has a **"बाद में"** (Later) option: in the voice screen, in the notification, and by
  voice ("अभी नहीं", "बाद में"). A deferral is reported to the backend so coordinators know not to call.
- Configurable cooldown between repeats of the same question, "pause non-urgent questions", and a "stop
  audio" button on the voice and settings screens. Prompts are dropped after 4 unanswered deliveries.
- No typing is ever required during a trip. Typing appears only in onboarding.
- Prompts are spoken only when the app is visible and the phone is not in a call or ringing. Otherwise a
  notification is shown.
- The app **cannot know whether the vehicle is parked**. It does not claim to, and it reminds the driver
  to stop safely before touching the screen.

## Hindi-first language

All UI copy lives in `res/values/strings.xml` (Hindi, default) and `res/values-en/strings.xml`. All spoken
text is in `HindiPromptCatalog` (Devanagari, for better TTS pronunciation). Adding a language means adding
a `values-xx` folder, a `PromptCatalog` implementation, and normaliser and lexicon entries. The language
picker already lists Marathi, Tamil, Telugu, Bengali and English as "coming soon".
