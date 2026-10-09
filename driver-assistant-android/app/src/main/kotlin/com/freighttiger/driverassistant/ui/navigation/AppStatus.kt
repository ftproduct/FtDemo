package com.freighttiger.driverassistant.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freighttiger.driverassistant.AppConfig
import com.freighttiger.driverassistant.core.model.OutboxStatus
import com.freighttiger.driverassistant.data.SettingsRepository
import com.freighttiger.driverassistant.domain.ports.ConnectivityMonitor
import com.freighttiger.driverassistant.domain.ports.OutboxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** App-wide status shown on every screen (demo label, connectivity, sync backlog). */
data class AppStatus(
    val demoMode: Boolean = false,
    val online: Boolean = true,
    val pending: Int = 0,
    val failed: Int = 0,
    val onboardingComplete: Boolean = false,
)

val LocalAppStatus = compositionLocalOf { AppStatus() }

@HiltViewModel
class AppStatusViewModel @Inject constructor(
    config: AppConfig,
    connectivity: ConnectivityMonitor,
    outbox: OutboxRepository,
    settings: SettingsRepository,
) : ViewModel() {
    val status: StateFlow<AppStatus> = combine(connectivity.isOnline, outbox.observeAll(), settings.settings) { online, events, s ->
        AppStatus(
            demoMode = config.demoMode,
            online = online,
            // Only driver-facing updates count; background interaction telemetry is not shown as backlog.
            pending = events.count { it.status.isOpen },
            failed = events.count { it.status == OutboxStatus.FAILED_MAX_RETRIES || it.status == OutboxStatus.FAILED_PERMANENT },
            onboardingComplete = s.onboardingComplete,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppStatus(demoMode = config.demoMode, onboardingComplete = settings.current.onboardingComplete))
}
