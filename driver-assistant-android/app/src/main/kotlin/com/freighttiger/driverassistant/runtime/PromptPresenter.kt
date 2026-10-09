package com.freighttiger.driverassistant.runtime

import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.PromptStatus
import com.freighttiger.driverassistant.domain.workflow.PromptScheduler
import com.freighttiger.driverassistant.platform.lifecycle.AudioPolicy
import com.freighttiger.driverassistant.platform.notifications.PromptNotifier
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Presents prompts in the least distracting permitted way:
 *  - app visible, auto-speak on, phone not in a call, no conversation running → open the voice
 *    screen, which SPEAKS the question; the mic still needs a tap unless the driver started the
 *    conversation;
 *  - otherwise → actionable notification ("Answer now" / "Later");
 *  - notifications denied → the prompt stays as a pending card on the home screen.
 */
@Singleton
class PromptPresenter @Inject constructor(
    private val scheduler: PromptScheduler,
    private val notifier: PromptNotifier,
    private val audioPolicy: AudioPolicy,
) {
    private val mutex = Mutex()
    private val presented = mutableSetOf<String>()
    private val _openRequests = MutableSharedFlow<String>(extraBufferCapacity = 8)

    /** Prompt ids the UI should open in the voice screen. */
    val openRequests: SharedFlow<String> = _openRequests.asSharedFlow()

    /** Set by the voice screen so a running conversation is never interrupted. */
    @Volatile var conversationActive: Boolean = false

    suspend fun present(prompt: AssistantPrompt) = mutex.withLock {
        presented += prompt.promptId
        // Only open in-app when the UI is actually collecting; otherwise the request would be dropped.
        if (audioPolicy.canAutoPresent() && !conversationActive && _openRequests.subscriptionCount.value > 0) {
            _openRequests.emit(prompt.promptId)
        } else {
            notifier.show(prompt)
        }
    }

    /** Presents the highest-priority prompt that has never been presented in this process. */
    suspend fun presentNewPrompts() {
        val next = scheduler.nextDue() ?: return
        if (next.status == PromptStatus.PENDING && next.promptId !in presented) present(next)
    }

    /** Re-presents the top due prompt, including deferred ones whose delay has passed. */
    suspend fun presentDue(includeDeferred: Boolean) {
        val next = scheduler.nextDue() ?: return
        if (includeDeferred || next.status == PromptStatus.PENDING) present(next)
    }

    fun dismissNotification(promptId: String) = notifier.cancel(promptId)

    /** UI could not open the prompt (e.g. onboarding not finished): fall back to a notification. */
    fun fallbackToNotification(promptId: String, prompt: AssistantPrompt?) {
        if (prompt != null) notifier.show(prompt)
    }
}
