package com.freighttiger.driverassistant.platform.push

import android.content.Context
import android.util.Log
import com.freighttiger.driverassistant.core.network.push.InboundEventParser
import com.freighttiger.driverassistant.core.network.push.ParseResult
import com.freighttiger.driverassistant.di.ApplicationScope
import com.freighttiger.driverassistant.di.BackendMode
import com.freighttiger.driverassistant.di.BackendSelection
import com.freighttiger.driverassistant.domain.backend.BackendResult
import com.freighttiger.driverassistant.domain.backend.code
import com.freighttiger.driverassistant.domain.ports.SessionRepository
import com.freighttiger.driverassistant.runtime.AssistantRuntime
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Receives Freight Tiger trip events as FCM data messages (key `ft_event`). Events go through the
 * same processor as every other source. A push never starts the microphone or speech by itself
 * while the app is in the background; it results in an actionable notification.
 */
class FtMessagingService : FirebaseMessagingService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun parser(): InboundEventParser
        fun runtime(): AssistantRuntime
        fun registrar(): PushTokenRegistrar
    }

    private val deps by lazy { EntryPointAccessors.fromApplication(applicationContext, Deps::class.java) }

    override fun onMessageReceived(message: RemoteMessage) {
        when (val parsed = deps.parser().fromPushData(message.data)) {
            is ParseResult.Invalid -> Log.w(TAG, "Rejected push payload: ${parsed.reasonCode}")
            // onMessageReceived runs on a background thread with a limited time budget.
            is ParseResult.Parsed -> runBlocking { withTimeoutOrNull(HANDLE_TIMEOUT_MS) { deps.runtime().handleInbound(parsed.event) } }
        }
    }

    override fun onNewToken(token: String) = deps.registrar().onNewToken(token)

    private companion object {
        const val TAG = "FtdaPush"
        const val HANDLE_TIMEOUT_MS = 8_000L
    }
}

enum class PushStatus { SIMULATED, NOT_CONFIGURED, PENDING, REGISTERED, FAILED }

/** Registers the FCM token with the backend (proposed `PUT /devices/push-token`). */
@Singleton
class PushTokenRegistrar @Inject constructor(
    @ApplicationContext private val context: Context,
    private val selection: BackendSelection,
    private val sessions: SessionRepository,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val _status = MutableStateFlow(initialStatus())
    val status: StateFlow<PushStatus> = _status.asStateFlow()

    private fun initialStatus() = when {
        selection.mode == BackendMode.DEMO_SIMULATED -> PushStatus.SIMULATED
        !firebaseConfigured() -> PushStatus.NOT_CONFIGURED
        else -> PushStatus.PENDING
    }

    /** Call after login and on app start. No-op in demo mode or without Firebase config. */
    fun refresh() {
        if (_status.value == PushStatus.SIMULATED || !firebaseConfigured()) return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { onNewToken(it) }
    }

    fun onNewToken(token: String) {
        if (selection.mode != BackendMode.REMOTE) return
        scope.launch {
            val session = sessions.current() ?: return@launch
            _status.value = when (val r = selection.backend.registerPushToken(session.profile.driverId, token)) {
                is BackendResult.Success -> PushStatus.REGISTERED
                is BackendResult.Failure -> {
                    Log.w("FtdaPush", "Token registration failed: ${r.error.code}")
                    PushStatus.FAILED
                }
            }
        }
    }

    private fun firebaseConfigured() = FirebaseApp.getApps(context).isNotEmpty()
}
