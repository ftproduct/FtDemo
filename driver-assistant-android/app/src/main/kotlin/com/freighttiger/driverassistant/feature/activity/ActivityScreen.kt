package com.freighttiger.driverassistant.feature.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.core.model.ActivityEntry
import com.freighttiger.driverassistant.core.model.InboundEventRecord
import com.freighttiger.driverassistant.core.model.InboundOutcome
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.domain.ports.ActivityLog
import com.freighttiger.driverassistant.domain.ports.InboundEventRepository
import com.freighttiger.driverassistant.domain.ports.OutboxRepository
import com.freighttiger.driverassistant.ui.components.EmptyState
import com.freighttiger.driverassistant.ui.components.FtScaffold
import com.freighttiger.driverassistant.ui.components.SectionCard
import com.freighttiger.driverassistant.ui.components.StatusPill
import com.freighttiger.driverassistant.ui.components.Tone
import com.freighttiger.driverassistant.ui.navigation.Nav
import com.freighttiger.driverassistant.ui.navigation.Routes
import com.freighttiger.driverassistant.ui.text.activityRes
import com.freighttiger.driverassistant.ui.text.activityTone
import com.freighttiger.driverassistant.ui.text.formatTime
import com.freighttiger.driverassistant.ui.text.inboundOutcomeRes
import com.freighttiger.driverassistant.ui.text.outboundTypeRes
import com.freighttiger.driverassistant.ui.text.outboxStatusRes
import com.freighttiger.driverassistant.ui.text.outboxTone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    activity: ActivityLog,
    inbound: InboundEventRepository,
    outbox: OutboxRepository,
) : ViewModel() {
    val entries: StateFlow<List<ActivityEntry>> = activity.observeRecent(300).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val received: StateFlow<List<InboundEventRecord>> = inbound.observeRecent(300).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val sent: StateFlow<List<OutboundEvent>> = outbox.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun ActivityScreen(nav: Nav, vm: ActivityViewModel = hiltViewModel()) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val entries by vm.entries.collectAsStateWithLifecycle()
    val received by vm.received.collectAsStateWithLifecycle()
    val sent by vm.sent.collectAsStateWithLifecycle()

    FtScaffold(title = stringResource(R.string.activity_title), onBack = nav::back, onOpenSync = { nav.to(Routes.SYNC) }, scrollable = false) {
        TabRow(selectedTabIndex = tab) {
            listOf(R.string.tab_activity, R.string.tab_received, R.string.tab_sent).forEachIndexed { i, label ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(stringResource(label), style = MaterialTheme.typography.labelMedium) })
            }
        }
        val empty = when (tab) {
            0 -> entries.isEmpty()
            1 -> received.isEmpty()
            else -> sent.isEmpty()
        }
        if (empty) {
            EmptyState(Icons.Filled.History, stringResource(R.string.activity_empty))
            return@FtScaffold
        }
        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when (tab) {
                0 -> items(entries, key = { it.id }) { e ->
                    Row2(stringResource(activityRes(e.kind)), activityTone(e.kind), formatTime(e.timestamp), e.detail, e.simulated)
                }
                1 -> items(received, key = { it.eventId }) { r ->
                    val tone = when (r.outcome) {
                        InboundOutcome.APPLIED -> Tone.SUCCESS
                        InboundOutcome.DUPLICATE, InboundOutcome.IGNORED -> Tone.NEUTRAL
                        InboundOutcome.REJECTED -> Tone.DANGER
                    }
                    Row2(stringResource(inboundOutcomeRes(r.outcome)), tone, formatTime(r.receivedAt), listOfNotNull(r.type.name, r.reasonCode).joinToString(" · "), r.source.name == "SIMULATED")
                }
                else -> items(sent, key = { it.eventId }) { e ->
                    Row2(stringResource(outboxStatusRes(e.status)), outboxTone(e.status), formatTime(e.createdAt), stringResource(outboundTypeRes(e.payload.eventType)), false)
                }
            }
        }
    }
}

@Composable
private fun Row2(label: String, tone: Tone, time: String, detail: String, simulated: Boolean) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusPill(label, tone)
            Spacer(Modifier.width(8.dp))
            Text(time, style = MaterialTheme.typography.bodySmall)
            if (simulated) {
                Spacer(Modifier.width(8.dp))
                StatusPill(stringResource(R.string.simulated_badge), Tone.WARNING)
            }
        }
        Column { Text(detail, style = MaterialTheme.typography.bodyMedium) }
    }
}
