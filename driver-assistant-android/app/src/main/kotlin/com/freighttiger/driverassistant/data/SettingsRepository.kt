package com.freighttiger.driverassistant.data

import android.content.Context
import com.freighttiger.driverassistant.domain.workflow.PromptPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

data class AssistantSettings(
    val onboardingComplete: Boolean = false,
    val autoSpeakPrompts: Boolean = true,
    val nonUrgentPaused: Boolean = false,
    val deferMinutes: Int = 10,
    val cooldownMinutes: Int = 10,
) {
    fun toPromptPolicy() = PromptPolicy(
        cooldown = Duration.ofMinutes(cooldownMinutes.toLong()),
        deferDuration = Duration.ofMinutes(deferMinutes.toLong()),
        nonUrgentPaused = nonUrgentPaused,
    )
}

/** Non-sensitive driver preferences (no tokens, no personal data). */
@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("ftda_settings", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<AssistantSettings> = _settings.asStateFlow()
    val current: AssistantSettings get() = _settings.value

    fun update(transform: (AssistantSettings) -> AssistantSettings) {
        val next = transform(_settings.value)
        prefs.edit()
            .putBoolean(K_ONBOARDED, next.onboardingComplete)
            .putBoolean(K_AUTO_SPEAK, next.autoSpeakPrompts)
            .putBoolean(K_PAUSED, next.nonUrgentPaused)
            .putInt(K_DEFER, next.deferMinutes)
            .putInt(K_COOLDOWN, next.cooldownMinutes)
            .apply()
        _settings.value = next
    }

    private fun read() = AssistantSettings(
        onboardingComplete = prefs.getBoolean(K_ONBOARDED, false),
        autoSpeakPrompts = prefs.getBoolean(K_AUTO_SPEAK, true),
        nonUrgentPaused = prefs.getBoolean(K_PAUSED, false),
        deferMinutes = prefs.getInt(K_DEFER, 10),
        cooldownMinutes = prefs.getInt(K_COOLDOWN, 10),
    )

    private companion object {
        const val K_ONBOARDED = "onboarding_complete"
        const val K_AUTO_SPEAK = "auto_speak"
        const val K_PAUSED = "non_urgent_paused"
        const val K_DEFER = "defer_minutes"
        const val K_COOLDOWN = "cooldown_minutes"
    }
}
