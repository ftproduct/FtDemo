package com.freighttiger.driverassistant.domain.onboarding

import com.freighttiger.driverassistant.core.model.AssistantLanguage
import com.freighttiger.driverassistant.core.model.DriverProfile
import com.freighttiger.driverassistant.domain.backend.AuthGateway
import com.freighttiger.driverassistant.domain.backend.AuthResult
import com.freighttiger.driverassistant.domain.backend.BackendError
import com.freighttiger.driverassistant.domain.backend.BackendResult
import com.freighttiger.driverassistant.domain.backend.OtpChallenge
import com.freighttiger.driverassistant.domain.inmemory.InMemoryAccessTokenStore
import com.freighttiger.driverassistant.domain.inmemory.InMemorySessionRepository
import com.freighttiger.driverassistant.domain.inmemory.InMemoryTripRepository
import com.freighttiger.driverassistant.domain.inmemory.MutableTimeSource
import com.freighttiger.driverassistant.domain.support.FakeBackend
import com.freighttiger.driverassistant.domain.support.T0
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration

class OnboardingFlowTest {
    private val clock = MutableTimeSource(T0)
    private val auth = object : AuthGateway {
        override val isSimulated = true
        override suspend fun requestOtp(phoneNumber: String) = BackendResult.Success(OtpChallenge("ch-1", T0.plus(Duration.ofMinutes(5)), true, "123456"))
        override suspend fun verifyOtp(challengeId: String, phoneNumber: String, otp: String): BackendResult<AuthResult> =
            if (otp == "123456") BackendResult.Success(AuthResult(DriverProfile("driver-123", "Ramesh", "******3210"), "tok", T0.plus(Duration.ofDays(1)), true))
            else BackendResult.Failure(BackendError.Validation("INVALID_OTP"))
    }
    private val sessions = InMemorySessionRepository()
    private val tokens = InMemoryAccessTokenStore()
    private val flow = OnboardingFlow(auth, FakeBackend(), sessions, InMemoryTripRepository(), tokens, clock)

    @Test
    fun `first-time onboarding happy path`() = runTest {
        flow.start()
        flow.onPhoneChanged("+91 98765 43210")
        flow.submitPhone()
        assertEquals(OnboardingStep.OTP, flow.state.value.step)
        assertEquals("9876543210", flow.state.value.normalizedPhone)
        flow.submitOtp("123456")
        assertEquals(OnboardingStep.IDENTITY, flow.state.value.step)
        assertNotNull(sessions.current())
        assertEquals("tok", tokens.current())
        flow.confirmIdentity(true)
        flow.selectLanguage(AssistantLanguage.HINDI)
        flow.confirmLanguage()
        flow.onMicrophonePermission(false) // denial does not block onboarding
        flow.onNotificationPermission(true)
        flow.continueFromPermissions()
        flow.acceptPrivacy()
        flow.onTestPromptPlayed()
        flow.finish()
        assertEquals(OnboardingStep.DONE, flow.state.value.step)
    }

    @Test
    fun `invalid phone and wrong otp are reported`() = runTest {
        flow.start()
        flow.onPhoneChanged("12345")
        flow.submitPhone()
        assertEquals("INVALID_PHONE", flow.state.value.errorCode)
        flow.onPhoneChanged("9876543210")
        flow.submitPhone()
        flow.submitOtp("000000")
        assertEquals("INVALID_OTP", flow.state.value.errorCode)
        assertNull(sessions.current())
    }

    @Test
    fun `expired otp returns to phone step`() = runTest {
        flow.start(); flow.onPhoneChanged("9876543210"); flow.submitPhone()
        clock.advance(Duration.ofMinutes(6))
        flow.submitOtp("123456")
        assertEquals(OnboardingStep.PHONE, flow.state.value.step)
        assertEquals("OTP_EXPIRED", flow.state.value.errorCode)
    }

    @Test
    fun `disabled language cannot be selected`() {
        flow.selectLanguage(AssistantLanguage.TAMIL)
        assertEquals("LANGUAGE_NOT_AVAILABLE", flow.state.value.errorCode)
        assertEquals(AssistantLanguage.HINDI, flow.state.value.language)
    }

    @Test
    fun `phone normalisation`() {
        assertEquals("9876543210", PhoneNumbers.normalizeIndianMobile("09876543210"))
        assertNull(PhoneNumbers.normalizeIndianMobile("5876543210"))
        assertEquals("******3210", PhoneNumbers.mask("9876543210"))
    }
}
