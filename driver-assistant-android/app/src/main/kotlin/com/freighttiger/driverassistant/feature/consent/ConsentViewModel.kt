package com.freighttiger.driverassistant.feature.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.ConsentDecision
import com.freighttiger.driverassistant.core.model.ConsentRecord
import com.freighttiger.driverassistant.core.model.PromptType
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.domain.conversation.DialogueOutcome
import com.freighttiger.driverassistant.domain.ports.ConsentRepository
import com.freighttiger.driverassistant.domain.ports.PromptRepository
import com.freighttiger.driverassistant.domain.ports.TripRepository
import com.freighttiger.driverassistant.domain.workflow.CoordinatorResult
import com.freighttiger.driverassistant.domain.workflow.TripCoordinator
import com.freighttiger.driverassistant.feature.trips.ActionFeedback
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
import java.time.Instant
import javax.inject.Inject

data class ConsentScreenState(
    val loading: Boolean = true,
    val trip: Trip? = null,
    /** Request the driver can answer now (unexpired REQUESTED / AWAITING_RESPONSE). */
    val open: ConsentRecord? = null,
    val latest: ConsentRecord? = null,
    val history: List<ConsentRecord> = emptyList(),
    val openPrompt: AssistantPrompt? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConsentViewModel @Inject constructor(
    trips: TripRepository,
    consents: ConsentRepository,
    prompts: PromptRepository,
    private val coordinator: TripCoordinator,
) : ViewModel() {

    private val tripFlow = trips.observeActiveTrip()

    val state: StateFlow<ConsentScreenState> = combine(
        tripFlow,
        tripFlow.flatMapLatest { t -> if (t == null) flowOf(emptyList()) else consents.observeForTrip(t.tripId) },
        prompts.observeAll(),
    ) { trip, list, promptList ->
        val now = Instant.now()
        val open = list.firstOrNull { it.state.acceptsResponse && !it.isExpiredAt(now) }
        ConsentScreenState(
            loading = false,
            trip = trip,
            open = open,
            latest = list.firstOrNull(),
            history = list.drop(1),
            openPrompt = open?.let { o -> promptList.firstOrNull { it.type == PromptType.CONSENT && it.consentRequestId == o.consentRequestId && it.status.isOpen } },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConsentScreenState())

    private val _feedback = MutableStateFlow<ActionFeedback?>(null)
    val feedback: StateFlow<ActionFeedback?> = _feedback.asStateFlow()

    /** Explicit on-screen decision. Never inferred; both options are equally prominent. */
    fun decide(decision: ConsentDecision) {
        val s = state.value
        val open = s.open ?: return
        viewModelScope.launch {
            val result = s.openPrompt?.let {
                coordinator.applyOutcome(it, DialogueOutcome.Consent(decision, CaptureMethod.TAP, null, null))
            } ?: coordinator.submitConsentDecision(open.consentRequestId, decision, CaptureMethod.TAP, null)
            _feedback.value = result.toFeedback()
        }
    }

    fun withdraw() {
        val tripId = state.value.trip?.tripId ?: return
        viewModelScope.launch { _feedback.value = coordinator.withdrawConsent(tripId).toFeedback() }
    }

    private fun CoordinatorResult.toFeedback() = when (this) {
        is CoordinatorResult.Refused -> ActionFeedback.Refused(reasonCode)
        else -> ActionFeedback.Queued
    }
}
