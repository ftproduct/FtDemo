package com.freighttiger.driverassistant.feature.consent

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.core.model.ConsentDecision
import com.freighttiger.driverassistant.core.model.ConsentRecord
import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.feature.trips.ActionFeedback
import com.freighttiger.driverassistant.ui.components.BigButton
import com.freighttiger.driverassistant.ui.components.ButtonKind
import com.freighttiger.driverassistant.ui.components.EmptyState
import com.freighttiger.driverassistant.ui.components.ErrorMessage
import com.freighttiger.driverassistant.ui.components.FtScaffold
import com.freighttiger.driverassistant.ui.components.LabeledValue
import com.freighttiger.driverassistant.ui.components.LoadingState
import com.freighttiger.driverassistant.ui.components.SectionCard
import com.freighttiger.driverassistant.ui.components.StatusPill
import com.freighttiger.driverassistant.ui.components.SuccessMessage
import com.freighttiger.driverassistant.ui.components.Tone
import com.freighttiger.driverassistant.ui.navigation.Nav
import com.freighttiger.driverassistant.ui.navigation.Routes
import com.freighttiger.driverassistant.ui.text.consentRes
import com.freighttiger.driverassistant.ui.text.consentTone
import com.freighttiger.driverassistant.ui.text.formatTime
import com.freighttiger.driverassistant.ui.text.methodRes
import com.freighttiger.driverassistant.ui.text.reasonText
import com.freighttiger.driverassistant.ui.text.submissionRes
import com.freighttiger.driverassistant.ui.text.trackingRes
import com.freighttiger.driverassistant.ui.text.trackingTone

@Composable
fun ConsentScreen(nav: Nav, vm: ConsentViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val feedback by vm.feedback.collectAsStateWithLifecycle()
    var confirmWithdraw by rememberSaveable { mutableStateOf(false) }

    FtScaffold(title = stringResource(R.string.consent_title), onBack = nav::back, onOpenSync = { nav.to(Routes.SYNC) }) {
        val trip = state.trip
        if (state.loading) {
            LoadingState()
            return@FtScaffold
        }
        if (trip == null) {
            EmptyState(Icons.Filled.GppGood, stringResource(R.string.consent_no_request))
            return@FtScaffold
        }

        when (val f = feedback) {
            ActionFeedback.Queued -> SuccessMessage(stringResource(R.string.voice_result_queued))
            is ActionFeedback.Refused -> ErrorMessage(stringResource(R.string.voice_result_refused, reasonText(f.reasonCode)))
            null -> Unit
        }

        val open = state.open
        if (open != null) {
            SectionCard(title = stringResource(R.string.consent_question)) {
                if (open.simulated) StatusPill(stringResource(R.string.simulated_badge), Tone.WARNING)
                Text(stringResource(R.string.consent_explain, trip.tripId), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.consent_purpose), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.consent_expires, formatTime(open.expiresAt)), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.consent_tracking_note), style = MaterialTheme.typography.bodySmall)
                BigButton(stringResource(R.string.consent_yes), { vm.decide(ConsentDecision.GRANTED) }, Modifier.testTag("consent_yes"), icon = Icons.Filled.CheckCircle)
                // Declining is exactly as easy as agreeing.
                BigButton(stringResource(R.string.consent_no), { vm.decide(ConsentDecision.DECLINED) }, Modifier.testTag("consent_no"), icon = Icons.Filled.Cancel, kind = ButtonKind.DANGER)
                if (state.openPrompt != null) {
                    BigButton(stringResource(R.string.consent_listen), { nav.voice(state.openPrompt?.promptId, userInitiated = true) }, icon = Icons.Filled.Mic, kind = ButtonKind.SECONDARY)
                }
            }
        }

        val latest = state.latest
        if (latest == null) {
            EmptyState(Icons.Filled.GppGood, stringResource(R.string.consent_no_request))
        } else if (latest !== open) {
            ConsentStatusCard(latest)
        }

        SectionCard(title = stringResource(R.string.label_tracking)) {
            StatusPill(stringResource(trackingRes(trip.trackingStatus)), trackingTone(trip.trackingStatus), Modifier.testTag("consent_tracking_status"))
            Text(stringResource(R.string.tracking_how), style = MaterialTheme.typography.bodySmall)
        }

        if (latest != null && (latest.state == ConsentState.GRANTED || latest.state == ConsentState.GRANTED_PENDING_VALIDATION)) {
            BigButton(stringResource(R.string.consent_withdraw), { confirmWithdraw = true }, Modifier.testTag("consent_withdraw"), kind = ButtonKind.SECONDARY)
        }

        if (state.history.isNotEmpty()) {
            SectionCard(title = stringResource(R.string.consent_history)) {
                state.history.forEach { record ->
                    StatusPill("${formatTime(record.requestedAt)} · ${stringResource(consentRes(record.state))}", consentTone(record.state))
                }
            }
        }
    }

    if (confirmWithdraw) {
        AlertDialog(
            onDismissRequest = { confirmWithdraw = false },
            title = { Text(stringResource(R.string.consent_withdraw_title)) },
            text = { Text(stringResource(R.string.consent_withdraw_body)) },
            confirmButton = {
                TextButton(onClick = { confirmWithdraw = false; vm.withdraw() }) { Text(stringResource(R.string.consent_withdraw_confirm)) }
            },
            dismissButton = { TextButton(onClick = { confirmWithdraw = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
private fun ConsentStatusCard(record: ConsentRecord) {
    SectionCard(title = stringResource(R.string.consent_status)) {
        if (record.simulated) StatusPill(stringResource(R.string.simulated_badge), Tone.WARNING)
        StatusPill(stringResource(consentRes(record.state)), consentTone(record.state), Modifier.testTag("consent_state"))
        val decision = record.decision
        val capturedAt = record.capturedAt
        val method = record.captureMethod
        if (decision != null && capturedAt != null && method != null) {
            Text(
                stringResource(
                    R.string.consent_captured_at,
                    stringResource(if (decision == ConsentDecision.GRANTED) R.string.action_yes else R.string.action_no),
                    "${formatTime(capturedAt)}, ${stringResource(methodRes(method))}",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        submissionRes(record.submissionState)?.let { LabeledValue(stringResource(R.string.consent_submission), stringResource(it)) }
        record.reasonCode?.let { Text(reasonText(it), style = MaterialTheme.typography.bodySmall) }
    }
}
