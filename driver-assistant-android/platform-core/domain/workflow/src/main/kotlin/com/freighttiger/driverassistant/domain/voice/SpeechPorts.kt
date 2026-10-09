package com.freighttiger.driverassistant.domain.voice

import com.freighttiger.driverassistant.core.model.Utterance

enum class SpeechErrorKind {
    /** Nothing was said before the recogniser timed out. Never interpreted as an answer. */
    SILENCE,
    NO_MATCH,
    PERMISSION_DENIED,
    UNAVAILABLE,
    NETWORK,
    BUSY,
    CANCELLED,
    OTHER,
}

sealed interface RecognitionResult {
    data class Recognized(val utterance: Utterance) : RecognitionResult
    data class Error(val kind: SpeechErrorKind) : RecognitionResult
}

/**
 * Speech recognition provider (Android SpeechRecognizer today, replaceable later).
 * Implementations must only listen for a single bounded turn after an explicit, user-visible
 * request. There is no continuous / background listening API by design.
 */
interface SpeechInput {
    val isAvailable: Boolean
    suspend fun listenOnce(languageTag: String): RecognitionResult
    fun cancel()
}

sealed interface SpeechOutputResult {
    data object Completed : SpeechOutputResult
    data object Stopped : SpeechOutputResult
    data class Failed(val reason: String) : SpeechOutputResult
}

/** Text-to-speech provider. */
interface SpeechOutput {
    val isReady: Boolean
    suspend fun speak(text: String, languageTag: String): SpeechOutputResult
    fun stop()
}
