package com.freighttiger.driverassistant.domain.voice

import com.freighttiger.driverassistant.core.model.AssistantIntent
import com.freighttiger.driverassistant.core.model.AssistantIntent.ARRIVAL_NO
import com.freighttiger.driverassistant.core.model.AssistantIntent.ARRIVAL_YES
import com.freighttiger.driverassistant.core.model.AssistantIntent.CONSENT_NO
import com.freighttiger.driverassistant.core.model.AssistantIntent.CONSENT_YES
import com.freighttiger.driverassistant.core.model.AssistantIntent.ETA_REPORTED
import com.freighttiger.driverassistant.core.model.AssistantIntent.LOADING_STATUS_REPORTED
import com.freighttiger.driverassistant.core.model.AssistantIntent.REPEAT_PROMPT
import com.freighttiger.driverassistant.core.model.AssistantIntent.REQUEST_HUMAN_SUPPORT
import com.freighttiger.driverassistant.core.model.AssistantIntent.UNKNOWN
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.QuestionContext
import com.freighttiger.driverassistant.core.model.QuestionContext.ARRIVAL
import com.freighttiger.driverassistant.core.model.QuestionContext.CONSENT
import com.freighttiger.driverassistant.core.model.QuestionContext.ETA
import com.freighttiger.driverassistant.core.model.QuestionContext.GENERAL
import com.freighttiger.driverassistant.core.model.QuestionContext.LOADING_STATUS
import com.freighttiger.driverassistant.core.model.RecognitionAlternative
import com.freighttiger.driverassistant.core.model.UnknownReason
import com.freighttiger.driverassistant.core.model.Utterance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleBasedIntentClassifierTest {
    private val classifier = RuleBasedIntentClassifier()
    private fun intent(text: String, context: QuestionContext): AssistantIntent = classifier.classify(Utterance(text), context).intent

    // --- Affirmative / negative in the consent context -------------------------------------

    @Test fun `haan to a consent question is CONSENT_YES`() = assertEquals(CONSENT_YES, intent("Haan", CONSENT))
    @Test fun `ji haan is CONSENT_YES`() = assertEquals(CONSENT_YES, intent("Ji haan", CONSENT))
    @Test fun `haan main sahmat hoon is CONSENT_YES`() = assertEquals(CONSENT_YES, intent("Haan, main sahmat hoon", CONSENT))
    @Test fun `devanagari haan is CONSENT_YES`() = assertEquals(CONSENT_YES, intent("हाँ जी", CONSENT))
    @Test fun `nahi is CONSENT_NO`() = assertEquals(CONSENT_NO, intent("Nahi", CONSENT))
    @Test fun `devanagari nahi is CONSENT_NO`() = assertEquals(CONSENT_NO, intent("नहीं", CONSENT))
    @Test fun `sahmat nahi hoon is CONSENT_NO not yes`() = assertEquals(CONSENT_NO, intent("Main sahmat nahi hoon", CONSENT))
    @Test fun `mujhe tracking nahi chahiye is CONSENT_NO`() = assertEquals(CONSENT_NO, intent("Mujhe tracking nahi chahiye", CONSENT))

    // --- Things that must never become consent ----------------------------------------------

    @Test
    fun `abhi nahi is not consent and is a deferral`() {
        val r = classifier.classify(Utterance("Abhi nahi"), CONSENT)
        assertNotEquals(CONSENT_YES, r.intent)
        assertEquals(UNKNOWN, r.intent)
        assertTrue(r.deferRequested)
    }

    @Test fun `silence is unknown`() = assertEquals(UnknownReason.EMPTY, classifier.classify(Utterance(emptyList()), CONSENT).unknownReason)
    @Test fun `blank transcript is unknown`() = assertEquals(UNKNOWN, intent("   ", CONSENT))
    @Test fun `weak agreement theek hai is not consent`() = assertEquals(UNKNOWN, intent("Theek hai", CONSENT))
    @Test fun `ok is not consent`() = assertEquals(UNKNOWN, intent("ok", CONSENT))
    @Test fun `hedge shayad haan is not consent`() = assertEquals(UNKNOWN, intent("Shayad haan", CONSENT))
    @Test fun `pata nahi is ambiguous not decline`() = assertEquals(UnknownReason.AMBIGUOUS, classifier.classify(Utterance("pata nahi"), CONSENT).unknownReason)
    @Test fun `mixed haan nahi is conflicting`() = assertEquals(UnknownReason.CONFLICTING, classifier.classify(Utterance("haan... nahi nahi"), CONSENT).unknownReason)
    @Test fun `eta answer to consent question is out of context`() =
        assertEquals(UnknownReason.OUT_OF_CONTEXT, classifier.classify(Utterance("ek ghanta"), CONSENT).unknownReason)
    @Test fun `random speech is unknown`() = assertEquals(UNKNOWN, intent("chai pi raha hoon", CONSENT))

    @Test
    fun `positive response to ETA question is never consent`() {
        assertNotEquals(CONSENT_YES, intent("Haan", ETA))
        assertEquals(UNKNOWN, intent("Haan", ETA))
    }

    @Test
    fun `consent intents are never produced outside the consent context`() {
        val inputs = listOf("Haan", "Ji haan", "Haan main sahmat hoon", "Nahi", "bilkul")
        for (context in listOf(ETA, ARRIVAL, LOADING_STATUS, GENERAL)) {
            for (text in inputs) {
                val i = intent(text, context)
                assertTrue("$text in $context gave $i", i != CONSENT_YES && i != CONSENT_NO)
            }
        }
    }

    @Test
    fun `consent uses only the top recogniser alternative`() {
        val u = Utterance(listOf(RecognitionAlternative("chai", 0.6f), RecognitionAlternative("haan", 0.3f)))
        assertEquals(UNKNOWN, classifier.classify(u, CONSENT).intent)
    }

    @Test
    fun `recogniser confidence lowers combined confidence`() {
        val r = classifier.classify(Utterance("haan", 0.5f), CONSENT)
        assertEquals(CONSENT_YES, r.intent)
        assertTrue(r.confidence < 0.6f)
    }

    // --- ETA ---------------------------------------------------------------------------------

    @Test
    fun `ek ghanta lagega is ETA_REPORTED with 60 minutes`() {
        val r = classifier.classify(Utterance("Ek ghanta lagega"), ETA)
        assertEquals(ETA_REPORTED, r.intent)
        assertEquals(60, r.etaMinutes)
    }

    @Test fun `do ghante is 120`() = assertEquals(120, classifier.classify(Utterance("Do ghante"), ETA).etaMinutes)
    @Test fun `already arrived during eta question`() = assertEquals(ARRIVAL_YES, intent("Main pahunch gaya", ETA))
    @Test fun `abhi nahi to ETA is a deferral`() = assertTrue(classifier.classify(Utterance("abhi nahi"), ETA).deferRequested)
    @Test fun `unparseable eta is unknown`() = assertEquals(UnknownReason.UNPARSEABLE, classifier.classify(Utterance("thodi der"), ETA).unknownReason)
    @Test fun `eta falls back to second alternative`() {
        val u = Utterance(listOf(RecognitionAlternative("ek gaana", 0.6f), RecognitionAlternative("ek ghanta", 0.5f)))
        val r = classifier.classify(u, ETA)
        assertEquals(ETA_REPORTED, r.intent)
        assertEquals(60, r.etaMinutes)
    }

    // --- Arrival -----------------------------------------------------------------------------

    @Test fun `main pahunch gaya is ARRIVAL_YES`() = assertEquals(ARRIVAL_YES, intent("Main pahunch gaya", ARRIVAL))
    @Test fun `haan to arrival question is ARRIVAL_YES`() = assertEquals(ARRIVAL_YES, intent("Haan", ARRIVAL))
    @Test fun `abhi raste mein hoon is ARRIVAL_NO`() = assertEquals(ARRIVAL_NO, intent("Abhi raste mein hoon", ARRIVAL))
    @Test fun `abhi nahi to arrival question is ARRIVAL_NO`() = assertEquals(ARRIVAL_NO, intent("Abhi nahi", ARRIVAL))
    @Test fun `pahunch raha hoon is not arrival`() = assertEquals(ARRIVAL_NO, intent("bas pahunch raha hoon", ARRIVAL))
    @Test fun `nahi with duration carries eta`() {
        val r = classifier.classify(Utterance("Nahi, aadha ghanta aur"), ARRIVAL)
        assertEquals(ARRIVAL_NO, r.intent)
        assertEquals(30, r.etaMinutes)
    }

    // --- Loading -----------------------------------------------------------------------------

    @Test fun `loading shuru ho gayi`() = assertLoading("Loading shuru ho gayi", LoadingStatus.LOADING_STARTED)
    @Test fun `loading ho gayi is completed`() = assertLoading("loading ho gayi", LoadingStatus.LOADING_COMPLETED)
    @Test fun `intezaar kar raha hoon is waiting`() = assertLoading("intezaar kar raha hoon", LoadingStatus.WAITING_FOR_LOADING)
    @Test fun `loading shuru nahi hui is waiting`() = assertLoading("loading shuru nahi hui", LoadingStatus.WAITING_FOR_LOADING)
    @Test fun `dikkat hai is issue`() = assertLoading("gaadi mein dikkat hai", LoadingStatus.ISSUE_REPORTED)

    private fun assertLoading(text: String, expected: LoadingStatus) {
        val r = classifier.classify(Utterance(text), LOADING_STATUS)
        assertEquals(LOADING_STATUS_REPORTED, r.intent)
        assertEquals(expected, r.loadingStatus)
    }

    // --- Cross-context ---------------------------------------------------------------------------

    @Test fun `dobara batao is REPEAT_PROMPT`() = assertEquals(REPEAT_PROMPT, intent("Dobara batao", CONSENT))
    @Test fun `samajh nahi aaya is repeat not decline`() = assertEquals(REPEAT_PROMPT, intent("samajh nahi aaya", CONSENT))
    @Test fun `support request in every context`() {
        for (c in QuestionContext.entries) assertEquals(REQUEST_HUMAN_SUPPORT, intent("Mujhe support se baat karni hai", c))
    }
    @Test fun `yes without a question is not interpreted`() = assertEquals(UnknownReason.OUT_OF_CONTEXT, classifier.classify(Utterance("haan"), GENERAL).unknownReason)
}
