package com.freighttiger.driverassistant.platform.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.freighttiger.driverassistant.core.model.RecognitionAlternative
import com.freighttiger.driverassistant.core.model.Utterance
import com.freighttiger.driverassistant.domain.voice.RecognitionResult
import com.freighttiger.driverassistant.domain.voice.SpeechErrorKind
import com.freighttiger.driverassistant.domain.voice.SpeechInput
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * One bounded listening turn with Android's SpeechRecognizer (hi-IN, up to 3 alternatives).
 * Must be started from a user-visible screen. There is intentionally no continuous mode.
 * Transcripts are returned to the caller and never logged.
 */
@Singleton
class AndroidSpeechRecognizerInput @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechInput {

    private val main = Handler(Looper.getMainLooper())
    @Volatile private var active: SpeechRecognizer? = null

    override val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    override suspend fun listenOnce(languageTag: String): RecognitionResult {
        if (!hasPermission()) return RecognitionResult.Error(SpeechErrorKind.PERMISSION_DENIED)
        if (!isAvailable) return RecognitionResult.Error(SpeechErrorKind.UNAVAILABLE)
        return withContext(Dispatchers.Main) {
            withTimeoutOrNull(TURN_TIMEOUT_MS) { recognize(languageTag) } ?: run {
                cancel()
                RecognitionResult.Error(SpeechErrorKind.SILENCE)
            }
        }
    }

    private suspend fun recognize(languageTag: String): RecognitionResult = suspendCancellableCoroutine { cont ->
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        active = recognizer
        fun finish(result: RecognitionResult) {
            if (active === recognizer) active = null
            recognizer.destroy()
            if (cont.isActive) cont.resume(result)
        }
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit

            override fun onResults(results: Bundle?) {
                val texts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                val alternatives = texts.mapIndexed { i, text ->
                    // Engines report 0 or -1 when they have no score; treat that as "unknown".
                    val score = scores?.getOrNull(i)?.takeIf { it > 0f }
                    RecognitionAlternative(text, score)
                }
                finish(
                    if (alternatives.isEmpty() || alternatives.all { it.text.isBlank() }) {
                        RecognitionResult.Error(SpeechErrorKind.NO_MATCH)
                    } else {
                        RecognitionResult.Recognized(Utterance(alternatives))
                    },
                )
            }

            override fun onError(error: Int) = finish(RecognitionResult.Error(mapError(error)))
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        cont.invokeOnCancellation {
            main.post {
                recognizer.cancel()
                recognizer.destroy()
                if (active === recognizer) active = null
            }
        }
        recognizer.startListening(intent)
    }

    override fun cancel() {
        main.post {
            active?.cancel()
            active?.destroy()
            active = null
        }
    }

    private fun mapError(code: Int): SpeechErrorKind = when (code) {
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechErrorKind.SILENCE
        SpeechRecognizer.ERROR_NO_MATCH -> SpeechErrorKind.NO_MATCH
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechErrorKind.PERMISSION_DENIED
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> SpeechErrorKind.NETWORK
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> SpeechErrorKind.BUSY
        SpeechRecognizer.ERROR_CLIENT -> SpeechErrorKind.CANCELLED
        ERROR_LANGUAGE_NOT_SUPPORTED, ERROR_LANGUAGE_UNAVAILABLE -> SpeechErrorKind.UNAVAILABLE
        else -> SpeechErrorKind.OTHER
    }

    private companion object {
        const val TURN_TIMEOUT_MS = 15_000L
        // SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED / ERROR_LANGUAGE_UNAVAILABLE (API 31+).
        const val ERROR_LANGUAGE_NOT_SUPPORTED = 12
        const val ERROR_LANGUAGE_UNAVAILABLE = 13
    }
}
