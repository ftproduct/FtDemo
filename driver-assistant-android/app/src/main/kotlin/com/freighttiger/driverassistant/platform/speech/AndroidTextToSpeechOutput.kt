package com.freighttiger.driverassistant.platform.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.freighttiger.driverassistant.domain.voice.SpeechOutput
import com.freighttiger.driverassistant.domain.voice.SpeechOutputResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Android TextToSpeech adapter (hi-IN). Requests transient, ducking audio focus while speaking so
 * music/navigation lowers instead of being interrupted. Never logs spoken text.
 */
@Singleton
class AndroidTextToSpeechOutput @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechOutput {

    private val ready = CompletableDeferred<Boolean>()
    private val pending = ConcurrentHashMap<String, CancellableContinuation<SpeechOutputResult>>()
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANT)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(attributes)
        .build()
    private lateinit var tts: TextToSpeech

    /** Whether a Hindi voice is installed. Null until the engine has initialised. */
    @Volatile var hindiVoiceAvailable: Boolean? = null
        private set

    override val isReady: Boolean get() = ready.isCompleted && hindiVoiceAvailable == true

    override suspend fun speak(text: String, languageTag: String): SpeechOutputResult {
        val ok = withTimeoutOrNull(INIT_TIMEOUT_MS) { ready.await() } ?: false
        if (!ok) return SpeechOutputResult.Failed("TTS_UNAVAILABLE")
        if (text.isBlank()) return SpeechOutputResult.Completed
        audioManager.requestAudioFocus(focusRequest)
        try {
            return suspendCancellableCoroutine { cont ->
                val id = UUID.randomUUID().toString()
                pending[id] = cont
                cont.invokeOnCancellation {
                    pending.remove(id)
                    tts.stop()
                }
                val queued = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
                if (queued != TextToSpeech.SUCCESS) {
                    pending.remove(id)
                    cont.resume(SpeechOutputResult.Failed("TTS_SPEAK_FAILED"))
                }
            }
        } finally {
            audioManager.abandonAudioFocusRequest(focusRequest)
        }
    }

    override fun stop() {
        if (ready.isCompleted) tts.stop()
        // UtteranceProgressListener.onStop resumes pending callers with Stopped.
    }

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit

        override fun onDone(utteranceId: String?) {
            utteranceId?.let { pending.remove(it)?.resumeSafely(SpeechOutputResult.Completed) }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            utteranceId?.let { pending.remove(it)?.resumeSafely(SpeechOutputResult.Failed("TTS_ERROR")) }
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            Log.w(TAG, "TTS error code $errorCode")
            utteranceId?.let { pending.remove(it)?.resumeSafely(SpeechOutputResult.Failed("TTS_ERROR_$errorCode")) }
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            utteranceId?.let { pending.remove(it)?.resumeSafely(SpeechOutputResult.Stopped) }
        }
    }

    init {
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                hindiVoiceAvailable = false
                ready.complete(false)
                return@TextToSpeech
            }
            val result = tts.setLanguage(Locale.forLanguageTag("hi-IN"))
            val ok = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            hindiVoiceAvailable = ok
            tts.setAudioAttributes(attributes)
            tts.setSpeechRate(0.95f)
            tts.setOnUtteranceProgressListener(listener)
            ready.complete(ok)
        }
    }

    private fun CancellableContinuation<SpeechOutputResult>.resumeSafely(value: SpeechOutputResult) {
        if (isActive) resume(value)
    }

    private companion object {
        const val TAG = "FtdaTts"
        const val INIT_TIMEOUT_MS = 5_000L
    }
}
