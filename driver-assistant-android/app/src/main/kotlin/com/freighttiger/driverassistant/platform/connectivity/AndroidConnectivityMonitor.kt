package com.freighttiger.driverassistant.platform.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.freighttiger.driverassistant.di.ApplicationScope
import com.freighttiger.driverassistant.domain.ports.ConnectivityMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OS network state (validated internet) combined with an optional demo-mode "simulate offline" switch.
 */
@Singleton
class AndroidConnectivityMonitor @Inject constructor(
    @ApplicationContext context: Context,
    @ApplicationScope scope: CoroutineScope,
) : ConnectivityMonitor {

    private val manager = context.getSystemService(ConnectivityManager::class.java)
    private val osOnline = MutableStateFlow(isCurrentlyOnline())
    private val _simulatedOffline = MutableStateFlow(false)

    /** Demo only: forces the app to behave as if offline. */
    val simulatedOffline: StateFlow<Boolean> = _simulatedOffline.asStateFlow()

    override val isOnline: StateFlow<Boolean> = combine(osOnline, _simulatedOffline) { os, simOffline -> os && !simOffline }
        .stateIn(scope, SharingStarted.Eagerly, osOnline.value)

    init {
        manager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { osOnline.value = isCurrentlyOnline() }
            override fun onLost(network: Network) { osOnline.value = false }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                osOnline.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
        })
    }

    fun setSimulatedOffline(offline: Boolean) { _simulatedOffline.value = offline }

    private fun isCurrentlyOnline(): Boolean {
        val caps = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
