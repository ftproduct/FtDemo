package com.freighttiger.driverassistant.feature.voice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.core.model.InteractionKind
import com.freighttiger.driverassistant.core.model.QuestionContext
import com.freighttiger.driverassistant.data.SettingsRepository
import com.freighttiger.driverassistant.domain.conversation.DialogueEngine
import com.freighttiger.driverassistant.domain.conversation.DialogueOutcome
import com.freighttiger.driverassistant.domain.conversation.DialogueSession
import com.freighttiger.driverassistant.domain.conversation.DialogueStep
import com.freighttiger.driverassistant.domain.conversation.TapResponse
import com.freighttiger.driverassistant.domain.ports.PromptRepository
import com.freighttiger.driverassistant.domain.ports.TripRepository
import com.freighttiger.driverassistant.domain.voice.PromptCatalog
import com.freighttiger.driverassistant.domain.voice.RecognitionResult
import com.freighttiger.driverassistant.domain.voice.SpeechInput
import com.freighttiger.driverassistant.domain.voice.SpeechOutput
import com.freighttiger.driverassistant.domain.voice.SpeechOutputResult
import com.freighttiger.driverassistant.domain.workflow.CoordinatorResult
import com.freighttiger.driverassistant.domain.workflow.PromptScheduler
import com.freighttiger.driverassistant.domain.workflow.TripCoordinator
import com.freighttiger.driverassistant.platform.sync.PromptReminderScheduler
import com.freighttiger.driverassistant.runtime.PromptPresenter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

enum class VoicePhase { LOADING, SPEAKING, WAITING, LISTENING, PROCESSING, DONE }

sealed interface VoiceResult {
    data object Queued : VoiceResult
    data class Refused(val reasonCode: String) : VoiceResult
    data object Deferred : VoiceResult
    data object Info : VoiceResult
    data object NotUnderstood : VoiceResult
}

data class VoiceUiState(
    val phase: VoicePhase = VoicePhase.LOADING,
    val prompt: AssistantPrompt? = null,
    /** Null when the driver opened the assistant without a pending question (free-form command). */
    val context: QuestionContext? = null,
    /** Caption of what the assistant last said (always shown — audio may be unavailable). */
    val caption: String = "",
    /** What the recogniser heard. Shown only to the driver on this screen; never logged. */
    val heard: String? = null,
    val showTapOptions: Boolean = false,
    val showSupport: Boolean = false,
    val micUnavailable: Boolean = false,
    val ttsFailed: Boolean = false,
    val result: VoiceResult? = null,
)

/**
 * Orchestrates one voice conversation: speaks via [SpeechOutput], listens via [SpeechInput] for single
 * bounded turns, delegates interpretation to [DialogueEngine] and applies outcomes via [TripCoordinator].
 *
 * Listening starts automatically only when the driver started this conversation with an explicit
 * action (tap on "Talk", "Answer now", mic button). Otherwise the question is spoken and the driver
 * taps the mic or a button to answer.
 */
@HiltViewModel
class VoiceViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val prompts: PromptRepository,
    private val trips: TripRepository,
    private val scheduler: PromptScheduler,
    private val dialogue: DialogueEngine,
    private val coordinator: TripCoordinator,
    private val catalog: PromptCatalog,
    private val speechOut: SpeechOutput,
    private val speechIn: SpeechInput,
    private val presenter: PromptPresenter,
    private val reminders: PromptReminderScheduler,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val requestedPromptId: String? = savedState.get<String>("promptId")?.takeIf { it.isNotBlank() }
    private var userInitiated: Boolean = savedState.get<Boolean>("user") ?: false

    private val _state = MutableStateFlow(VoiceUiState())
    val state: StateFlow<VoiceUiState> = _state.asStateFlow()

    private var session: DialogueSession? = null
    private var job: Job? = null

    init {
        presenter.conversationActive = true
        job = viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val prompt = requestedPromptId?.let { prompts.get(it) }?.takeIf { it.status.isOpen }
            ?: if (requestedPromptId == null) scheduler.nextDue() else null
        if (prompt == null) {
            // No pending question: free-form command mode ("main pahunch gaya", "ek ghanta", "support").
            _state.value = VoiceUiState(phase = VoicePhase.SPEAKING, caption = catalog.commandHelp(), showTapOptions = false)
            speak(catalog.commandHelp())
            if (userInitiated) listenForCommand() else _state.update { it.copy(phase = VoicePhase.WAITING) }
            return
        }
        presenter.dismissNotification(prompt.promptId)
        val trip = trips.get(prompt.tripId)
        val next = trip?.let { coordinator.nextActionFor(it) } ?: com.freighttiger.driverassistant.domain.voice.NextAction.NONE
        prompt.consentRequestId?.let { coordinator.onConsentPromptDelivered(it) }
        scheduler.markDelivered(prompt.promptId)
        _state.value = VoiceUiState(phase = VoicePhase.SPEAKING, prompt = prompt, context = prompt.type.questionContext)
        handle(dialogue.start(prompt, trip, next))
    }

    private suspend fun handle(step: DialogueStep) {
        session = step.session
        val prompt = step.session.prompt
        when (step) {
            is DialogueStep.Ask -> {
                val completed = speak(step.speech)
                if (step.listen && userInitiated && completed && !_state.value.micUnavailable) {
                    listen()
                } else {
                    _state.update { it.copy(phase = VoicePhase.WAITING, showTapOptions = true) }
                }
            }
            is DialogueStep.Complete -> {
                _state.update { it.copy(phase = VoicePhase.PROCESSING, showTapOptions = false) }
                val result = coordinator.applyOutcome(prompt, step.outcome)
                val voiceResult = when {
                    result is CoordinatorResult.Refused -> VoiceResult.Refused(result.reasonCode)
                    step.outcome is DialogueOutcome.Acknowledged -> VoiceResult.Info
                    else -> VoiceResult.Queued
                }
                if (voiceResult is VoiceResult.Refused) {
                    _state.update { it.copy(result = voiceResult) }
                } else {
                    speak(step.speech)
                }
                _state.update { it.copy(phase = VoicePhase.DONE, result = voiceResult, showSupport = false) }
            }
            is DialogueStep.Defer -> {
                defer(prompt)
                speak(step.speech)
                _state.update { it.copy(phase = VoicePhase.DONE, result = VoiceResult.Deferred, showTapOptions = false) }
            }
            is DialogueStep.Escalate -> {
                coordinator.recordInteraction(prompt, InteractionKind.PROMPT_ESCALATED)
                speak(step.speech)
                _state.update { it.copy(phase = VoicePhase.WAITING, showTapOptions = true, showSupport = true) }
            }
            is DialogueStep.FallbackToTap -> {
                _state.update { it.copy(micUnavailable = true) }
                speak(step.speech)
                _state.update { it.copy(phase = VoicePhase.WAITING, showTapOptions = true) }
            }
        }
    }

    /** Returns true if the text was spoken to completion (false if stopped or TTS unavailable). */
    private suspend fun speak(text: String): Boolean {
        if (text.isBlank()) return true
        _state.update { it.copy(phase = VoicePhase.SPEAKING, caption = text) }
        val result = speechOut.speak(text, catalog.languageTag)
        if (result is SpeechOutputResult.Failed) _state.update { it.copy(ttsFailed = true) }
        // If TTS is unavailable the caption is on screen; still allow the turn to continue.
        return result is SpeechOutputResult.Completed || result is SpeechOutputResult.Failed
    }

    private suspend fun listen() {
        val current = session ?: return
        _state.update { it.copy(phase = VoicePhase.LISTENING, heard = null) }
        val result = speechIn.listenOnce(catalog.languageTag)
        val heard = (result as? RecognitionResult.Recognized)?.utterance?.best?.text
        _state.update { it.copy(phase = VoicePhase.PROCESSING, heard = heard) }
        handle(dialogue.onRecognition(current, result))
    }

    private suspend fun listenForCommand() {
        _state.update { it.copy(phase = VoicePhase.LISTENING, heard = null) }
        val result = speechIn.listenOnce(catalog.languageTag)
        if (result !is RecognitionResult.Recognized) {
            _state.update { it.copy(phase = VoicePhase.WAITING, micUnavailable = result is RecognitionResult.Error && isMicProblem(result)) }
            return
        }
        val heard = result.utterance.best?.text
        val outcome = dialogue.interpretCommand(result.utterance)
        if (outcome == null) {
            _state.update { it.copy(heard = heard, result = VoiceResult.NotUnderstood) }
            speak(catalog.commandNotUnderstood())
            _state.update { it.copy(phase = VoicePhase.WAITING) }
            return
        }
        _state.update { it.copy(phase = VoicePhase.PROCESSING, heard = heard) }
        val trip = trips.activeTrip()?.takeIf { it.isActive }
        val applied = coordinator.applyCommand(trip?.tripId, outcome)
        if (applied is CoordinatorResult.Refused) {
            _state.update { it.copy(phase = VoicePhase.DONE, result = VoiceResult.Refused(applied.reasonCode)) }
        } else {
            speak(dialogue.confirmationFor(outcome))
            _state.update { it.copy(phase = VoicePhase.DONE, result = VoiceResult.Queued) }
        }
    }

    private fun isMicProblem(r: RecognitionResult.Error) =
        r.kind == com.freighttiger.driverassistant.domain.voice.SpeechErrorKind.PERMISSION_DENIED ||
            r.kind == com.freighttiger.driverassistant.domain.voice.SpeechErrorKind.UNAVAILABLE

    private suspend fun defer(prompt: AssistantPrompt) {
        val updated = coordinator.deferPrompt(prompt, Duration.ofMinutes(settings.current.deferMinutes.toLong()))
        updated?.deferredUntil?.let { reminders.remindAt(it) }
    }

    // ------------------------------------------------------------------ UI events

    /** Explicit mic tap: the only way listening starts without a prior driver action. */
    fun onMicTapped() {
        userInitiated = true
        restart {
            if (session == null) listenForCommand() else listen()
        }
    }

    fun onTap(response: TapResponse) {
        val current = session ?: return
        restart { handle(dialogue.onTap(current, response)) }
    }

    fun onRepeat() {
        val current = session
        if (current == null) restart { speak(catalog.commandHelp()); _state.update { it.copy(phase = VoicePhase.WAITING) } }
        else onTap(TapResponse.Repeat)
    }

    /** Stops audio output and any listening immediately. */
    fun stopAudio() {
        job?.cancel()
        speechOut.stop()
        speechIn.cancel()
        _state.update {
            if (it.phase == VoicePhase.DONE) it else it.copy(phase = VoicePhase.WAITING, showTapOptions = session != null)
        }
    }

    private fun restart(block: suspend () -> Unit) {
        job?.cancel()
        speechOut.stop()
        speechIn.cancel()
        job = viewModelScope.launch { block() }
    }

    override fun onCleared() {
        presenter.conversationActive = false
        speechOut.stop()
        speechIn.cancel()
    }
}
