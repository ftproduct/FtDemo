package com.freighttiger.driverassistant.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freighttiger.driverassistant.AppConfig
import com.freighttiger.driverassistant.core.model.AssistantLanguage
import com.freighttiger.driverassistant.data.SettingsRepository
import com.freighttiger.driverassistant.domain.onboarding.OnboardingFlow
import com.freighttiger.driverassistant.domain.onboarding.OnboardingState
import com.freighttiger.driverassistant.domain.onboarding.OnboardingStep
import com.freighttiger.driverassistant.domain.voice.PromptCatalog
import com.freighttiger.driverassistant.domain.voice.SpeechOutput
import com.freighttiger.driverassistant.domain.voice.SpeechOutputResult
import com.freighttiger.driverassistant.platform.push.PushTokenRegistrar
import com.freighttiger.driverassistant.platform.speech.SpeechCapabilities
import com.freighttiger.driverassistant.platform.speech.SpeechCapabilityReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val flow: OnboardingFlow,
    private val speech: SpeechOutput,
    private val catalog: PromptCatalog,
    private val capabilities: SpeechCapabilities,
    private val settings: SettingsRepository,
    private val registrar: PushTokenRegistrar,
    config: AppConfig,
) : ViewModel() {
    val state: StateFlow<OnboardingState> = flow.state
    val demoMode = config.demoMode

    private val _caps = MutableStateFlow(capabilities.report())
    val caps: StateFlow<SpeechCapabilityReport> = _caps.asStateFlow()

    private val _speaking = MutableStateFlow(false)
    val speaking: StateFlow<Boolean> = _speaking.asStateFlow()

    private val _ttsFailed = MutableStateFlow(false)
    val ttsFailed: StateFlow<Boolean> = _ttsFailed.asStateFlow()

    fun start() = flow.start()
    fun back() = flow.back()
    fun onPhoneChanged(value: String) = flow.onPhoneChanged(value)
    fun submitPhone() { viewModelScope.launch { flow.submitPhone() } }
    fun submitOtp(otp: String) { viewModelScope.launch { flow.submitOtp(otp) } }
    fun confirmIdentity(isMe: Boolean) = flow.confirmIdentity(isMe)
    fun selectLanguage(language: AssistantLanguage) = flow.selectLanguage(language)
    fun confirmLanguage() { flow.confirmLanguage(); refreshCapabilities() }

    fun onMicrophoneResult(granted: Boolean) {
        flow.onMicrophonePermission(granted)
        refreshCapabilities()
    }

    fun onNotificationResult(granted: Boolean) = flow.onNotificationPermission(granted)
    fun refreshCapabilities() { _caps.value = capabilities.report() }
    fun continueFromPermissions() = flow.continueFromPermissions()
    fun acceptPrivacy() = flow.acceptPrivacy()

    fun playTestPrompt() {
        viewModelScope.launch {
            _speaking.value = true
            val result = speech.speak(catalog.testPrompt(), catalog.languageTag)
            _speaking.value = false
            _ttsFailed.value = result is SpeechOutputResult.Failed
            refreshCapabilities()
            flow.onTestPromptPlayed()
        }
    }

    fun finish(onDone: () -> Unit) {
        flow.finish()
        if (flow.state.value.step == OnboardingStep.DONE) {
            settings.update { it.copy(onboardingComplete = true) }
            registrar.refresh()
            onDone()
        }
    }

    override fun onCleared() {
        speech.stop()
    }
}
