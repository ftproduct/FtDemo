package com.freighttiger.driverassistant

import com.freighttiger.driverassistant.core.model.Utterance
import com.freighttiger.driverassistant.di.PlatformBindings
import com.freighttiger.driverassistant.domain.ports.ConnectivityMonitor
import com.freighttiger.driverassistant.domain.voice.RecognitionResult
import com.freighttiger.driverassistant.domain.voice.SpeechErrorKind
import com.freighttiger.driverassistant.domain.voice.SpeechInput
import com.freighttiger.driverassistant.domain.voice.SpeechOutput
import com.freighttiger.driverassistant.domain.voice.SpeechOutputResult
import com.freighttiger.driverassistant.domain.workflow.SyncTrigger
import com.freighttiger.driverassistant.platform.connectivity.AndroidConnectivityMonitor
import com.freighttiger.driverassistant.platform.sync.SyncScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/** Speech output that completes immediately and records what was "spoken". */
@Singleton
class FakeSpeechOutput @Inject constructor() : SpeechOutput {
    val spoken = ConcurrentLinkedQueue<String>()
    override val isReady = true
    override suspend fun speak(text: String, languageTag: String): SpeechOutputResult {
        spoken += text
        return SpeechOutputResult.Completed
    }
    override fun stop() = Unit
}

/** Scripted recogniser: each listening turn returns the next queued answer (or silence). */
@Singleton
class FakeSpeechInput @Inject constructor() : SpeechInput {
    val script = ConcurrentLinkedQueue<RecognitionResult>()
    override val isAvailable = true
    override suspend fun listenOnce(languageTag: String): RecognitionResult =
        script.poll() ?: RecognitionResult.Error(SpeechErrorKind.SILENCE)
    override fun cancel() = Unit

    fun say(vararg texts: String) = texts.forEach { script += RecognitionResult.Recognized(Utterance(it)) }
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [PlatformBindings::class])
abstract class TestPlatformBindings {
    @Binds abstract fun speechOutput(impl: FakeSpeechOutput): SpeechOutput
    @Binds abstract fun speechInput(impl: FakeSpeechInput): SpeechInput
    @Binds abstract fun connectivity(impl: AndroidConnectivityMonitor): ConnectivityMonitor
    @Binds abstract fun syncTrigger(impl: SyncScheduler): SyncTrigger
}
