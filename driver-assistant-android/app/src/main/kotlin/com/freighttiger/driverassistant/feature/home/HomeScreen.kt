package com.freighttiger.driverassistant.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.ui.components.BigButton
import com.freighttiger.driverassistant.ui.components.ButtonKind
import com.freighttiger.driverassistant.ui.components.EmptyState
import com.freighttiger.driverassistant.ui.components.FtScaffold
import com.freighttiger.driverassistant.ui.components.LabeledValue
import com.freighttiger.driverassistant.ui.components.LoadingState
import com.freighttiger.driverassistant.ui.components.SectionCard
import com.freighttiger.driverassistant.ui.components.StatusPill
import com.freighttiger.driverassistant.ui.components.Tone
import com.freighttiger.driverassistant.ui.navigation.LocalAppStatus
import com.freighttiger.driverassistant.ui.navigation.Nav
import com.freighttiger.driverassistant.ui.navigation.Routes
import com.freighttiger.driverassistant.ui.text.activityRes
import com.freighttiger.driverassistant.ui.text.activityTone
import com.freighttiger.driverassistant.ui.text.consentRes
import com.freighttiger.driverassistant.ui.text.consentTone
import com.freighttiger.driverassistant.ui.text.formatTime
import com.freighttiger.driverassistant.ui.text.nextActionRes
import com.freighttiger.driverassistant.ui.text.promptTitleRes
import com.freighttiger.driverassistant.ui.text.trackingRes
import com.freighttiger.driverassistant.ui.text.trackingTone
import com.freighttiger.driverassistant.ui.text.tripStatusRes

@Composable
fun HomeScreen(nav: Nav, vm: HomeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val demo = LocalAppStatus.current.demoMode

    FtScaffold(
        title = stringResource(R.string.home_title),
        onBack = null,
        onOpenSync = { nav.to(Routes.SYNC) },
        actions = {
            IconButton(onClick = { nav.to(Routes.SETTINGS) }) {
                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.menu_settings))
            }
        },
        bottomBar = {
            // The primary voice action is always one large tap away.
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                BigButton(
                    text = stringResource(R.string.talk_button),
                    onClick = { nav.voice(state.pendingPrompt?.promptId, userInitiated = true) },
                    icon = Icons.Filled.Mic,
                    kind = ButtonKind.ACCENT,
                    modifier = Modifier.heightIn(min = 72.dp).testTag("talk_button"),
                )
            }
        },
    ) {
        if (state.loading) {
            LoadingState()
            return@FtScaffold
        }

        state.pendingPrompt?.let { prompt ->
            SectionCard(title = stringResource(R.string.pending_question)) {
                Text(stringResource(promptTitleRes(prompt.type)), style = MaterialTheme.typography.titleLarge)
                if (prompt.simulated) StatusPill(stringResource(R.string.simulated_badge), Tone.WARNING)
                BigButton(stringResource(R.string.answer_now), { nav.voice(prompt.promptId, userInitiated = true) }, Modifier.testTag("answer_pending"))
            }
        }

        val trip = state.trip
        if (trip == null) {
            EmptyState(Icons.Filled.LocalShipping, stringResource(R.string.home_no_trip_title), stringResource(R.string.home_no_trip_body))
        } else {
            SectionCard(onClick = { nav.to(Routes.TRIP) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.route_from_to, trip.origin.city, trip.destination.city),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                    StatusPill(stringResource(tripStatusRes(trip.status)), if (trip.isActive) Tone.INFO else Tone.NEUTRAL)
                }
                if (trip.simulated) StatusPill(stringResource(R.string.simulated_badge), Tone.WARNING)
                LabeledValue(stringResource(R.string.label_vehicle), trip.vehicleRegistration)
                LabeledValue(stringResource(R.string.label_loading_point), "${trip.loadingPoint.name}, ${trip.loadingPoint.city}")
            }

            SectionCard(title = stringResource(R.string.label_next_action)) {
                Text(stringResource(nextActionRes(state.nextAction)), style = MaterialTheme.typography.titleLarge)
            }

            SectionCard(title = stringResource(R.string.label_tracking), onClick = { nav.to(Routes.TRACKING) }) {
                StatusPill(stringResource(trackingRes(trip.trackingStatus)), trackingTone(trip.trackingStatus))
                state.consent?.let { StatusPill(stringResource(consentRes(it.state)), consentTone(it.state)) }
            }
        }

        // Large, simple navigation grid.
        val items = buildList {
            add(Triple(R.string.menu_report, Icons.Filled.EditNote, Routes.REPORT))
            add(Triple(R.string.menu_consent, Icons.Filled.GppGood, Routes.CONSENT))
            add(Triple(R.string.menu_trip_details, Icons.AutoMirrored.Filled.Assignment, Routes.TRIP))
            add(Triple(R.string.menu_tracking, Icons.Filled.MyLocation, Routes.TRACKING))
            add(Triple(R.string.menu_activity, Icons.Filled.History, Routes.ACTIVITY))
            add(Triple(R.string.menu_support, Icons.Filled.SupportAgent, Routes.SUPPORT))
            add(Triple(R.string.menu_sync, Icons.Filled.Sync, Routes.SYNC))
            if (demo) add(Triple(R.string.menu_demo, Icons.Filled.Science, Routes.DEMO))
        }
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, icon, route) -> NavTile(stringResource(label), icon, Modifier.weight(1f)) { nav.to(route) } }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        SectionCard(title = stringResource(R.string.recent_activity)) {
            if (state.recent.isEmpty()) Text(stringResource(R.string.activity_empty), style = MaterialTheme.typography.bodyMedium)
            state.recent.forEach { entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(stringResource(activityRes(entry.kind)), activityTone(entry.kind))
                    Spacer(Modifier.width(8.dp))
                    Text(formatTime(entry.timestamp), style = MaterialTheme.typography.bodySmall)
                }
            }
            TextButton(onClick = { nav.to(Routes.ACTIVITY) }) { Text(stringResource(R.string.view_all)) }
        }
    }
}

@Composable
private fun NavTile(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, modifier = modifier.heightIn(min = 88.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null)
            Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
        }
    }
}
