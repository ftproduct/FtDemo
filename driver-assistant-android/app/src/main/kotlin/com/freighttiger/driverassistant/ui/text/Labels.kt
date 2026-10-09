package com.freighttiger.driverassistant.ui.text

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.core.model.ActivityKind
import com.freighttiger.driverassistant.core.model.CaptureMethod
import com.freighttiger.driverassistant.core.model.ConsentState
import com.freighttiger.driverassistant.core.model.InboundOutcome
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.OutboundEventType
import com.freighttiger.driverassistant.core.model.OutboxStatus
import com.freighttiger.driverassistant.core.model.PromptType
import com.freighttiger.driverassistant.core.model.SubmissionState
import com.freighttiger.driverassistant.core.model.TrackingStatus
import com.freighttiger.driverassistant.core.model.TripStatus
import com.freighttiger.driverassistant.domain.voice.NextAction
import com.freighttiger.driverassistant.ui.components.Tone
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Maps domain enums to centralised string resources. Keep every user-visible label here.

@StringRes fun promptTitleRes(type: PromptType): Int = when (type) {
    PromptType.CONSENT -> R.string.prompt_consent
    PromptType.ETA -> R.string.prompt_eta
    PromptType.ARRIVAL -> R.string.prompt_arrival
    PromptType.LOADING_STATUS -> R.string.prompt_loading
    PromptType.TRIP_BRIEFING -> R.string.prompt_briefing
    PromptType.TRACKING_RESULT -> R.string.prompt_tracking_result
    PromptType.TRIP_CANCELLED -> R.string.prompt_trip_cancelled
    PromptType.TRIP_COMPLETED -> R.string.prompt_trip_completed
}

@StringRes fun trackingRes(s: TrackingStatus): Int = when (s) {
    TrackingStatus.NOT_STARTED -> R.string.tracking_not_started
    TrackingStatus.PENDING_ACTIVATION -> R.string.tracking_pending
    TrackingStatus.ACTIVE -> R.string.tracking_active
    TrackingStatus.FAILED -> R.string.tracking_failed
    TrackingStatus.STOP_REQUESTED -> R.string.tracking_stop_requested
    TrackingStatus.STOPPED -> R.string.tracking_stopped
}

fun trackingTone(s: TrackingStatus): Tone = when (s) {
    TrackingStatus.ACTIVE -> Tone.SUCCESS
    TrackingStatus.PENDING_ACTIVATION, TrackingStatus.STOP_REQUESTED -> Tone.WARNING
    TrackingStatus.FAILED -> Tone.DANGER
    else -> Tone.NEUTRAL
}

@StringRes fun consentRes(s: ConsentState): Int = when (s) {
    ConsentState.NOT_REQUESTED -> R.string.consent_state_not_requested
    ConsentState.REQUESTED -> R.string.consent_state_requested
    ConsentState.AWAITING_RESPONSE -> R.string.consent_state_awaiting
    ConsentState.GRANTED_PENDING_VALIDATION -> R.string.consent_state_pending_validation
    ConsentState.GRANTED -> R.string.consent_state_granted
    ConsentState.DECLINED -> R.string.consent_state_declined
    ConsentState.EXPIRED -> R.string.consent_state_expired
    ConsentState.WITHDRAWN -> R.string.consent_state_withdrawn
    ConsentState.VALIDATION_FAILED -> R.string.consent_state_validation_failed
}

fun consentTone(s: ConsentState): Tone = when (s) {
    ConsentState.GRANTED -> Tone.SUCCESS
    ConsentState.REQUESTED, ConsentState.AWAITING_RESPONSE, ConsentState.GRANTED_PENDING_VALIDATION -> Tone.WARNING
    ConsentState.VALIDATION_FAILED -> Tone.DANGER
    else -> Tone.NEUTRAL
}

@StringRes fun submissionRes(s: SubmissionState): Int? = when (s) {
    SubmissionState.NONE -> null
    SubmissionState.CAPTURED_LOCALLY -> R.string.submission_captured
    SubmissionState.SUBMITTED -> R.string.submission_submitted
    SubmissionState.ACCEPTED -> R.string.submission_accepted
    SubmissionState.REJECTED -> R.string.submission_rejected
}

@StringRes fun loadingRes(s: LoadingStatus): Int = when (s) {
    LoadingStatus.NOT_REACHED -> R.string.loading_not_reached
    LoadingStatus.REACHED_LOADING_POINT -> R.string.loading_reached
    LoadingStatus.WAITING_FOR_LOADING -> R.string.loading_waiting
    LoadingStatus.LOADING_STARTED -> R.string.loading_started
    LoadingStatus.LOADING_COMPLETED -> R.string.loading_completed
    LoadingStatus.ISSUE_REPORTED -> R.string.loading_issue
}

@StringRes fun tripStatusRes(s: TripStatus): Int = when (s) {
    TripStatus.ASSIGNED -> R.string.trip_assigned
    TripStatus.IN_PROGRESS -> R.string.trip_in_progress
    TripStatus.CANCELLED -> R.string.trip_cancelled
    TripStatus.COMPLETED -> R.string.trip_completed
}

@StringRes fun outboxStatusRes(s: OutboxStatus): Int = when (s) {
    OutboxStatus.PENDING -> R.string.outbox_pending
    OutboxStatus.IN_FLIGHT -> R.string.outbox_in_flight
    OutboxStatus.RETRY_SCHEDULED -> R.string.outbox_retry
    OutboxStatus.ACKNOWLEDGED -> R.string.outbox_ack
    OutboxStatus.FAILED_PERMANENT -> R.string.outbox_failed_permanent
    OutboxStatus.FAILED_MAX_RETRIES -> R.string.outbox_failed_max
    OutboxStatus.CANCELLED_CONFLICT -> R.string.outbox_cancelled
}

fun outboxTone(s: OutboxStatus): Tone = when (s) {
    OutboxStatus.ACKNOWLEDGED -> Tone.SUCCESS
    OutboxStatus.PENDING, OutboxStatus.IN_FLIGHT, OutboxStatus.RETRY_SCHEDULED -> Tone.WARNING
    OutboxStatus.FAILED_PERMANENT, OutboxStatus.FAILED_MAX_RETRIES -> Tone.DANGER
    OutboxStatus.CANCELLED_CONFLICT -> Tone.NEUTRAL
}

@StringRes fun outboundTypeRes(t: OutboundEventType): Int = when (t) {
    OutboundEventType.CONSENT_RESPONSE_CAPTURED -> R.string.ev_consent
    OutboundEventType.CONSENT_WITHDRAWN -> R.string.ev_withdraw
    OutboundEventType.ETA_REPORTED -> R.string.ev_eta
    OutboundEventType.ARRIVAL_REPORTED -> R.string.ev_arrival
    OutboundEventType.LOADING_STATUS_REPORTED -> R.string.ev_loading
    OutboundEventType.SUPPORT_REQUESTED -> R.string.ev_support
    OutboundEventType.ASSISTANT_INTERACTION -> R.string.ev_interaction
}

@StringRes fun activityRes(k: ActivityKind): Int = when (k) {
    ActivityKind.EVENT_RECEIVED -> R.string.act_event_received
    ActivityKind.EVENT_REJECTED -> R.string.act_event_rejected
    ActivityKind.PROMPT_SCHEDULED -> R.string.act_prompt_scheduled
    ActivityKind.PROMPT_SUPPRESSED -> R.string.act_prompt_suppressed
    ActivityKind.PROMPT_DEFERRED -> R.string.act_prompt_deferred
    ActivityKind.DRIVER_RESPONSE -> R.string.act_driver_response
    ActivityKind.CONSENT_UPDATE -> R.string.act_consent
    ActivityKind.TRACKING_UPDATE -> R.string.act_tracking
    ActivityKind.TRIP_UPDATE -> R.string.act_trip
    ActivityKind.SYNC_SUCCESS -> R.string.act_sync_success
    ActivityKind.SYNC_FAILURE -> R.string.act_sync_failure
    ActivityKind.SUPPORT -> R.string.act_support
    ActivityKind.CONFLICT -> R.string.act_conflict
}

fun activityTone(k: ActivityKind): Tone = when (k) {
    ActivityKind.SYNC_SUCCESS -> Tone.SUCCESS
    ActivityKind.SYNC_FAILURE, ActivityKind.CONFLICT, ActivityKind.EVENT_REJECTED -> Tone.DANGER
    ActivityKind.PROMPT_DEFERRED, ActivityKind.PROMPT_SUPPRESSED -> Tone.WARNING
    ActivityKind.DRIVER_RESPONSE, ActivityKind.CONSENT_UPDATE, ActivityKind.TRACKING_UPDATE -> Tone.INFO
    else -> Tone.NEUTRAL
}

@StringRes fun inboundOutcomeRes(o: InboundOutcome): Int = when (o) {
    InboundOutcome.APPLIED -> R.string.out_applied
    InboundOutcome.DUPLICATE -> R.string.out_duplicate
    InboundOutcome.REJECTED -> R.string.out_rejected
    InboundOutcome.IGNORED -> R.string.out_ignored
}

@StringRes fun nextActionRes(a: NextAction): Int = when (a) {
    NextAction.GIVE_TRACKING_CONSENT -> R.string.next_consent
    NextAction.SHARE_ETA -> R.string.next_eta
    NextAction.REACH_LOADING_POINT -> R.string.next_reach
    NextAction.UPDATE_LOADING_STATUS -> R.string.next_loading
    NextAction.NONE -> R.string.next_none
}

@StringRes fun methodRes(m: CaptureMethod): Int = if (m == CaptureMethod.VOICE) R.string.method_voice else R.string.method_tap

/** Human-readable text for a domain/back-end reason code. Unknown codes are shown verbatim. */
@Composable
fun reasonText(code: String): String = when (code) {
    "REQUEST_EXPIRED", "CONSENT_REQUEST_EXPIRED" -> stringResource(R.string.err_request_expired)
    "TRIP_NOT_ACTIVE" -> stringResource(R.string.err_trip_not_active)
    "INVALID_LOADING_TRANSITION" -> stringResource(R.string.err_invalid_loading_transition)
    "ALREADY_AT_LOADING_POINT" -> stringResource(R.string.err_already_at_loading_point)
    "ARRIVAL_ALREADY_REPORTED" -> stringResource(R.string.err_arrival_already_reported)
    "NOT_CONFIGURED" -> stringResource(R.string.err_not_configured)
    "NETWORK", "SIMULATED_UNREACHABLE" -> stringResource(R.string.err_network)
    "INVALID_OTP" -> stringResource(R.string.err_invalid_otp)
    "INVALID_PHONE" -> stringResource(R.string.err_invalid_phone)
    "OTP_EXPIRED" -> stringResource(R.string.err_otp_expired)
    "INVALID_OTP_FORMAT" -> stringResource(R.string.err_invalid_otp_format)
    "STATUS_UNCHANGED" -> stringResource(R.string.err_status_unchanged)
    "NO_ACTIVE_SESSION" -> stringResource(R.string.err_no_session)
    else -> stringResource(R.string.err_other, code)
}

private val timeFormat = DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.forLanguageTag("hi-IN"))

fun formatTime(instant: Instant): String = timeFormat.format(instant.atZone(ZoneId.systemDefault()))
