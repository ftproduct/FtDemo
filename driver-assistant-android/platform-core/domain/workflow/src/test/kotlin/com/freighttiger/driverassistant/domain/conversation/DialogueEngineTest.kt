package com.freighttiger.driverassistant.domain.conversation

import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.ConsentDecision
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.PromptType
import com.freighttiger.driverassistant.core.model.Utterance
import com.freighttiger.driverassistant.domain.support.T0
import com.freighttiger.driverassistant.domain.voice.HindiPromptCatalog
import com.freighttiger.driverassistant.domain.voice.RecognitionResult
import com.freighttiger.driverassistant.domain.voice.RuleBasedIntentClassifier
import com.freighttiger.driverassistant.domain.voice.SpeechErrorKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DialogueEngineTest {
    private val catalog = HindiPromptCatalog()
    private val engine = DialogueEngine(RuleBasedIntentClassifier(), catalog)
    private fun prompt(type: PromptType) = AssistantPrompt("p-1", "trip-456", type, "k", T0, consentRequestId = if (type == PromptType.CONSENT) "consent-789" else null)
    private fun say(text: String, confidence: Float? = null) = RecognitionResult.Recognized(Utterance(text, confidence))
    private val silence = RecognitionResult.Error(SpeechErrorKind.SILENCE)

    private fun consentSession() = (engine.start(prompt(PromptType.CONSENT), null) as DialogueStep.Ask).session

    @Test
    fun `consent question is asked with the explicit hindi prompt`() {
        val step = engine.start(prompt(PromptType.CONSENT), null) as DialogueStep.Ask
        assertEquals(catalog.consentQuestion(), step.speech)
        assertTrue(step.listen)
    }

    @Test
    fun `haan completes with GRANTED and voice evidence`() {
        val step = engine.onRecognition(consentSession(), say("Haan, main sahmat hoon")) as DialogueStep.Complete
        val outcome = step.outcome as DialogueOutcome.Consent
        assertEquals(ConsentDecision.GRANTED, outcome.decision)
        assertEquals(CaptureMethod.VOICE, outcome.method)
        assertEquals("Haan, main sahmat hoon", outcome.transcript)
        // Confirmation does not claim tracking is active.
        assertEquals(catalog.consentCapturedYes(), step.speech)
    }

    @Test
    fun `silence never grants consent - asks again then defers`() {
        val first = engine.onRecognition(consentSession(), silence)
        assertTrue(first is DialogueStep.Ask)
        val second = engine.onRecognition(first.session, silence)
        assertTrue(second is DialogueStep.Defer)
    }

    @Test
    fun `ambiguous answer is clarified then escalated, never granted`() {
        var step: DialogueStep = engine.start(prompt(PromptType.CONSENT), null)
        val texts = listOf("theek hai", "shayad", "haan nahi")
        val kinds = mutableListOf<String>()
        for (t in texts) {
            step = engine.onRecognition(step.session, say(t))
            kinds += step::class.simpleName!!
            assertTrue(step !is DialogueStep.Complete)
        }
        assertEquals(listOf("Ask", "Ask", "Escalate"), kinds)
    }

    @Test
    fun `clarification uses the consent clarification prompt`() {
        val step = engine.onRecognition(consentSession(), say("theek hai")) as DialogueStep.Ask
        assertEquals(catalog.consentClarify(), step.speech)
        val ok = engine.onRecognition(step.session, say("haan")) as DialogueStep.Complete
        assertEquals(ConsentDecision.GRANTED, (ok.outcome as DialogueOutcome.Consent).decision)
    }

    @Test
    fun `low recogniser confidence on yes asks again`() {
        assertTrue(engine.onRecognition(consentSession(), say("haan", 0.3f)) is DialogueStep.Ask)
    }

    @Test
    fun `abhi nahi defers the consent prompt`() {
        assertTrue(engine.onRecognition(consentSession(), say("Abhi nahi")) is DialogueStep.Defer)
    }

    @Test
    fun `mic denied falls back to tap`() {
        assertTrue(engine.onRecognition(consentSession(), RecognitionResult.Error(SpeechErrorKind.PERMISSION_DENIED)) is DialogueStep.FallbackToTap)
    }

    @Test
    fun `tap yes and no are explicit decisions`() {
        val yes = engine.onTap(consentSession(), TapResponse.Yes) as DialogueStep.Complete
        assertEquals(DialogueOutcome.Consent(ConsentDecision.GRANTED, CaptureMethod.TAP, null, null), yes.outcome)
        val no = engine.onTap(consentSession(), TapResponse.No) as DialogueStep.Complete
        assertEquals(ConsentDecision.DECLINED, (no.outcome as DialogueOutcome.Consent).decision)
    }

    @Test
    fun `dobara batao repeats the question`() {
        val s = consentSession()
        val step = engine.onRecognition(s, say("Dobara batao")) as DialogueStep.Ask
        assertEquals(s.questionText, step.speech)
    }

    @Test
    fun `support request completes with SupportRequested`() {
        val step = engine.onRecognition(consentSession(), say("Mujhe support se baat karni hai")) as DialogueStep.Complete
        assertEquals(DialogueOutcome.SupportRequested, step.outcome)
    }

    @Test
    fun `eta question - haan is not accepted, duration is`() {
        val s = (engine.start(prompt(PromptType.ETA), null) as DialogueStep.Ask).session
        assertTrue(engine.onRecognition(s, say("Haan")) is DialogueStep.Ask)
        val done = engine.onRecognition(s, say("Ek ghanta lagega")) as DialogueStep.Complete
        assertEquals(DialogueOutcome.Eta(60, false, CaptureMethod.VOICE), done.outcome)
    }

    @Test
    fun `arrival question`() {
        val s = (engine.start(prompt(PromptType.ARRIVAL), null) as DialogueStep.Ask).session
        assertEquals(DialogueOutcome.Arrival(true, null, CaptureMethod.VOICE), (engine.onRecognition(s, say("Main pahunch gaya")) as DialogueStep.Complete).outcome)
        assertEquals(DialogueOutcome.Arrival(false, null, CaptureMethod.VOICE), (engine.onRecognition(s, say("Abhi raste mein hoon")) as DialogueStep.Complete).outcome)
    }

    @Test
    fun `loading status by voice`() {
        val s = (engine.start(prompt(PromptType.LOADING_STATUS), null) as DialogueStep.Ask).session
        val done = engine.onRecognition(s, say("loading shuru ho gayi")) as DialogueStep.Complete
        assertEquals(DialogueOutcome.Loading(LoadingStatus.LOADING_STARTED, CaptureMethod.VOICE), done.outcome)
    }

    @Test
    fun `informational prompts complete immediately`() {
        val step = engine.start(prompt(PromptType.TRIP_CANCELLED), null) as DialogueStep.Complete
        assertEquals(DialogueOutcome.Acknowledged, step.outcome)
    }
}
