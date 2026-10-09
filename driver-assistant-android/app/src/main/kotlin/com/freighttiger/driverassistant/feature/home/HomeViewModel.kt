package com.freighttiger.driverassistant.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freighttiger.driverassistant.core.model.ActivityEntry
import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.ConsentRecord
import com.freighttiger.driverassistant.core.model.DriverSession
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.domain.ports.ActivityLog
import com.freighttiger.driverassistant.domain.ports.ConsentRepository
import com.freighttiger.driverassistant.domain.ports.PromptRepository
import com.freighttiger.driverassistant.domain.ports.SessionRepository
import com.freighttiger.driverassistant.domain.ports.TripRepository
import com.freighttiger.driverassistant.domain.voice.NextAction
import com.freighttiger.driverassistant.domain.workflow.TripCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import javax.inject.Inject

data class HomeState(
    val loading: Boolean = true,
    val session: DriverSession? = null,
    val trip: Trip? = null,
    val consent: ConsentRecord? = null,
    val nextAction: NextAction = NextAction.NONE,
    val pendingPrompt: AssistantPrompt? = null,
    val recent: List<ActivityEntry> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    sessions: SessionRepository,
    trips: TripRepository,
    consents: ConsentRepository,
    prompts: PromptRepository,
    activity: ActivityLog,
    private val coordinator: TripCoordinator,
) : ViewModel() {

    private val tripFlow = trips.observeActiveTrip()
    private val consentFlow = tripFlow.flatMapLatest { trip ->
        if (trip == null) flowOf(emptyList()) else consents.observeForTrip(trip.tripId)
    }

    val state: StateFlow<HomeState> = combine(
        sessions.observe(),
        tripFlow,
        consentFlow,
        prompts.observeAll(),
        activity.observeRecent(5),
    ) { session, trip, consentList, promptList, recent ->
        val now = Instant.now()
        val pending = promptList
            .filter { it.status.isOpen && it.type.expectsResponse && (it.deferredUntil == null || !now.isBefore(it.deferredUntil)) }
            .filter { trip == null || it.tripId == trip.tripId }
            .maxWithOrNull(compareBy<AssistantPrompt> { it.type.priority }.thenByDescending { it.createdAt })
        HomeState(false, session, trip, consentList.firstOrNull(), NextAction.NONE, pending, recent)
    }.mapLatest { s -> s.copy(nextAction = s.trip?.let { coordinator.nextActionFor(it) } ?: NextAction.NONE) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())
}
