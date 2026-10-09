package com.freighttiger.driverassistant.feature.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.QuestionContext
import com.freighttiger.driverassistant.domain.conversation.TapResponse
import com.freighttiger.driverassistant.ui.components.BigButton
import com.freighttiger.driverassistant.ui.components.ButtonKind
import com.freighttiger.driverassistant.ui.components.ErrorMessage
import com.freighttiger.driverassistant.ui.components.FtScaffold
import com.freighttiger.driverassistant.ui.components.MicIndicator
import com.freighttiger.driverassistant.ui.components.MicState
import com.freighttiger.driverassistant.ui.components.SectionCard
import com.freighttiger.driverassistant.ui.components.StatusPill
import com.freighttiger.driverassistant.ui.components.SuccessMessage
import com.freighttiger.driverassistant.ui.components.Tone
import com.freighttiger.driverassistant.ui.navigation.Nav
import com.freighttiger.driverassistant.ui.navigation.Routes
import com.freighttiger.driverassistant.ui.text.loadingRes
import com.freighttiger.driverassistant.ui.text.promptTitleRes
import com.freighttiger.driverassistant.ui.text.reasonText

@Composable
fun VoiceScreen(nav: Nav, vm: VoiceViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Requested just in time. If denied, the listening turn reports PERMISSION_DENIED and the
    // dialogue falls back to on-screen buttons.
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { vm.onMicTapped() }
    val onMic = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            vm.onMicTapped()
        } else {
            micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    FtScaffold(
        title = stringResource(state.prompt?.let { promptTitleRes(it.type) } ?: R.string.voice_title),
        onBack = { vm.stopAudio(); nav.back() },
        onOpenSync = { nav.to(Routes.SYNC) },
    ) {
        val micState = when (state.phase) {
            VoicePhase.LISTENING -> MicState.LISTENING
            VoicePhase.SPEAKING -> MicState.SPEAKING
            else -> MicState.OFF
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MicIndicator(micState, Modifier.testTag("mic_indicator"))
            if (state.prompt?.simulated == true) StatusPill(stringResource(R.string.simulated_badge), Tone.WARNING)
        }

        // Caption: everything spoken is also shown, in large text.
        SectionCard {
            Text(
                state.caption.ifBlank { stringResource(R.string.loading) },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.testTag("voice_caption"),
            )
            when (state.phase) {
                VoicePhase.LISTENING -> Text(stringResource(R.string.voice_listening), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                VoicePhase.PROCESSING -> LinearProgressIndicator(Modifier.fillMaxWidth())
                else -> Unit
            }
            state.heard?.let { Text(stringResource(R.string.voice_heard, it), style = MaterialTheme.typography.bodyLarge) }
        }
        if (state.ttsFailed) ErrorMessage(stringResource(R.string.voice_tts_unavailable))

        when (val r = state.result) {
            VoiceResult.Queued -> SuccessMessage(stringResource(R.string.voice_result_queued))
            VoiceResult.Deferred -> SuccessMessage(stringResource(R.string.voice_result_deferred))
            VoiceResult.Info -> SuccessMessage(stringResource(R.string.voice_result_info))
            VoiceResult.NotUnderstood -> ErrorMessage(stringResource(R.string.error_generic))
            is VoiceResult.Refused -> ErrorMessage(stringResource(R.string.voice_result_refused, reasonText(r.reasonCode)))
            null -> Unit
        }

        if (state.phase == VoicePhase.DONE) {
            BigButton(stringResource(R.string.action_close), { nav.back() }, Modifier.testTag("voice_close"))
            return@FtScaffold
        }

        // Large mic button: listening only ever starts from here or from an explicit prior tap.
        if (!state.micUnavailable) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val listening = state.phase == VoicePhase.LISTENING
                val label = stringResource(if (listening) R.string.voice_listening else R.string.voice_tap_mic)
                FilledIconButton(
                    onClick = { if (listening) vm.stopAudio() else onMic() },
                    modifier = Modifier.size(120.dp).semantics { contentDescription = label }.testTag("mic_button"),
                    shape = CircleShape,
                    colors = if (listening) IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error)
                    else IconButtonDefaults.filledIconButtonColors(),
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(56.dp))
                }
            }
            Text(
                stringResource(if (state.phase == VoicePhase.LISTENING) R.string.voice_listening else R.string.voice_tap_mic),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.showTapOptions) TapOptions(state.context, vm)

        if (state.showSupport) {
            BigButton(stringResource(R.string.voice_support_option), { vm.onTap(TapResponse.Support) }, icon = Icons.Filled.SupportAgent, kind = ButtonKind.ACCENT)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BigButton(stringResource(R.string.action_repeat), vm::onRepeat, Modifier.weight(1f), icon = Icons.Filled.Replay, kind = ButtonKind.SECONDARY)
            BigButton(stringResource(R.string.action_stop_audio), vm::stopAudio, Modifier.weight(1f).testTag("stop_audio"), icon = Icons.AutoMirrored.Filled.VolumeOff, kind = ButtonKind.SECONDARY)
        }
        if (state.prompt != null) {
            BigButton(stringResource(R.string.voice_defer), { vm.onTap(TapResponse.Later) }, Modifier.testTag("voice_later"), icon = Icons.Filled.Schedule, kind = ButtonKind.SECONDARY)
        }
        Text(stringResource(R.string.safety_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TapOptions(context: QuestionContext?, vm: VoiceViewModel) {
    SectionCard(title = stringResource(R.string.voice_answer_by_tap)) {
        when (context) {
            QuestionContext.CONSENT -> {
                BigButton(stringResource(R.string.consent_yes), { vm.onTap(TapResponse.Yes) }, Modifier.testTag("tap_yes"), icon = Icons.Filled.CheckCircle)
                BigButton(stringResource(R.string.consent_no), { vm.onTap(TapResponse.No) }, Modifier.testTag("tap_no"), icon = Icons.Filled.Cancel, kind = ButtonKind.DANGER)
            }
            QuestionContext.ARRIVAL -> {
                BigButton(stringResource(R.string.arrival_yes), { vm.onTap(TapResponse.Yes) }, Modifier.testTag("tap_yes"), icon = Icons.Filled.CheckCircle)
                BigButton(stringResource(R.string.arrival_no), { vm.onTap(TapResponse.No) }, Modifier.testTag("tap_no"), kind = ButtonKind.SECONDARY)
            }
            QuestionContext.ETA -> {
                Text(stringResource(R.string.voice_other_eta), style = MaterialTheme.typography.bodyMedium)
                listOf(15 to R.string.eta_15, 30 to R.string.eta_30, 60 to R.string.eta_60, 120 to R.string.eta_120).chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { (minutes, label) ->
                            BigButton(stringResource(label), { vm.onTap(TapResponse.EtaMinutes(minutes)) }, Modifier.weight(1f).testTag("tap_eta_$minutes"), kind = ButtonKind.SECONDARY)
                        }
                    }
                }
            }
            QuestionContext.LOADING_STATUS -> {
                listOf(
                    LoadingStatus.REACHED_LOADING_POINT, LoadingStatus.WAITING_FOR_LOADING, LoadingStatus.LOADING_STARTED,
                    LoadingStatus.LOADING_COMPLETED, LoadingStatus.ISSUE_REPORTED,
                ).forEach { status ->
                    BigButton(stringResource(loadingRes(status)), { vm.onTap(TapResponse.Loading(status)) }, kind = ButtonKind.SECONDARY)
                }
            }
            QuestionContext.GENERAL, null -> Unit
        }
    }
}
