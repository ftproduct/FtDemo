package com.freighttiger.driverassistant.feature.sync

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.OutboxStatus
import com.freighttiger.driverassistant.di.BackendMode
import com.freighttiger.driverassistant.di.BackendSelection
import com.freighttiger.driverassistant.domain.ports.ConnectivityMonitor
import com.freighttiger.driverassistant.domain.ports.OutboxRepository
import com.freighttiger.driverassistant.domain.sync.OutboxSyncEngine
import com.freighttiger.driverassistant.platform.push.PushStatus
import com.freighttiger.driverassistant.platform.push.PushTokenRegistrar
import com.freighttiger.driverassistant.platform.sync.SyncScheduler
import com.freighttiger.driverassistant.ui.components.BigButton
import com.freighttiger.driverassistant.ui.components.ButtonKind
import com.freighttiger.driverassistant.ui.components.EmptyState
import com.freighttiger.driverassistant.ui.components.ErrorMessage
import com.freighttiger.driverassistant.ui.components.FtScaffold
import com.freighttiger.driverassistant.ui.components.SectionCard
import com.freighttiger.driverassistant.ui.components.StatusPill
import com.freighttiger.driverassistant.ui.components.Tone
import com.freighttiger.driverassistant.ui.navigation.Nav
import com.freighttiger.driverassistant.ui.text.formatTime
import com.freighttiger.driverassistant.ui.text.outboundTypeRes
import com.freighttiger.driverassistant.ui.text.outboxStatusRes
import com.freighttiger.driverassistant.ui.text.outboxTone
import com.freighttiger.driverassistant.ui.text.reasonText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    connectivity: ConnectivityMonitor,
    outbox: OutboxRepository,
    private val engine: OutboxSyncEngine,
    private val scheduler: SyncScheduler,
    registrar: PushTokenRegistrar,
    selection: BackendSelection,
) : ViewModel() {
    val online: StateFlow<Boolean> = connectivity.isOnline
    val events: StateFlow<List<OutboundEvent>> = outbox.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val push: StateFlow<PushStatus> = registrar.status
    val backendMode = selection.mode

    fun syncNow() { viewModelScope.launch { scheduler.runNow() } }

    fun retryFailed() {
        viewModelScope.launch {
            engine.retryFailed()
            scheduler.requestSync()
        }
    }
}

@Composable
fun SyncScreen(nav: Nav, vm: SyncViewModel = hiltViewModel()) {
    val online by vm.online.collectAsStateWithLifecycle()
    val events by vm.events.collectAsStateWithLifecycle()
    val push by vm.push.collectAsStateWithLifecycle()

    FtScaffold(title = stringResource(R.string.sync_title), onBack = nav::back, onOpenSync = {}) {
        if (vm.backendMode == BackendMode.NOT_CONFIGURED) ErrorMessage(stringResource(R.string.sync_not_configured))

        SectionCard {
            StatusPill(
                stringResource(if (online) R.string.sync_online else R.string.sync_offline),
                if (online) Tone.SUCCESS else Tone.DANGER,
            )
            val pending = events.count { it.status.isOpen }
            val failed = events.count { it.status == OutboxStatus.FAILED_MAX_RETRIES || it.status == OutboxStatus.FAILED_PERMANENT }
            Text(
                if (pending == 0 && failed == 0) stringResource(R.string.all_synced) else stringResource(R.string.sync_pending, pending),
                style = MaterialTheme.typography.titleMedium,
            )
            if (failed > 0) Text(stringResource(R.string.sync_failed, failed), color = MaterialTheme.colorScheme.error)
            val pushLabel = stringResource(
                when (push) {
                    PushStatus.SIMULATED -> R.string.push_simulated
                    PushStatus.NOT_CONFIGURED -> R.string.push_not_configured
                    PushStatus.PENDING -> R.string.push_pending
                    PushStatus.REGISTERED -> R.string.push_registered
                    PushStatus.FAILED -> R.string.push_failed
                },
            )
            Text(stringResource(R.string.sync_push_status, pushLabel), style = MaterialTheme.typography.bodySmall)
        }

        BigButton(stringResource(R.string.sync_now), vm::syncNow, icon = Icons.Filled.Sync, enabled = online)
        if (events.any { it.status == OutboxStatus.FAILED_MAX_RETRIES }) {
            BigButton(stringResource(R.string.sync_retry_failed), vm::retryFailed, icon = Icons.Filled.Refresh, kind = ButtonKind.SECONDARY)
        }

        if (events.isEmpty()) {
            EmptyState(if (online) Icons.Filled.CloudDone else Icons.Filled.CloudOff, stringResource(R.string.sync_empty))
        }
        events.forEach { e ->
            SectionCard {
                StatusPill(stringResource(outboxStatusRes(e.status)), outboxTone(e.status))
                Text(stringResource(outboundTypeRes(e.payload.eventType)), style = MaterialTheme.typography.titleMedium)
                Text(formatTime(e.createdAt), style = MaterialTheme.typography.bodySmall)
                if (e.attempts > 0) Text(stringResource(R.string.sync_attempts, e.attempts), style = MaterialTheme.typography.bodySmall)
                e.nextAttemptAt?.takeIf { e.status == OutboxStatus.RETRY_SCHEDULED }?.let {
                    Text(stringResource(R.string.sync_next, formatTime(it)), style = MaterialTheme.typography.bodySmall)
                }
                e.lastErrorCode?.let { Text(stringResource(R.string.sync_error, reasonText(it)), style = MaterialTheme.typography.bodySmall) }
                e.serverReference?.let { Text(stringResource(R.string.sync_reference, it), style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

