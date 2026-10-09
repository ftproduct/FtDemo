package com.freighttiger.driverassistant.feature.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.freighttiger.driverassistant.AppConfig
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.data.AssistantSettings
import com.freighttiger.driverassistant.data.SettingsRepository
import com.freighttiger.driverassistant.di.BackendMode
import com.freighttiger.driverassistant.di.BackendSelection
import com.freighttiger.driverassistant.domain.ports.AccessTokenStore
import com.freighttiger.driverassistant.domain.ports.SessionRepository
import com.freighttiger.driverassistant.domain.voice.SpeechInput
import com.freighttiger.driverassistant.domain.voice.SpeechOutput
import com.freighttiger.driverassistant.ui.components.BigButton
import com.freighttiger.driverassistant.ui.components.ButtonKind
import com.freighttiger.driverassistant.ui.components.FtScaffold
import com.freighttiger.driverassistant.ui.components.LabeledValue
import com.freighttiger.driverassistant.ui.components.SectionCard
import com.freighttiger.driverassistant.ui.navigation.Nav
import com.freighttiger.driverassistant.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val sessions: SessionRepository,
    private val tokens: AccessTokenStore,
    private val speechOut: SpeechOutput,
    private val speechIn: SpeechInput,
    selection: BackendSelection,
    config: AppConfig,
) : ViewModel() {
    val settings: StateFlow<AssistantSettings> = settingsRepo.settings
    val backendMode = selection.mode
    val environment = config.api.environment
    val version = config.versionName

    fun update(transform: (AssistantSettings) -> AssistantSettings) = settingsRepo.update(transform)

    fun stopAudio() {
        speechOut.stop()
        speechIn.cancel()
    }

    /** Clears the local session and token. Queued events stay in the outbox until delivered. */
    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            tokens.clear()
            sessions.clear()
            settingsRepo.update { it.copy(onboardingComplete = false) }
            onDone()
        }
    }
}

@Composable
fun SettingsScreen(nav: Nav, vm: SettingsViewModel = hiltViewModel()) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    FtScaffold(title = stringResource(R.string.settings_title), onBack = nav::back, onOpenSync = { nav.to(Routes.SYNC) }) {
        BigButton(stringResource(R.string.action_stop_audio), vm::stopAudio, icon = Icons.AutoMirrored.Filled.VolumeOff, kind = ButtonKind.ACCENT)

        SectionCard {
            ToggleRow(stringResource(R.string.settings_auto_speak), stringResource(R.string.settings_auto_speak_desc), s.autoSpeakPrompts) { v ->
                vm.update { it.copy(autoSpeakPrompts = v) }
            }
            ToggleRow(stringResource(R.string.settings_pause), stringResource(R.string.settings_pause_desc), s.nonUrgentPaused) { v ->
                vm.update { it.copy(nonUrgentPaused = v) }
            }
        }

        SectionCard(title = stringResource(R.string.settings_defer)) {
            MinuteChips(listOf(5, 10, 20, 30), s.deferMinutes) { m -> vm.update { it.copy(deferMinutes = m) } }
        }
        SectionCard(title = stringResource(R.string.settings_cooldown)) {
            MinuteChips(listOf(5, 10, 15, 30), s.cooldownMinutes) { m -> vm.update { it.copy(cooldownMinutes = m) } }
        }

        BigButton(stringResource(R.string.settings_consent), { nav.to(Routes.CONSENT) }, icon = Icons.Filled.GppGood, kind = ButtonKind.SECONDARY)
        BigButton(stringResource(R.string.settings_privacy), { nav.to(Routes.PRIVACY) }, icon = Icons.Filled.PrivacyTip, kind = ButtonKind.SECONDARY)
        BigButton(
            stringResource(R.string.settings_notifications),
            {
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            },
            icon = Icons.Filled.Notifications,
            kind = ButtonKind.SECONDARY,
        )
        BigButton(
            stringResource(R.string.settings_app_permissions),
            {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            },
            icon = Icons.Filled.Tune,
            kind = ButtonKind.SECONDARY,
        )

        SectionCard {
            LabeledValue(stringResource(R.string.settings_language), "hi-IN")
            val backend = when (vm.backendMode) {
                BackendMode.DEMO_SIMULATED -> stringResource(R.string.settings_backend_demo)
                BackendMode.REMOTE -> stringResource(R.string.settings_backend_remote, vm.environment)
                BackendMode.NOT_CONFIGURED -> stringResource(R.string.settings_backend_unconfigured)
            }
            Text(stringResource(R.string.settings_environment, backend), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.settings_version, vm.version), style = MaterialTheme.typography.bodySmall)
        }

        BigButton(stringResource(R.string.settings_logout), { vm.logout(nav::restartOnboarding) }, icon = Icons.AutoMirrored.Filled.Logout, kind = ButtonKind.SECONDARY)
    }
}

@Composable
private fun ToggleRow(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun MinuteChips(options: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    Row {
        options.forEach { m ->
            FilterChip(
                selected = m == selected,
                onClick = { onSelect(m) },
                label = { Text(stringResource(R.string.minutes_value, m)) },
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}
