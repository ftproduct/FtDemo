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
import com.freighttiger.driverassistant.core.model.IntentResult
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.QuestionContext
import com.freighttiger.driverassistant.core.model.UnknownReason
import com.freighttiger.driverassistant.core.model.Utterance

/**
 * Deterministic, context-aware intent rules for common Hindi/Hinglish answers.
 *
 * Design rules:
 *  - Support and repeat requests are recognised in every context.
 *  - Consent intents are produced only in the CONSENT context and only from explicit words.
 *    Weak agreement ("theek hai", "ok"), hedges ("shayad"), deferrals ("abhi nahi") and mixed
 *    yes/no are UNKNOWN so the conversation asks again.
 *  - For consent only the top recognition alternative is used; other contexts may fall back to
 *    lower-ranked alternatives (with reduced confidence) when the top one is not understood.
 */
class RuleBasedIntentClassifier(
    private val etaParser: EtaParser = EtaParser(),
) : IntentClassifier {

    override fun classify(utterance: Utterance, context: QuestionContext): IntentResult {
        val alternatives = utterance.alternatives.filter { it.text.isNotBlank() }
        if (alternatives.isEmpty()) {
            return IntentResult(UNKNOWN, 0f, "", unknownReason = UnknownReason.EMPTY)
        }
        val candidates = if (context == QuestionContext.CONSENT) alternatives.take(1) else alternatives.take(3)
        var first: IntentResult? = null
        candidates.forEachIndexed { index, alt ->
            val normalized = HindiTextNormalizer.normalize(alt.text)
            val ruled = classifyNormalized(normalized, context)
            val recognizerConfidence = alt.confidence?.coerceIn(0f, 1f) ?: 1f
            val rankPenalty = if (index == 0) 1f else 0.8f
            val result = ruled.copy(confidence = ruled.confidence * recognizerConfidence * rankPenalty)
            if (first == null) first = result
            if (result.intent != UNKNOWN || result.deferRequested) return result
        }
        return first!!
    }

    internal fun classifyNormalized(text: String, context: QuestionContext): IntentResult {
        val tokens = text.split(' ').filter { it.isNotBlank() }
        if (tokens.isEmpty()) return unknown(text, UnknownReason.EMPTY)

        if (tokens.hasAny(SUPPORT_PHRASES)) return IntentResult(REQUEST_HUMAN_SUPPORT, 0.9f, text)
        if (tokens.hasAny(REPEAT_PHRASES)) return IntentResult(REPEAT_PROMPT, 0.9f, text)

        return when (context) {
            QuestionContext.CONSENT -> consent(text, tokens)
            QuestionContext.ETA -> eta(text, tokens)
            QuestionContext.ARRIVAL -> arrival(text, tokens)
            QuestionContext.LOADING_STATUS -> loading(text, tokens)
            QuestionContext.GENERAL -> general(text, tokens)
        }
    }

    private fun consent(text: String, tokens: List<String>): IntentResult {
        if (tokens.hasAny(HEDGE_PHRASES)) return unknown(text, UnknownReason.AMBIGUOUS)
        if (tokens.hasAny(DEFER_PHRASES)) return unknown(text, UnknownReason.AMBIGUOUS, defer = true)
        if (tokens.hasAny(EXPLICIT_DECLINE_PHRASES)) {
            // "sahmat nahi", "manzoor nahi" — a negated agreement word is a decline, unless the
            // driver also said a separate affirmative ("haan ... sahmat nahi") which is conflicting.
            return if (tokens.hasAnyOf(listOf("haan"), listOf("bilkul"), listOf("zaroor"))) {
                unknown(text, UnknownReason.CONFLICTING)
            } else {
                IntentResult(CONSENT_NO, 0.9f, text)
            }
        }
        val negative = tokens.hasAny(NEGATION_PHRASES)
        val strong = tokens.hasAny(STRONG_AFFIRM_PHRASES)
        val weak = tokens.hasAny(WEAK_AFFIRM_PHRASES)
        return when {
            negative && (strong || weak) -> unknown(text, UnknownReason.CONFLICTING)
            negative -> IntentResult(CONSENT_NO, 0.9f, text)
            strong -> IntentResult(CONSENT_YES, 0.9f, text)
            weak -> unknown(text, UnknownReason.LOW_CONFIDENCE)
            etaParser.parse(text) != null || tokens.hasAny(ARRIVED_PHRASES) -> unknown(text, UnknownReason.OUT_OF_CONTEXT)
            else -> unknown(text, UnknownReason.UNPARSEABLE)
        }
    }

    private fun eta(text: String, tokens: List<String>): IntentResult {
        if (tokens.hasAny(ARRIVED_PHRASES) && !tokens.hasAny(NOT_ARRIVED_PHRASES)) {
            return IntentResult(ARRIVAL_YES, 0.8f, text)
        }
        val estimate = etaParser.parse(text)
        if (estimate != null && !tokens.hasAny(HEDGE_PHRASES)) {
            return IntentResult(
                ETA_REPORTED,
                if (estimate.approximate) 0.7f else 0.9f,
                text,
                etaMinutes = estimate.minutes,
                etaApproximate = estimate.approximate,
            )
        }
        if (tokens.hasAny(DEFER_PHRASES)) return unknown(text, UnknownReason.AMBIGUOUS, defer = true)
        // A bare "haan"/"nahi" does not answer "how long?" — and is never consent here.
        return unknown(text, if (estimate != null) UnknownReason.AMBIGUOUS else UnknownReason.UNPARSEABLE)
    }

    private fun arrival(text: String, tokens: List<String>): IntentResult {
        if (tokens.hasAny(HEDGE_PHRASES)) return unknown(text, UnknownReason.AMBIGUOUS)
        val estimate = etaParser.parse(text)
        val arrived = tokens.hasAny(ARRIVED_PHRASES)
        val notArrived = tokens.hasAny(NOT_ARRIVED_PHRASES) || tokens.containsPhrase(listOf("abhi", "nahi"))
        val negative = tokens.hasAny(NEGATION_PHRASES)
        val affirmative = tokens.hasAny(STRONG_AFFIRM_PHRASES)
        return when {
            notArrived -> IntentResult(ARRIVAL_NO, 0.85f, text, etaMinutes = estimate?.minutes, etaApproximate = estimate?.approximate ?: false)
            arrived && negative -> unknown(text, UnknownReason.CONFLICTING)
            arrived -> IntentResult(ARRIVAL_YES, 0.9f, text)
            tokens.hasAny(DEFER_PHRASES) -> unknown(text, UnknownReason.AMBIGUOUS, defer = true)
            affirmative && negative -> unknown(text, UnknownReason.CONFLICTING)
            negative -> IntentResult(ARRIVAL_NO, 0.85f, text, etaMinutes = estimate?.minutes, etaApproximate = estimate?.approximate ?: false)
            affirmative -> IntentResult(ARRIVAL_YES, 0.8f, text)
            estimate != null -> IntentResult(ARRIVAL_NO, 0.75f, text, etaMinutes = estimate.minutes, etaApproximate = estimate.approximate)
            else -> unknown(text, UnknownReason.UNPARSEABLE)
        }
    }

    private fun loading(text: String, tokens: List<String>): IntentResult {
        val status = loadingStatus(tokens)
            ?: return if (tokens.hasAny(DEFER_PHRASES)) {
                unknown(text, UnknownReason.AMBIGUOUS, defer = true)
            } else {
                unknown(text, UnknownReason.UNPARSEABLE)
            }
        return IntentResult(LOADING_STATUS_REPORTED, 0.85f, text, loadingStatus = status)
    }

    private fun general(text: String, tokens: List<String>): IntentResult {
        loadingStatus(tokens)?.let { status ->
            if (status != LoadingStatus.REACHED_LOADING_POINT) {
                return IntentResult(LOADING_STATUS_REPORTED, 0.75f, text, loadingStatus = status)
            }
        }
        if (tokens.hasAny(ARRIVED_PHRASES) && !tokens.hasAny(NOT_ARRIVED_PHRASES)) {
            return IntentResult(ARRIVAL_YES, 0.75f, text)
        }
        etaParser.parse(text)?.let {
            return IntentResult(ETA_REPORTED, 0.7f, text, etaMinutes = it.minutes, etaApproximate = it.approximate)
        }
        // Yes/No without an active question is never interpreted (and never as consent).
        return unknown(text, UnknownReason.OUT_OF_CONTEXT)
    }

    private fun loadingStatus(tokens: List<String>): LoadingStatus? = when {
        tokens.hasAny(LOADING_COMPLETED_PHRASES) -> LoadingStatus.LOADING_COMPLETED
        tokens.hasAny(LOADING_NOT_STARTED_PHRASES) -> LoadingStatus.WAITING_FOR_LOADING
        tokens.hasAny(LOADING_STARTED_PHRASES) -> LoadingStatus.LOADING_STARTED
        tokens.hasAny(ISSUE_PHRASES) -> LoadingStatus.ISSUE_REPORTED
        tokens.hasAny(WAITING_PHRASES) -> LoadingStatus.WAITING_FOR_LOADING
        tokens.hasAny(ARRIVED_PHRASES) && !tokens.hasAny(NOT_ARRIVED_PHRASES) -> LoadingStatus.REACHED_LOADING_POINT
        else -> null
    }

    private fun unknown(text: String, reason: UnknownReason, defer: Boolean = false) =
        IntentResult(UNKNOWN, 0f, text, deferRequested = defer, unknownReason = reason)

    private companion object {
        fun p(vararg words: String) = words.toList()

        val SUPPORT_PHRASES = listOf(
            p("support"), p("madad"), p("sahayata"), p("baat", "karni"), p("baat", "karna"),
            p("baat", "karao"), p("call", "karo"), p("phone", "karo"), p("customer", "care"),
            p("manager"), p("insaan"), p("operator"),
        )
        val REPEAT_PHRASES = listOf(
            p("dobara"), p("phir", "se"), p("phir", "bolo"), p("phir", "batao"), p("repeat"),
            p("kya", "bola"), p("kya", "kaha"), p("samajh", "nahi"), p("suna", "nahi"), p("sunai", "nahi"),
            p("ek", "baar", "aur"),
        )
        val HEDGE_PHRASES = listOf(
            p("shayad"), p("pata", "nahi"), p("maloom", "nahi"), p("sochna"), p("soch"),
            p("dekhte", "hain"), p("dekhta", "hoon"), p("kya", "matlab"), p("kyun"), p("kyon"), p("maybe"),
        )
        val DEFER_PHRASES = listOf(
            p("abhi", "nahi"), p("baad", "mein"), p("baad"), p("thodi", "der", "baad"), p("busy"),
            p("chala", "raha"), p("driving"), p("gaadi", "chala"), p("later"),
        )
        val EXPLICIT_DECLINE_PHRASES = listOf(
            p("sahmat", "nahi"), p("nahi", "sahmat"), p("manzoor", "nahi"), p("nahi", "chahiye"),
            p("mat", "karo"), p("track", "mat"), p("nahi", "karna"), p("anumati", "nahi"),
            p("nahi", "deta"), p("nahi", "dunga"), p("mana", "karta"), p("theek", "nahi"),
        )
        val NEGATION_PHRASES = listOf(p("nahi"), p("na"), p("mat"), p("mana"), p("inkaar"))
        val STRONG_AFFIRM_PHRASES = listOf(
            p("haan"), p("sahmat"), p("bilkul"), p("manzoor"), p("zaroor"), p("anumati", "deta"),
            p("anumati", "hai"),
        )
        val WEAK_AFFIRM_PHRASES = listOf(p("theek"), p("ok"), p("chalega"), p("ji"), p("accha"), p("acha"))
        val ARRIVED_PHRASES = listOf(
            p("pahunch", "gaya"), p("pahunch", "chuka"), p("pahuncha"), p("aa", "gaya"),
            p("reach", "ho", "gaya"), p("reached"), p("arrived"), p("gate", "par"),
        )
        val NOT_ARRIVED_PHRASES = listOf(
            p("raste"), p("pahunch", "raha"), p("pahunchne", "wala"), p("pahunchunga"), p("aa", "raha"),
            p("chal", "raha"), p("nahi", "pahuncha"), p("pahuncha", "nahi"), p("door"),
        )
        val LOADING_COMPLETED_PHRASES = listOf(
            p("loading", "ho", "gaya"), p("loading", "khatam"), p("loading", "poori"), p("load", "ho", "gaya"),
            p("loading", "complete"), p("loading", "done"),
        )
        val LOADING_NOT_STARTED_PHRASES = listOf(p("shuru", "nahi"), p("loading", "nahi"), p("number", "nahi"))
        val LOADING_STARTED_PHRASES = listOf(
            p("loading", "shuru"), p("loading", "chal"), p("load", "ho", "raha"), p("loading", "ho", "raha"),
            p("loading", "start"),
        )
        val ISSUE_PHRASES = listOf(
            p("samasya"), p("dikkat"), p("pareshani"), p("problem"), p("issue"), p("deri"),
            p("der", "ho"), p("late"), p("kharab"), p("puncture"), p("jam"),
        )
        val WAITING_PHRASES = listOf(
            p("intezaar"), p("wait"), p("ruka"), p("line", "mein"), p("khada"), p("khade"),
        )
    }
}

internal fun List<String>.containsPhrase(phrase: List<String>): Boolean {
    if (phrase.isEmpty() || phrase.size > size) return false
    for (start in 0..size - phrase.size) {
        var match = true
        for (offset in phrase.indices) {
            if (this[start + offset] != phrase[offset]) {
                match = false
                break
            }
        }
        if (match) return true
    }
    return false
}

internal fun List<String>.hasAny(phrases: List<List<String>>): Boolean = phrases.any { containsPhrase(it) }

internal fun List<String>.hasAnyOf(vararg phrases: List<String>): Boolean = phrases.any { containsPhrase(it) }
