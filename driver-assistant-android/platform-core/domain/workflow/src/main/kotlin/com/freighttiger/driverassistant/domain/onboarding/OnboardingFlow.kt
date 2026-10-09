package com.freighttiger.driverassistant.domain.onboarding

import com.freighttiger.driverassistant.core.model.AssistantLanguage
import com.freighttiger.driverassistant.core.model.DriverProfile
import com.freighttiger.driverassistant.core.model.DriverSession
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.domain.backend.AssistantBackend
import com.freighttiger.driverassistant.domain.backend.AuthGateway
import com.freighttiger.driverassistant.domain.backend.BackendResult
import com.freighttiger.driverassistant.domain.backend.OtpChallenge
import com.freighttiger.driverassistant.domain.backend.code
import com.freighttiger.driverassistant.domain.ports.AccessTokenStore
import com.freighttiger.driverassistant.domain.ports.SessionRepository
import com.freighttiger.driverassistant.domain.ports.TimeSource
import com.freighttiger.driverassistant.domain.ports.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class OnboardingStep { WELCOME, PHONE, OTP, IDENTITY, LANGUAGE, PERMISSIONS, PRIVACY, TEST_PROMPT, DONE }

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val phoneInput: String = "",
    val normalizedPhone: String? = null,
    val challenge: OtpChallenge? = null,
    val profile: DriverProfile? = null,
    val activeTrip: Trip? = null,
    val language: AssistantLanguage = AssistantLanguage.HINDI,
    val microphoneGranted: Boolean? = null,
    val notificationsGranted: Boolean? = null,
    val privacyAccepted: Boolean = false,
    val testPromptPlayed: Boolean = false,
    val busy: Boolean = false,
    val errorCode: String? = null,
    val simulatedAuth: Boolean = false,
)

object PhoneNumbers {
    /** Accepts Indian mobile numbers with optional +91/91/0 prefix and separators. Returns 10 digits. */
    fun normalizeIndianMobile(raw: String): String? {
        var digits = raw.filter { it.isDigit() }
        if (digits.length == 12 && digits.startsWith("91")) digits = digits.substring(2)
        if (digits.length == 11 && digits.startsWith("0")) digits = digits.substring(1)
        if (digits.length != 10 || digits.first() !in '6'..'9') return null
        return digits
    }

    fun mask(tenDigits: String): String = "******" + tenDigits.takeLast(4)
}

/**
 * Onboarding state machine (UI-agnostic). Permissions are requested just in time by the UI;
 * this flow only records outcomes. Denying the microphone or notifications does not block
 * onboarding — the app falls back to tap responses / in-app prompts.
 */
class OnboardingFlow(
    private val auth: AuthGateway,
    private val backend: AssistantBackend,
    private val sessions: SessionRepository,
    private val trips: TripRepository,
    private val tokens: AccessTokenStore,
    private val clock: TimeSource,
) {
    private val _state = MutableStateFlow(OnboardingState(simulatedAuth = auth.isSimulated))
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun start() = _state.update { it.copy(step = OnboardingStep.PHONE, errorCode = null) }

    fun onPhoneChanged(input: String) = _state.update { it.copy(phoneInput = input.take(16), errorCode = null) }

    suspend fun submitPhone() {
        val phone = PhoneNumbers.normalizeIndianMobile(_state.value.phoneInput)
        if (phone == null) {
            _state.update { it.copy(errorCode = "INVALID_PHONE") }
            return
        }
        _state.update { it.copy(busy = true, errorCode = null, normalizedPhone = phone) }
        when (val r = auth.requestOtp(phone)) {
            is BackendResult.Success -> _state.update { it.copy(busy = false, challenge = r.value, step = OnboardingStep.OTP) }
            is BackendResult.Failure -> _state.update { it.copy(busy = false, errorCode = r.error.code) }
        }
    }

    suspend fun submitOtp(otp: String) {
        val s = _state.value
        val challenge = s.challenge ?: return _state.update { it.copy(errorCode = "NO_OTP_CHALLENGE") }
        val phone = s.normalizedPhone ?: return _state.update { it.copy(errorCode = "INVALID_PHONE") }
        if (otp.length != 6 || !otp.all { it.isDigit() }) {
            _state.update { it.copy(errorCode = "INVALID_OTP_FORMAT") }
            return
        }
        if (!clock.now().isBefore(challenge.expiresAt)) {
            _state.update { it.copy(errorCode = "OTP_EXPIRED", step = OnboardingStep.PHONE, challenge = null) }
            return
        }
        _state.update { it.copy(busy = true, errorCode = null) }
        when (val r = auth.verifyOtp(challenge.challengeId, phone, otp)) {
            is BackendResult.Failure -> _state.update { it.copy(busy = false, errorCode = r.error.code) }
            is BackendResult.Success -> {
                val result = r.value
                tokens.save(result.accessToken, result.expiresAt)
                sessions.save(DriverSession(result.profile, clock.now(), result.simulated))
                val trip = when (val t = backend.fetchActiveTrip(result.profile.driverId)) {
                    is BackendResult.Success -> t.value
                    is BackendResult.Failure -> null
                }
                trip?.takeIf { it.driverId == result.profile.driverId }?.let { trips.upsert(it) }
                _state.update {
                    it.copy(busy = false, profile = result.profile, activeTrip = trip, step = OnboardingStep.IDENTITY)
                }
            }
        }
    }

    fun confirmIdentity(isMe: Boolean) = _state.update {
        if (isMe) it.copy(step = OnboardingStep.LANGUAGE) else it.copy(errorCode = "IDENTITY_NOT_CONFIRMED")
    }

    fun selectLanguage(language: AssistantLanguage) = _state.update {
        if (!language.enabled) it.copy(errorCode = "LANGUAGE_NOT_AVAILABLE") else it.copy(language = language, errorCode = null)
    }

    fun confirmLanguage() = _state.update { it.copy(step = OnboardingStep.PERMISSIONS) }

    fun onMicrophonePermission(granted: Boolean) = _state.update { it.copy(microphoneGranted = granted) }

    fun onNotificationPermission(granted: Boolean) = _state.update { it.copy(notificationsGranted = granted) }

    fun continueFromPermissions() = _state.update { it.copy(step = OnboardingStep.PRIVACY) }

    fun acceptPrivacy() = _state.update { it.copy(privacyAccepted = true, step = OnboardingStep.TEST_PROMPT) }

    fun onTestPromptPlayed() = _state.update { it.copy(testPromptPlayed = true) }

    fun finish() = _state.update {
        if (!it.privacyAccepted || it.profile == null) it.copy(errorCode = "ONBOARDING_INCOMPLETE") else it.copy(step = OnboardingStep.DONE)
    }

    fun back() = _state.update {
        val prev = when (it.step) {
            OnboardingStep.PHONE -> OnboardingStep.WELCOME
            OnboardingStep.OTP -> OnboardingStep.PHONE
            OnboardingStep.LANGUAGE -> OnboardingStep.IDENTITY
            OnboardingStep.PERMISSIONS -> OnboardingStep.LANGUAGE
            OnboardingStep.PRIVACY -> OnboardingStep.PERMISSIONS
            OnboardingStep.TEST_PROMPT -> OnboardingStep.PRIVACY
            else -> it.step
        }
        it.copy(step = prev, errorCode = null)
    }
}
