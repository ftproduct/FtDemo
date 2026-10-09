package com.freighttiger.driverassistant.feature.support

import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.freighttiger.driverassistant.AppConfig
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.core.model.OutboundEvent
import com.freighttiger.driverassistant.core.model.OutboundEventType
import com.freighttiger.driverassistant.core.model.OutboxStatus
import com.freighttiger.driverassistant.core.model.SupportReason
import com.freighttiger.driverassistant.domain.ports.OutboxRepository
import com.freighttiger.driverassistant.domain.ports.TripRepository
import com.freighttiger.driverassistant.domain.workflow.TripCoordinator
import com.freighttiger.driverassistant.ui.components.BigButton
import com.freighttiger.driverassistant.ui.components.ButtonKind
import com.freighttiger.driverassistant.ui.components.FtScaffold
import com.freighttiger.driverassistant.ui.components.SectionCard
import com.freighttiger.driverassistant.ui.components.StatusPill
import com.freighttiger.driverassistant.ui.components.Tone
import com.freighttiger.driverassistant.ui.navigation.Nav
import com.freighttiger.driverassistant.ui.navigation.Routes
import com.freighttiger.driverassistant.ui.text.formatTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupportViewModel @Inject constructor(
    outbox: OutboxRepository,
    private val trips: TripRepository,
    private val coordinator: TripCoordinator,
    config: AppConfig,
) : ViewModel() {
    /** Only a configured, verified number is offered. The app never invents a number. */
    val supportPhone: String? = config.supportPhoneNumber

    val requests: StateFlow<List<OutboundEvent>> = outbox.observeAll()
        .map { list -> list.filter { it.payload.eventType == OutboundEventType.SUPPORT_REQUESTED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun sendRequest() {
        viewModelScope.launch {
            coordinator.requestSupport(trips.activeTrip()?.takeIf { it.isActive }?.tripId, SupportReason.DRIVER_REQUESTED)
        }
    }
}

@Composable
fun SupportScreen(nav: Nav, vm: SupportViewModel = hiltViewModel()) {
    val requests by vm.requests.collectAsStateWithLifecycle()
    val context = LocalContext.current

    FtScaffold(title = stringResource(R.string.support_title), onBack = nav::back, onOpenSync = { nav.to(Routes.SYNC) }) {
        Text(stringResource(R.string.support_body), style = MaterialTheme.typography.bodyLarge)
        BigButton(stringResource(R.string.support_send), vm::sendRequest, Modifier.testTag("support_send"), icon = Icons.Filled.SupportAgent)

        val phone = vm.supportPhone
        if (phone != null) {
            // ACTION_DIAL opens the dialer; the driver places the call. No CALL_PHONE permission needed.
            BigButton(
                stringResource(R.string.support_call),
                { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) },
                icon = Icons.Filled.Call,
                kind = ButtonKind.SECONDARY,
            )
        } else {
            Text(stringResource(R.string.support_no_number), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (requests.isNotEmpty()) {
            SectionCard(title = stringResource(R.string.support_recent)) {
                requests.forEach { r ->
                    Text(formatTime(r.createdAt), style = MaterialTheme.typography.bodySmall)
                    when (r.status) {
                        OutboxStatus.ACKNOWLEDGED -> {
                            StatusPill(stringResource(R.string.support_status_sent), Tone.SUCCESS)
                            // Only claim a callback when the backend explicitly confirmed one.
                            StatusPill(
                                stringResource(if (r.callbackScheduled == true) R.string.support_callback_confirmed else R.string.support_callback_not_confirmed),
                                if (r.callbackScheduled == true) Tone.SUCCESS else Tone.NEUTRAL,
                            )
                        }
                        OutboxStatus.FAILED_PERMANENT, OutboxStatus.FAILED_MAX_RETRIES -> StatusPill(stringResource(R.string.support_failed), Tone.DANGER)
                        else -> StatusPill(stringResource(R.string.support_status_queued), Tone.WARNING)
                    }
                }
            }
        }
    }
}
