package com.freighttiger.driverassistant.platform.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.freighttiger.driverassistant.di.ApplicationScope
import com.freighttiger.driverassistant.domain.ports.PromptRepository
import com.freighttiger.driverassistant.domain.workflow.TripCoordinator
import com.freighttiger.driverassistant.platform.sync.PromptReminderScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Handles the "Later" notification action: defers the prompt and schedules a reminder. */
class PromptActionReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun prompts(): PromptRepository
        fun coordinator(): TripCoordinator
        fun notifier(): PromptNotifier
        fun reminders(): PromptReminderScheduler
        @ApplicationScope fun scope(): CoroutineScope
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DEFER) return
        val promptId = intent.getStringExtra(EXTRA_PROMPT_ID) ?: return
        val deps = EntryPointAccessors.fromApplication(context.applicationContext, Deps::class.java)
        val pending = goAsync()
        deps.scope().launch {
            try {
                deps.notifier().cancel(promptId)
                val prompt = deps.prompts().get(promptId) ?: return@launch
                deps.coordinator().deferPrompt(prompt)?.deferredUntil?.let { deps.reminders().remindAt(it) }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_DEFER = "com.freighttiger.driverassistant.action.DEFER_PROMPT"
        const val EXTRA_PROMPT_ID = "prompt_id"
    }
}
