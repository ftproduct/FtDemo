package com.freighttiger.driverassistant.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.selection.selectable
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.core.model.AssistantLanguage
import com.freighttiger.driverassistant.domain.onboarding.OnboardingStep
import com.freighttiger.driverassistant.ui.components.BigButton
import com.freighttiger.driverassistant.ui.components.ButtonKind
import com.freighttiger.driverassistant.ui.components.DemoBanner
import com.freighttiger.driverassistant.ui.components.ErrorMessage
import com.freighttiger.driverassistant.ui.components.FtScaffold
import com.freighttiger.driverassistant.ui.components.SectionCard
import com.freighttiger.driverassistant.ui.components.StatusPill
import com.freighttiger.driverassistant.ui.components.Tone
import com.freighttiger.driverassistant.ui.navigation.Nav
import com.freighttiger.driverassistant.ui.text.reasonText

@Composable
fun OnboardingScreen(onFinished: () -> Unit, vm: OnboardingViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    BackHandler(enabled = state.step != OnboardingStep.WELCOME) { vm.back() }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (vm.demoMode) DemoBanner()
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (state.step) {
                    OnboardingStep.WELCOME -> WelcomeStep(vm::start)
                    OnboardingStep.PHONE -> PhoneStep(state.phoneInput, state.busy, state.errorCode, vm::onPhoneChanged, vm::submitPhone)
                    OnboardingStep.OTP -> OtpStep(
                        maskedPhone = state.normalizedPhone?.let { "******" + it.takeLast(4) }.orEmpty(),
                        demoHint = state.challenge?.demoHint,
                        busy = state.busy,
                        errorCode = state.errorCode,
                        onSubmit = vm::submitOtp,
                        onBack = vm::back,
                    )
                    OnboardingStep.IDENTITY -> IdentityStep(vm, state.errorCode)
                    OnboardingStep.LANGUAGE -> LanguageStep(state.language, state.errorCode, vm::selectLanguage, vm::confirmLanguage)
                    OnboardingStep.PERMISSIONS -> PermissionsStep(vm)
                    OnboardingStep.PRIVACY -> {
                        PrivacyContent()
                        BigButton(stringResource(R.string.privacy_accept), vm::acceptPrivacy, Modifier.testTag("accept_privacy"))
                    }
                    OnboardingStep.TEST_PROMPT, OnboardingStep.DONE -> TestPromptStep(vm, onFinished)
                }
            }
        }
    }
}

@Composable
private fun StepTitle(text: String) = Text(text, style = MaterialTheme.typography.headlineSmall)

@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    Spacer(Modifier.height(24.dp))
    Icon(Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
    Text(stringResource(R.string.welcome_title), style = MaterialTheme.typography.headlineMedium)
    Text(stringResource(R.string.welcome_body), style = MaterialTheme.typography.bodyLarge)
    Spacer(Modifier.height(16.dp))
    BigButton(stringResource(R.string.welcome_start), onStart, Modifier.testTag("welcome_start"))
}

@Composable
private fun PhoneStep(value: String, busy: Boolean, errorCode: String?, onChange: (String) -> Unit, onSubmit: () -> Unit) {
    StepTitle(stringResource(R.string.phone_title))
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(R.string.phone_hint)) },
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineSmall,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth().testTag("phone_input"),
    )
    if (errorCode != null) ErrorMessage(reasonText(errorCode))
    BigButton(stringResource(R.string.phone_send_otp), onSubmit, Modifier.testTag("send_otp"), enabled = !busy)
}

@Composable
private fun OtpStep(maskedPhone: String, demoHint: String?, busy: Boolean, errorCode: String?, onSubmit: (String) -> Unit, onBack: () -> Unit) {
    var otp by rememberSaveable { mutableStateOf("") }
    StepTitle(stringResource(R.string.otp_title))
    Text(stringResource(R.string.otp_sent_to, maskedPhone), style = MaterialTheme.typography.bodyLarge)
    if (demoHint != null) StatusPill(stringResource(R.string.otp_demo_hint, demoHint), Tone.WARNING)
    OutlinedTextField(
        value = otp,
        onValueChange = { otp = it.filter(Char::isDigit).take(6) },
        label = { Text(stringResource(R.string.otp_hint)) },
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineSmall,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth().testTag("otp_input"),
    )
    if (errorCode != null) ErrorMessage(reasonText(errorCode))
    BigButton(stringResource(R.string.otp_verify), { onSubmit(otp) }, Modifier.testTag("verify_otp"), enabled = !busy && otp.length == 6)
    BigButton(stringResource(R.string.action_back), onBack, kind = ButtonKind.SECONDARY)
}

@Composable
private fun IdentityStep(vm: OnboardingViewModel, errorCode: String?) {
    val state by vm.state.collectAsStateWithLifecycle()
    val profile = state.profile ?: return
    StepTitle(stringResource(R.string.identity_title))
    SectionCard {
        Text(profile.displayName, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.identity_driver_id, profile.driverId), style = MaterialTheme.typography.bodyLarge)
        Text(stringResource(R.string.identity_phone, profile.maskedPhone), style = MaterialTheme.typography.bodyLarge)
        val trip = state.activeTrip
        Text(
            if (trip != null) stringResource(R.string.identity_trip, trip.origin.city, trip.destination.city) else stringResource(R.string.identity_no_trip),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (state.simulatedAuth) StatusPill(stringResource(R.string.simulated_badge), Tone.WARNING)
    }
    if (errorCode == "IDENTITY_NOT_CONFIRMED") ErrorMessage(stringResource(R.string.identity_not_me_help))
    BigButton(stringResource(R.string.identity_confirm), { vm.confirmIdentity(true) }, Modifier.testTag("identity_confirm"))
    BigButton(stringResource(R.string.identity_not_me), { vm.confirmIdentity(false) }, kind = ButtonKind.SECONDARY)
}

@Composable
private fun LanguageStep(selected: AssistantLanguage, errorCode: String?, onSelect: (AssistantLanguage) -> Unit, onConfirm: () -> Unit) {
    StepTitle(stringResource(R.string.language_title))
    AssistantLanguage.entries.forEach { language ->
        val label = stringResource(
            when (language) {
                AssistantLanguage.HINDI -> R.string.language_hi
                AssistantLanguage.ENGLISH_INDIA -> R.string.language_en
                AssistantLanguage.MARATHI -> R.string.language_mr
                AssistantLanguage.TAMIL -> R.string.language_ta
                AssistantLanguage.TELUGU -> R.string.language_te
                AssistantLanguage.BENGALI -> R.string.language_bn
            },
        )
        SectionCard {
            Row(
                Modifier.fillMaxWidth().selectable(selected = language == selected, enabled = language.enabled, role = Role.RadioButton) { onSelect(language) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = language == selected, onClick = null, enabled = language.enabled)
                Spacer(Modifier.width(12.dp))
                Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (!language.enabled) StatusPill(stringResource(R.string.language_coming_soon), Tone.NEUTRAL)
            }
        }
    }
    if (errorCode == "LANGUAGE_NOT_AVAILABLE") ErrorMessage(stringResource(R.string.language_coming_soon))
    BigButton(stringResource(R.string.action_continue), onConfirm, Modifier.testTag("language_continue"))
}

@Composable
private fun PermissionsStep(vm: OnboardingViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val caps by vm.caps.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { vm.onMicrophoneResult(it) }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { vm.onNotificationResult(it) }
    LaunchedEffect(Unit) {
        vm.refreshCapabilities()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            vm.onNotificationResult(NotificationManagerCompat.from(context).areNotificationsEnabled())
        }
    }

    StepTitle(stringResource(R.string.permissions_title))

    SectionCard(title = stringResource(R.string.mic_title)) {
        Text(stringResource(R.string.mic_why), style = MaterialTheme.typography.bodyMedium)
        val micGranted = state.microphoneGranted ?: caps.microphonePermission
        when {
            micGranted -> PermissionResult(true, stringResource(R.string.mic_granted))
            state.microphoneGranted == false -> PermissionResult(false, stringResource(R.string.mic_denied))
        }
        if (!micGranted) BigButton(stringResource(R.string.mic_grant), { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }, icon = Icons.Filled.Mic)
    }

    SectionCard(title = stringResource(R.string.notif_title)) {
        Text(stringResource(R.string.notif_why), style = MaterialTheme.typography.bodyMedium)
        when (state.notificationsGranted) {
            true -> PermissionResult(true, stringResource(R.string.notif_granted))
            false -> PermissionResult(false, stringResource(R.string.notif_denied))
            null -> Unit
        }
        if (state.notificationsGranted != true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            BigButton(stringResource(R.string.notif_grant), { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }, icon = Icons.Filled.Notifications)
        }
    }

    SectionCard(title = stringResource(R.string.speech_check_title)) {
        PermissionResult(caps.ttsHindiAvailable != false, stringResource(if (caps.ttsHindiAvailable != false) R.string.tts_ok else R.string.tts_missing))
        PermissionResult(caps.recognitionAvailable, stringResource(if (caps.recognitionAvailable) R.string.stt_ok else R.string.stt_missing))
    }

    BigButton(stringResource(R.string.action_continue), vm::continueFromPermissions, Modifier.testTag("permissions_continue"))
}

@Composable
private fun PermissionResult(ok: Boolean, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (ok) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            contentDescription = null,
            tint = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PrivacyContent() {
    StepTitle(stringResource(R.string.privacy_title))
    listOf(
        R.string.privacy_point_scope,
        R.string.privacy_point_mic,
        R.string.privacy_point_recording,
        R.string.privacy_point_tracking,
        R.string.privacy_point_safety,
    ).forEach { res ->
        SectionCard {
            Row {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(res), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun TestPromptStep(vm: OnboardingViewModel, onFinished: () -> Unit) {
    val speaking by vm.speaking.collectAsStateWithLifecycle()
    val failed by vm.ttsFailed.collectAsStateWithLifecycle()
    StepTitle(stringResource(R.string.test_title))
    Text(stringResource(R.string.test_body), style = MaterialTheme.typography.bodyLarge)
    BigButton(
        stringResource(if (speaking) R.string.voice_speaking else R.string.test_play),
        vm::playTestPrompt,
        icon = Icons.AutoMirrored.Filled.VolumeUp,
        kind = ButtonKind.ACCENT,
        enabled = !speaking,
    )
    if (failed) ErrorMessage(stringResource(R.string.tts_missing))
    BigButton(stringResource(R.string.test_done), { vm.finish(onFinished) }, Modifier.testTag("finish_onboarding"))
}

/** Privacy information reachable from settings after onboarding. */
@Composable
fun PrivacyInfoScreen(nav: Nav) {
    FtScaffold(title = stringResource(R.string.settings_privacy), onBack = nav::back, onOpenSync = { nav.to(com.freighttiger.driverassistant.ui.navigation.Routes.SYNC) }) {
        PrivacyContent()
    }
}
