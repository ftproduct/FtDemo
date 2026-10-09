package com.freighttiger.driverassistant.runtime

import android.util.Log
import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.core.model.InboundTripEvent
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.data.SettingsRepository
import com.freighttiger.driverassistant.di.ApplicationScope
import com.freighttiger.driverassistant.di.BackendSelection
import com.freighttiger.driverassistant.domain.backend.BackendResult
import com.freighttiger.driverassistant.domain.ports.ConnectivityMonitor
import com.freighttiger.driverassistant.domain.ports.ConsentRepository
import com.freighttiger.driverassistant.domain.ports.TripRepository
import com.freighttiger.driverassistant.domain.workflow.ProcessingResult
import com.freighttiger.driverassistant.domain.workflow.PromptScheduler
import com.freighttiger.driverassistant.domain.workflow.TripEventProcessor
import com.freighttiger.driverassistant.platform.lifecycle.AppForegroundTracker
import com.freighttiger.driverassistant.platform.push.PushTokenRegistrar
import com.freighttiger.driverassistant.platform.sync.SyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-wide orchestration: routes inbound events (FCM, demo channel, state polling) through the
 * processor, presents resulting prompts, and triggers outbox sync when connectivity returns.
 */
@Singleton
class AssistantRuntime @Inject constructor(
    private val processor: TripEventProcessor,
    private val presenter: PromptPresenter,
    private val sync: SyncScheduler,
    private val connectivity: ConnectivityMonitor,
    private val selection: BackendSelection,
    private val trips: TripRepository,
    private val consents: ConsentRepository,
    private val scheduler: PromptScheduler,
    private val settings: SettingsRepository,
    private val foreground: AppForegroundTracker,
    private val pushRegistrar: PushTokenRegistrar,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private var started = false

    fun start() {
        if (started) return
        started = true

        // Demo mode: the simulated backend's event channel stands in for FCM.
        selection.mock?.let { mock -> scope.launch { mock.pushEvents.collect { handleInbound(it) } } }

        scope.launch { settings.settings.collect { scheduler.policy = it.toPromptPolicy() } }

        scope.launch {
            connectivity.isOnline.distinctUntilChanged().filter { it }.collect { sync.requestSync() }
        }

        // While visible, reconcile with the authoritative trip state (covers missing/late pushes).
        scope.launch {
            foreground.isForeground.collectLatest { visible ->
                if (!visible) return@collectLatest
                pushRegistrar.refresh()
                while (true) { // cancelled by collectLatest when the app leaves the foreground
                    reconcileActiveTrip()
                    delay(RECONCILE_INTERVAL_MS)
                }
            }
        }
    }

    suspend fun handleInbound(event: InboundTripEvent): ProcessingResult {
        val result = processor.process(event)
        if (result is ProcessingResult.Applied) result.prompt?.let { presenter.present(it) }
        if (result !is ProcessingResult.Applied) Log.i(TAG, "Inbound ${event.type} not applied: ${result::class.simpleName}")
        return result
    }

    suspend fun reconcileActiveTrip() {
        val trip = trips.activeTrip() ?: return
        if (trip.status.isTerminal) return
        val awaitingBackend = trip.trackingStatus == TrackingStatus.PENDING_ACTIVATION ||
            trip.trackingStatus == TrackingStatus.STOP_REQUESTED ||
            consents.forTrip(trip.tripId).any { it.state == ConsentState.GRANTED_PENDING_VALIDATION }
        if (!awaitingBackend && selection.mock == null) return
        when (val state = selection.backend.fetchTripState(trip.tripId)) {
            is BackendResult.Success -> processor.applySnapshot(state.value, selection.backend.isSimulated)?.let { presenter.present(it) }
            is BackendResult.Failure -> Unit
        }
    }

    private companion object {
        const val TAG = "FtdaRuntime"
        const val RECONCILE_INTERVAL_MS = 60_000L
    }
}
