package com.freighttiger.driverassistant.feature.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.ConsentRecord
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.domain.ports.ConsentRepository
import com.freighttiger.driverassistant.domain.ports.TripRepository
import com.freighttiger.driverassistant.domain.workflow.CoordinatorResult
import com.freighttiger.driverassistant.domain.workflow.TripCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripScreenState(
    val loading: Boolean = true,
    val trip: Trip? = null,
    val consents: List<ConsentRecord> = emptyList(),
)

/** Result of the last tap action. Queued means "saved locally and being sent" — not "delivered". */
sealed interface ActionFeedback {
    data object Queued : ActionFeedback
    data class Refused(val reasonCode: String) : ActionFeedback
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TripViewModel @Inject constructor(
    trips: TripRepository,
    consents: ConsentRepository,
    private val coordinator: TripCoordinator,
) : ViewModel() {

    private val tripFlow = trips.observeActiveTrip()

    val state: StateFlow<TripScreenState> = combine(
        tripFlow,
        tripFlow.flatMapLatest { t -> if (t == null) flowOf(emptyList()) else consents.observeForTrip(t.tripId) },
    ) { trip, consentList -> TripScreenState(false, trip, consentList) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TripScreenState())

    private val _feedback = MutableStateFlow<ActionFeedback?>(null)
    val feedback: StateFlow<ActionFeedback?> = _feedback.asStateFlow()

    fun reportEta(minutes: Int) = perform { tripId -> coordinator.reportEta(tripId, minutes, approximate = false, method = CaptureMethod.TAP) }
    fun confirmArrival(arrived: Boolean) = perform { tripId -> coordinator.confirmArrival(tripId, arrived, CaptureMethod.TAP) }
    fun reportLoading(status: LoadingStatus) = perform { tripId -> coordinator.reportLoadingStatus(tripId, status, CaptureMethod.TAP) }

    private fun perform(action: suspend (String) -> CoordinatorResult) {
        val tripId = state.value.trip?.tripId ?: return
        viewModelScope.launch {
            _feedback.value = when (val r = action(tripId)) {
                is CoordinatorResult.Queued, CoordinatorResult.NothingToSend -> ActionFeedback.Queued
                is CoordinatorResult.Refused -> ActionFeedback.Refused(r.reasonCode)
            }
        }
    }
}
