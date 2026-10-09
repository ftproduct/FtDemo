package com.freighttiger.driverassistant.domain.conversation

import com.freighttiger.driverassistant.core.model.AssistantIntent
import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.ConsentDecision
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.PromptType
import com.freighttiger.driverassistant.core.model.QuestionContext
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.domain.voice.IntentClassifier
import com.freighttiger.driverassistant.domain.voice.NextAction
import com.freighttiger.driverassistant.domain.voice.PromptCatalog
import com.freighttiger.driverassistant.domain.voice.RecognitionResult
import com.freighttiger.driverassistant.domain.voice.SpeechErrorKind

data class DialoguePolicy(
    /** Clarification questions before escalating to tap/support. */
    val maxClarifications: Int = 2,
    /** Silent / no-match turns before deferring. Silence never counts as an answer. */
    val maxSilentTurns: Int = 2,
    val maxRepeats: Int = 3,
    /** Minimum combined confidence to accept a voice consent answer. */
    val consentMinConfidence: Float = 0.6f,
    val otherMinConfidence: Float = 0.45f,
)

/** Immutable state of one conversation about one prompt. */
data class DialogueSession(
    val prompt: AssistantPrompt,
    val context: QuestionContext,
    val questionText: String,
    val clarifications: Int = 0,
    val silentTurns: Int = 0,
    val repeats: Int = 0,
)

sealed interface DialogueOutcome {
    data class Consent(
        val decision: ConsentDecision,
        val method: CaptureMethod,
        val transcript: String?,
        val confidence: Float?,
    ) : DialogueOutcome

    data class Eta(val minutes: Int, val approximate: Boolean, val method: CaptureMethod) : DialogueOutcome
    data class Arrival(val arrived: Boolean, val etaMinutes: Int?, val method: CaptureMethod) : DialogueOutcome
    data class Loading(val status: LoadingStatus, val method: CaptureMethod) : DialogueOutcome
    data object Acknowledged : DialogueOutcome
    data object SupportRequested : DialogueOutcome
}

enum class EscalationReason { REPEATED_MISUNDERSTANDING, TOO_MANY_REPEATS }

sealed interface DialogueStep {
    val session: DialogueSession

    /** Speak [speech]; if [listen] is true the UI may open one listening turn (or show tap buttons). */
    data class Ask(override val session: DialogueSession, val speech: String, val listen: Boolean = true) : DialogueStep

    /** Conversation finished with a structured outcome. [speech] confirms what was captured. */
    data class Complete(override val session: DialogueSession, val outcome: DialogueOutcome, val speech: String) : DialogueStep

    /** Driver asked to be asked later (or did not answer). Prompt is re-scheduled. */
    data class Defer(override val session: DialogueSession, val speech: String) : DialogueStep

    /** Voice is not working for this prompt: show tap buttons and support option. */
    data class Escalate(override val session: DialogueSession, val speech: String, val reason: EscalationReason) : DialogueStep

    /** Microphone unavailable / denied: show tap buttons only. */
    data class FallbackToTap(override val session: DialogueSession, val speech: String) : DialogueStep
}

/** Explicit on-screen answers (large buttons). Always available as a fallback to voice. */
sealed interface TapResponse {
    data object Yes : TapResponse
    data object No : TapResponse
    data class EtaMinutes(val minutes: Int) : TapResponse
    data class Loading(val status: LoadingStatus) : TapResponse
    data object Repeat : TapResponse
    data object Support : TapResponse
    data object Later : TapResponse
}

/**
 * Pure conversation logic. Does not touch audio, persistence or network: the caller speaks
 * [DialogueStep] text, decides whether listening is allowed, and applies outcomes via
 * [com.freighttiger.driverassistant.domain.workflow.TripCoordinator].
 */
class DialogueEngine(
    private val classifier: IntentClassifier,
    private val catalog: PromptCatalog,
    private val policy: DialoguePolicy = DialoguePolicy(),
) {

    fun start(prompt: AssistantPrompt, trip: Trip?, nextAction: NextAction = NextAction.NONE): DialogueStep {
        val question = when (prompt.type) {
            PromptType.CONSENT -> catalog.consentQuestion()
            PromptType.ETA -> catalog.etaQuestion()
            PromptType.ARRIVAL -> catalog.arrivalQuestion()
            PromptType.LOADING_STATUS -> catalog.loadingQuestion()
            PromptType.TRIP_BRIEFING -> trip?.let { catalog.tripBriefing(it, nextAction) } ?: ""
            PromptType.TRACKING_RESULT -> when (prompt.detail) {
                "ACTIVE" -> catalog.trackingActivated()
                "STOPPED" -> catalog.trackingStopped()
                "EXPIRED" -> catalog.consentExpired()
                else -> catalog.trackingFailed()
            }
            PromptType.TRIP_CANCELLED -> catalog.tripCancelled()
            PromptType.TRIP_COMPLETED -> catalog.tripCompleted()
        }
        val session = DialogueSession(prompt, prompt.type.questionContext, question)
        return if (prompt.type.expectsResponse) {
            DialogueStep.Ask(session, question)
        } else {
            DialogueStep.Complete(session, DialogueOutcome.Acknowledged, question)
        }
    }

    /**
     * Interprets a free-form driver command when no question is pending ("main pahunch gaya",
     * "ek ghanta lagega", "loading shuru", "support"). Uses the GENERAL context, so it can never
     * produce a consent decision. Returns null when not understood.
     */
    fun interpretCommand(utterance: com.freighttiger.driverassistant.core.model.Utterance): DialogueOutcome? {
        val r = classifier.classify(utterance, QuestionContext.GENERAL)
        if (r.intent == AssistantIntent.REQUEST_HUMAN_SUPPORT) return DialogueOutcome.SupportRequested
        if (r.confidence < policy.otherMinConfidence) return null
        val eta = r.etaMinutes
        val loading = r.loadingStatus
        return when (r.intent) {
            AssistantIntent.ARRIVAL_YES -> DialogueOutcome.Arrival(true, null, CaptureMethod.VOICE)
            AssistantIntent.ETA_REPORTED -> eta?.let { DialogueOutcome.Eta(it, r.etaApproximate, CaptureMethod.VOICE) }
            AssistantIntent.LOADING_STATUS_REPORTED -> loading?.let { DialogueOutcome.Loading(it, CaptureMethod.VOICE) }
            else -> null
        }
    }

    /** Spoken confirmation for an outcome captured outside a question (commands, taps). */
    fun confirmationFor(outcome: DialogueOutcome): String = when (outcome) {
        is DialogueOutcome.Consent ->
            if (outcome.decision == ConsentDecision.GRANTED) catalog.consentCapturedYes() else catalog.consentCapturedNo()
        is DialogueOutcome.Eta -> catalog.etaCaptured(outcome.minutes, outcome.approximate)
        is DialogueOutcome.Arrival -> if (outcome.arrived) catalog.arrivalCapturedYes() else catalog.arrivalCapturedNo(outcome.etaMinutes)
        is DialogueOutcome.Loading -> catalog.loadingCaptured(outcome.status)
        DialogueOutcome.SupportRequested -> catalog.supportRequestQueued()
        DialogueOutcome.Acknowledged -> ""
    }

    fun onRecognition(session: DialogueSession, result: RecognitionResult): DialogueStep = when (result) {
        is RecognitionResult.Error -> onSpeechError(session, result.kind)
        is RecognitionResult.Recognized -> onUtterance(session, result)
    }

    fun onTap(session: DialogueSession, tap: TapResponse): DialogueStep {
        val m = CaptureMethod.TAP
        return when (tap) {
            TapResponse.Repeat -> DialogueStep.Ask(session, session.questionText)
            TapResponse.Support -> DialogueStep.Complete(session, DialogueOutcome.SupportRequested, catalog.supportRequestQueued())
            TapResponse.Later -> DialogueStep.Defer(session, catalog.deferAcknowledged())
            TapResponse.Yes -> when (session.context) {
                QuestionContext.CONSENT -> DialogueStep.Complete(session, DialogueOutcome.Consent(ConsentDecision.GRANTED, m, null, null), catalog.consentCapturedYes())
                QuestionContext.ARRIVAL -> DialogueStep.Complete(session, DialogueOutcome.Arrival(true, null, m), catalog.arrivalCapturedYes())
                else -> DialogueStep.Ask(session, session.questionText)
            }
            TapResponse.No -> when (session.context) {
                QuestionContext.CONSENT -> DialogueStep.Complete(session, DialogueOutcome.Consent(ConsentDecision.DECLINED, m, null, null), catalog.consentCapturedNo())
                QuestionContext.ARRIVAL -> DialogueStep.Complete(session, DialogueOutcome.Arrival(false, null, m), catalog.arrivalCapturedNo(null))
                else -> DialogueStep.Ask(session, session.questionText)
            }
            is TapResponse.EtaMinutes ->
                if (session.context == QuestionContext.ETA && tap.minutes > 0) {
                    DialogueStep.Complete(session, DialogueOutcome.Eta(tap.minutes, false, m), catalog.etaCaptured(tap.minutes, false))
                } else {
                    DialogueStep.Ask(session, session.questionText)
                }
            is TapResponse.Loading ->
                if (session.context == QuestionContext.LOADING_STATUS) {
                    DialogueStep.Complete(session, DialogueOutcome.Loading(tap.status, m), catalog.loadingCaptured(tap.status))
                } else {
                    DialogueStep.Ask(session, session.questionText)
                }
        }
    }

    private fun onSpeechError(session: DialogueSession, kind: SpeechErrorKind): DialogueStep = when (kind) {
        SpeechErrorKind.PERMISSION_DENIED, SpeechErrorKind.UNAVAILABLE ->
            DialogueStep.FallbackToTap(session, catalog.micUnavailable())
        SpeechErrorKind.CANCELLED -> DialogueStep.Defer(session, catalog.deferAcknowledged())
        else -> {
            val next = session.copy(silentTurns = session.silentTurns + 1)
            if (next.silentTurns >= policy.maxSilentTurns) {
                // Silence is never an answer. Defer; the prompt comes back later.
                DialogueStep.Defer(next, catalog.didNotHear() + " " + catalog.deferAcknowledged())
            } else {
                DialogueStep.Ask(next, catalog.didNotHear() + " " + session.questionText)
            }
        }
    }

    private fun onUtterance(session: DialogueSession, result: RecognitionResult.Recognized): DialogueStep {
        val classification = classifier.classify(result.utterance, session.context)
        val etaMinutes = classification.etaMinutes
        val loadingStatus = classification.loadingStatus
        val transcript = result.utterance.best?.text
        val v = CaptureMethod.VOICE

        when (classification.intent) {
            AssistantIntent.REQUEST_HUMAN_SUPPORT ->
                return DialogueStep.Complete(session, DialogueOutcome.SupportRequested, catalog.supportRequestQueued())
            AssistantIntent.REPEAT_PROMPT -> {
                val next = session.copy(repeats = session.repeats + 1)
                return if (next.repeats > policy.maxRepeats) {
                    DialogueStep.Escalate(next, catalog.escalate(), EscalationReason.TOO_MANY_REPEATS)
                } else {
                    DialogueStep.Ask(next, session.questionText)
                }
            }
            else -> Unit
        }
        if (classification.deferRequested) return DialogueStep.Defer(session, catalog.deferAcknowledged())

        val accepted: DialogueStep.Complete? = when (session.context) {
            QuestionContext.CONSENT -> when {
                classification.intent == AssistantIntent.CONSENT_YES &&
                    classification.confidence >= policy.consentMinConfidence ->
                    DialogueStep.Complete(
                        session,
                        DialogueOutcome.Consent(ConsentDecision.GRANTED, v, transcript, classification.confidence),
                        catalog.consentCapturedYes(),
                    )
                classification.intent == AssistantIntent.CONSENT_NO &&
                    classification.confidence >= policy.otherMinConfidence ->
                    DialogueStep.Complete(
                        session,
                        DialogueOutcome.Consent(ConsentDecision.DECLINED, v, transcript, classification.confidence),
                        catalog.consentCapturedNo(),
                    )
                else -> null
            }
            QuestionContext.ETA -> when {
                classification.confidence < policy.otherMinConfidence -> null
                classification.intent == AssistantIntent.ETA_REPORTED && etaMinutes != null ->
                    DialogueStep.Complete(
                        session,
                        DialogueOutcome.Eta(etaMinutes, classification.etaApproximate, v),
                        catalog.etaCaptured(etaMinutes, classification.etaApproximate),
                    )
                classification.intent == AssistantIntent.ARRIVAL_YES ->
                    DialogueStep.Complete(session, DialogueOutcome.Arrival(true, null, v), catalog.arrivalCapturedYes())
                else -> null
            }
            QuestionContext.ARRIVAL -> when {
                classification.confidence < policy.otherMinConfidence -> null
                classification.intent == AssistantIntent.ARRIVAL_YES ->
                    DialogueStep.Complete(session, DialogueOutcome.Arrival(true, null, v), catalog.arrivalCapturedYes())
                classification.intent == AssistantIntent.ARRIVAL_NO ->
                    DialogueStep.Complete(
                        session,
                        DialogueOutcome.Arrival(false, classification.etaMinutes, v),
                        catalog.arrivalCapturedNo(classification.etaMinutes),
                    )
                else -> null
            }
            QuestionContext.LOADING_STATUS -> when {
                classification.confidence < policy.otherMinConfidence -> null
                classification.intent == AssistantIntent.LOADING_STATUS_REPORTED && loadingStatus != null ->
                    DialogueStep.Complete(
                        session,
                        DialogueOutcome.Loading(loadingStatus, v),
                        catalog.loadingCaptured(loadingStatus),
                    )
                else -> null
            }
            QuestionContext.GENERAL -> null
        }
        if (accepted != null) return accepted

        val next = session.copy(clarifications = session.clarifications + 1)
        if (next.clarifications > policy.maxClarifications) {
            return DialogueStep.Escalate(next, catalog.escalate(), EscalationReason.REPEATED_MISUNDERSTANDING)
        }
        val clarify = when (session.context) {
            QuestionContext.CONSENT -> catalog.consentClarify()
            QuestionContext.ETA -> catalog.etaClarify()
            QuestionContext.ARRIVAL -> catalog.arrivalClarify()
            QuestionContext.LOADING_STATUS -> catalog.loadingClarify()
            QuestionContext.GENERAL -> session.questionText
        }
        return DialogueStep.Ask(next, clarify)
    }
}
