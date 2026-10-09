package com.freighttiger.driverassistant.domain.voice

import com.freighttiger.driverassistant.core.model.IntentResult
import com.freighttiger.driverassistant.core.model.QuestionContext
import com.freighttiger.driverassistant.core.model.Utterance

/**
 * Maps a recognised utterance to a structured intent **for the question currently being asked**.
 *
 * The MVP ships [RuleBasedIntentClassifier]. An AI-based classifier can implement the same
 * interface later; it must keep the same safety contract:
 *  - never return CONSENT_YES/CONSENT_NO outside [QuestionContext.CONSENT];
 *  - never return CONSENT_YES for empty, hedged, deferring or conflicting input.
 */
interface IntentClassifier {
    fun classify(utterance: Utterance, context: QuestionContext): IntentResult
}
