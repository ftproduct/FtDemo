package com.freighttiger.driverassistant.core.model

enum class AssistantIntent {
    CONSENT_YES,
    CONSENT_NO,
    ETA_REPORTED,
    ARRIVAL_YES,
    ARRIVAL_NO,
    LOADING_STATUS_REPORTED,
    REPEAT_PROMPT,
    REQUEST_HUMAN_SUPPORT,
    UNKNOWN,
}

/** The question currently being asked. Intent interpretation always depends on it. */
enum class QuestionContext { CONSENT, ETA, ARRIVAL, LOADING_STATUS, GENERAL }

enum class UnknownReason { EMPTY, AMBIGUOUS, CONFLICTING, OUT_OF_CONTEXT, UNPARSEABLE, LOW_CONFIDENCE }

data class RecognitionAlternative(val text: String, val confidence: Float? = null)

/** Output of a speech recogniser: n-best alternatives, best first. */
data class Utterance(val alternatives: List<RecognitionAlternative>) {
    constructor(text: String, confidence: Float? = null) : this(listOf(RecognitionAlternative(text, confidence)))

    val best: RecognitionAlternative? get() = alternatives.firstOrNull()
}

data class IntentResult(
    val intent: AssistantIntent,
    /** Combined rule and recogniser confidence in [0, 1]. */
    val confidence: Float,
    val normalizedText: String,
    val etaMinutes: Int? = null,
    val etaApproximate: Boolean = false,
    val loadingStatus: LoadingStatus? = null,
    /** Driver asked to be asked later ("abhi nahi", "baad mein"). Never a decision. */
    val deferRequested: Boolean = false,
    val unknownReason: UnknownReason? = null,
)
