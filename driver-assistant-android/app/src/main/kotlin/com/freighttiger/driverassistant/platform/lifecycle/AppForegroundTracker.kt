package com.freighttiger.driverassistant.platform.lifecycle

import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.freighttiger.driverassistant.data.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Whether any app UI is visible. Must be started on the main thread (Application.onCreate). */
@Singleton
class AppForegroundTracker @Inject constructor() {
    private val _isForeground = MutableStateFlow(false)
    val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    fun start() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) { _isForeground.value = true }
            override fun onStop(owner: LifecycleOwner) { _isForeground.value = false }
        })
    }
}

/**
 * Decides whether a prompt may be spoken right now. Speaking happens only while the app is visible,
 * the driver has auto-speak enabled, and the phone is not in a call or ringing. Otherwise the prompt
 * is delivered as a notification and the driver chooses when to answer.
 */
@Singleton
class AudioPolicy @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foreground: AppForegroundTracker,
    private val settings: SettingsRepository,
) {
    fun canAutoPresent(): Boolean = foreground.isForeground.value && settings.current.autoSpeakPrompts && !phoneBusy()

    fun phoneBusy(): Boolean {
        val mode = context.getSystemService(AudioManager::class.java).mode
        return mode == AudioManager.MODE_IN_CALL || mode == AudioManager.MODE_IN_COMMUNICATION || mode == AudioManager.MODE_RINGTONE
    }
}
