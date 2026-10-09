package com.freighttiger.driverassistant.platform.speech

import javax.inject.Inject
import javax.inject.Singleton

data class SpeechCapabilityReport(
    val ttsHindiAvailable: Boolean?,
    val recognitionAvailable: Boolean,
    val microphonePermission: Boolean,
)

/** Capability checks shown during onboarding and in settings. */
@Singleton
class SpeechCapabilities @Inject constructor(
    private val tts: AndroidTextToSpeechOutput,
    private val stt: AndroidSpeechRecognizerInput,
) {
    fun report() = SpeechCapabilityReport(
        ttsHindiAvailable = tts.hindiVoiceAvailable,
        recognitionAvailable = stt.isAvailable,
        microphonePermission = stt.hasPermission(),
    )
}
