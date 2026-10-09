package com.freighttiger.driverassistant.feature.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.core.model.ConsentRecord
import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.core.model.InboundTripEvent
import com.freighttiger.driverassistant.core.model.Milestone
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.core.network.mock.ConsentValidationMode
import com.freighttiger.driverassistant.core.network.mock.MockFreightTigerBackend
import com.freighttiger.driverassistant.core.network.mock.MockSubmission
import com.freighttiger.driverassistant.core.network.mock.SimulationControls
import com.freighttiger.driverassistant.di.BackendSelection
import com.freighttiger.driverassistant.domain.ports.ConsentRepository
import com.freighttiger.driverassistant.domain.ports.SessionRepository
import com.freighttiger.driverassistant.domain.ports.TripRepository
import com.freighttiger.driverassistant.platform.connectivity.AndroidConnectivityMonitor
import com.freighttiger.driverassistant.platform.sync.SyncScheduler
import com.freighttiger.driverassistant.ui.components.BigButton
import com.freighttiger.driverassistant.ui.components.ButtonKind
import com.freighttiger.driverassistant.ui.components.EmptyState
import com.freighttiger.driverassistant.ui.components.FtScaffold
import com.freighttiger.driverassistant.ui.components.SectionCard
import com.freighttiger.driverassistant.ui.components.StatusPill
import com.freighttiger.driverassistant.ui.components.Tone
import com.freighttiger.driverassistant.ui.navigation.Nav
import com.freighttiger.driverassistant.ui.navigation.Routes
import com.freighttiger.driverassistant.ui.text.formatTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Demo-only controls for the SIMULATED backend. Not available (and not reachable from the UI)
 * unless the build has demo mode enabled.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DemoViewModel @Inject constructor(
    selection: BackendSelection,
    private val connectivity: AndroidConnectivityMonitor,
    trips: TripRepository,
    consents: ConsentRepository,
    private val sessions: SessionRepository,
    private val sync: SyncScheduler,
) : ViewModel() {
    private val mock: MockFreightTigerBackend? = selection.mock
    val available = mock != null

    val controls: StateFlow<SimulationControls> = mock?.controls ?: MutableStateFlow(SimulationControls())
    val submissions: StateFlow<List<MockSubmission>> = mock?.submissions ?: MutableStateFlow(emptyList())
    val simulatedOffline: StateFlow<Boolean> = connectivity.simulatedOffline

    val activeTrip: StateFlow<Trip?> = trips.observeActiveTrip().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val pendingValidation: StateFlow<ConsentRecord?> = trips.observeActiveTrip()
        .flatMapLatest { t -> if (t == null) flowOf(emptyList()) else consents.observeForTrip(t.tripId) }
        .map { list -> list.firstOrNull { it.state == ConsentState.GRANTED_PENDING_VALIDATION } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _lastResult = MutableStateFlow<String?>(null)
    val lastResult: StateFlow<String?> = _lastResult.asStateFlow()

    private fun simulate(block: suspend (MockFreightTigerBackend) -> InboundTripEvent?) {
        val m = mock ?: return
        viewModelScope.launch {
            val event = block(m)
            _lastResult.value = event?.let { "${it.type} · ${it.eventId.takeLast(8)}" } ?: "NOT_APPLICABLE"
        }
    }

    private fun tripId() = activeTrip.value?.tripId

    fun assignTrip() = simulate { m -> m.simulateTripAssigned(sessions.current()?.profile?.driverId ?: MockFreightTigerBackend.DEMO_DRIVER_ID) }
    fun requestConsent() = simulate { m -> tripId()?.let { m.simulateConsentRequest(it) } }
    fun milestone(milestone: Milestone) = simulate { m -> tripId()?.let { m.simulateMilestone(it, milestone) } }
    fun activation(success: Boolean) = simulate { m -> pendingValidation.value?.let { m.simulateActivationResult(it.consentRequestId, success) } }
    fun cancelTrip() = simulate { m -> tripId()?.let { m.simulateTripCancelled(it) } }
    fun completeTrip() = simulate { m -> tripId()?.let { m.simulateTripCompleted(it) } }
    fun resendDuplicate() = simulate { m -> m.resendLastEvent() }

    fun setDeviceOffline(offline: Boolean) = connectivity.setSimulatedOffline(offline)
    fun setServerDown(down: Boolean) { mock?.updateControls { it.copy(serverReachable = !down) } }
    fun failNextThree() { mock?.updateControls { it.copy(failNextSubmissions = 3) } }
    fun setMode(mode: ConsentValidationMode) { mock?.updateControls { it.copy(consentValidationMode = mode) } }
    fun syncNow() { viewModelScope.launch { sync.runNow() } }
}

@Composable
fun DemoScreen(nav: Nav, vm: DemoViewModel = hiltViewModel()) {
    val controls by vm.controls.collectAsStateWithLifecycle()
    val submissions by vm.submissions.collectAsStateWithLifecycle()
    val offline by vm.simulatedOffline.collectAsStateWithLifecycle()
    val trip by vm.activeTrip.collectAsStateWithLifecycle()
    val pending by vm.pendingValidation.collectAsStateWithLifecycle()
    val last by vm.lastResult.collectAsStateWithLifecycle()

    FtScaffold(title = stringResource(R.string.demo_title), onBack = nav::back, onOpenSync = { nav.to(Routes.SYNC) }) {
        if (!vm.available) {
            EmptyState(Icons.Filled.Science, stringResource(R.string.demo_title))
            return@FtScaffold
        }
        StatusPill(stringResource(R.string.demo_disclaimer), Tone.WARNING)
        last?.let { Text(stringResource(R.string.demo_result, it), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("demo_result")) }

        SectionCard(title = stringResource(R.string.demo_section_events)) {
            BigButton(stringResource(R.string.demo_assign), vm::assignTrip, Modifier.testTag("demo_assign"))
            BigButton(stringResource(R.string.demo_open_voice), { nav.voice(null, userInitiated = true) }, kind = ButtonKind.ACCENT)
            val hasTrip = trip?.isActive == true
            if (!hasTrip) Text(stringResource(R.string.demo_no_trip), style = MaterialTheme.typography.bodySmall)
            BigButton(stringResource(R.string.demo_consent), vm::requestConsent, Modifier.testTag("demo_consent"), enabled = hasTrip)
            BigButton(stringResource(R.string.demo_activation_ok), { vm.activation(true) }, Modifier.testTag("demo_activation_ok"), enabled = pending != null, kind = ButtonKind.SECONDARY)
            BigButton(stringResource(R.string.demo_activation_fail), { vm.activation(false) }, enabled = pending != null, kind = ButtonKind.SECONDARY)
            if (pending == null) Text(stringResource(R.string.demo_no_pending_consent), style = MaterialTheme.typography.bodySmall)
            BigButton(stringResource(R.string.demo_eta), { vm.milestone(Milestone.ETA_TO_LOADING_POINT) }, enabled = hasTrip, kind = ButtonKind.SECONDARY)
            BigButton(stringResource(R.string.demo_arrival), { vm.milestone(Milestone.ARRIVAL_AT_LOADING_POINT) }, enabled = hasTrip, kind = ButtonKind.SECONDARY)
            BigButton(stringResource(R.string.demo_loading), { vm.milestone(Milestone.LOADING_STATUS) }, enabled = hasTrip, kind = ButtonKind.SECONDARY)
            BigButton(stringResource(R.string.demo_duplicate), vm::resendDuplicate, kind = ButtonKind.SECONDARY)
            BigButton(stringResource(R.string.demo_complete), vm::completeTrip, enabled = hasTrip, kind = ButtonKind.SECONDARY)
            BigButton(stringResource(R.string.demo_cancel), vm::cancelTrip, enabled = hasTrip, kind = ButtonKind.DANGER)
        }

        SectionCard(title = stringResource(R.string.demo_validation_mode)) {
            Row {
                listOf(
                    ConsentValidationMode.MANUAL to R.string.mode_manual,
                    ConsentValidationMode.AUTO_APPROVE to R.string.mode_auto_ok,
                    ConsentValidationMode.AUTO_REJECT to R.string.mode_auto_fail,
                ).forEach { (mode, label) ->
                    FilterChip(selected = controls.consentValidationMode == mode, onClick = { vm.setMode(mode) }, label = { Text(stringResource(label)) })
                    Spacer(Modifier.width(8.dp))
                }
            }
        }

        SectionCard(title = stringResource(R.string.demo_section_network)) {
            DemoToggle(stringResource(R.string.demo_device_offline), offline, vm::setDeviceOffline, "demo_offline")
            DemoToggle(stringResource(R.string.demo_server_down), !controls.serverReachable, vm::setServerDown, "demo_server_down")
            BigButton(stringResource(R.string.demo_fail_next), vm::failNextThree, kind = ButtonKind.SECONDARY)
            BigButton(stringResource(R.string.demo_sync_now), vm::syncNow, kind = ButtonKind.SECONDARY)
            BigButton(stringResource(R.string.demo_event_history), { nav.to(Routes.ACTIVITY) }, kind = ButtonKind.SECONDARY)
        }

        SectionCard(title = stringResource(R.string.demo_submissions)) {
            if (submissions.isEmpty()) Text(stringResource(R.string.sync_empty), style = MaterialTheme.typography.bodySmall)
            submissions.asReversed().take(30).forEach { s ->
                Column {
                    Text("${s.eventType} · ${s.outcome}", style = MaterialTheme.typography.bodyMedium)
                    Text("${formatTime(s.receivedAt)} · ${s.idempotencyKey.takeLast(24)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun DemoToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit, tag: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange, modifier = Modifier.testTag(tag))
    }
}
