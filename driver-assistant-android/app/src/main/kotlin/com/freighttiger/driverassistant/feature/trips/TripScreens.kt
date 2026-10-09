package com.freighttiger.driverassistant.feature.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.core.model.LoadingStatus
import com.freighttiger.driverassistant.core.model.Trip
import com.freighttiger.driverassistant.ui.components.BigButton
import com.freighttiger.driverassistant.ui.components.ButtonKind
import com.freighttiger.driverassistant.ui.components.EmptyState
import com.freighttiger.driverassistant.ui.components.ErrorMessage
import com.freighttiger.driverassistant.ui.components.FtScaffold
import com.freighttiger.driverassistant.ui.components.LabeledValue
import com.freighttiger.driverassistant.ui.components.LoadingState
import com.freighttiger.driverassistant.ui.components.SectionCard
import com.freighttiger.driverassistant.ui.components.StatusPill
import com.freighttiger.driverassistant.ui.components.SuccessMessage
import com.freighttiger.driverassistant.ui.components.Tone
import com.freighttiger.driverassistant.ui.navigation.Nav
import com.freighttiger.driverassistant.ui.navigation.Routes
import com.freighttiger.driverassistant.ui.text.consentRes
import com.freighttiger.driverassistant.ui.text.consentTone
import com.freighttiger.driverassistant.ui.text.formatTime
import com.freighttiger.driverassistant.ui.text.loadingRes
import com.freighttiger.driverassistant.ui.text.reasonText
import com.freighttiger.driverassistant.ui.text.trackingRes
import com.freighttiger.driverassistant.ui.text.trackingTone
import com.freighttiger.driverassistant.ui.text.tripStatusRes

@Composable
private fun WithTrip(nav: Nav, titleRes: Int, vm: TripViewModel, content: @Composable (Trip) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    FtScaffold(title = stringResource(titleRes), onBack = nav::back, onOpenSync = { nav.to(Routes.SYNC) }) {
        val trip = state.trip
        when {
            state.loading -> LoadingState()
            trip == null -> EmptyState(Icons.Filled.LocalShipping, stringResource(R.string.home_no_trip_title), stringResource(R.string.home_no_trip_body))
            else -> {
                if (trip.simulated) StatusPill(stringResource(R.string.simulated_badge), Tone.WARNING)
                content(trip)
            }
        }
    }
}

@Composable
fun TripDetailsScreen(nav: Nav, vm: TripViewModel = hiltViewModel()) {
    WithTrip(nav, R.string.trip_details_title, vm) { trip ->
        SectionCard {
            LabeledValue(stringResource(R.string.label_trip_id), trip.tripId)
            LabeledValue(stringResource(R.string.label_trip_status), stringResource(tripStatusRes(trip.status)))
            LabeledValue(stringResource(R.string.label_vehicle), trip.vehicleRegistration)
            LabeledValue(stringResource(R.string.label_origin), "${trip.origin.name}, ${trip.origin.city}")
            LabeledValue(stringResource(R.string.label_destination), "${trip.destination.name}, ${trip.destination.city}")
        }
        SectionCard(title = stringResource(R.string.label_loading_point)) {
            LabeledValue(stringResource(R.string.label_loading_point), trip.loadingPoint.name)
            LabeledValue(stringResource(R.string.label_address), "${trip.loadingPoint.address}, ${trip.loadingPoint.city}")
            trip.loadingPoint.reportingTime?.let { LabeledValue(stringResource(R.string.label_reporting_time), formatTime(it)) }
            LabeledValue(stringResource(R.string.label_loading_status), stringResource(loadingRes(trip.loadingStatus)))
        }
        // Driver-reported values are shown separately from backend/GPS verification.
        SectionCard(title = stringResource(R.string.label_driver_eta)) {
            val eta = trip.driverReportedEta
            Text(
                if (eta == null) stringResource(R.string.not_reported)
                else stringResource(R.string.driver_eta_value, formatTime(eta.estimatedArrivalAt), formatTime(eta.reportedAt)),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        SectionCard(title = stringResource(R.string.label_driver_arrival)) {
            val arrival = trip.driverReportedArrival
            Text(
                when {
                    arrival == null -> stringResource(R.string.not_reported)
                    arrival.arrived -> stringResource(R.string.driver_arrived)
                    else -> stringResource(R.string.driver_not_arrived)
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            LabeledValue(
                stringResource(R.string.label_verified_arrival),
                trip.verifiedArrival?.let { stringResource(R.string.verified_at, formatTime(it.verifiedAt)) } ?: stringResource(R.string.not_verified),
            )
        }
        BigButton(stringResource(R.string.menu_report), { nav.to(Routes.REPORT) })
    }
}

@Composable
fun TrackingStatusScreen(nav: Nav, vm: TripViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    WithTrip(nav, R.string.tracking_title, vm) { trip ->
        SectionCard(title = stringResource(R.string.label_tracking)) {
            StatusPill(stringResource(trackingRes(trip.trackingStatus)), trackingTone(trip.trackingStatus), Modifier.testTag("tracking_status"))
            Text(stringResource(R.string.tracking_how), style = MaterialTheme.typography.bodyMedium)
        }
        state.consents.firstOrNull()?.let { consent ->
            SectionCard(title = stringResource(R.string.consent_status)) {
                StatusPill(stringResource(consentRes(consent.state)), consentTone(consent.state))
            }
        }
        BigButton(stringResource(R.string.tracking_open_consent), { nav.to(Routes.CONSENT) }, kind = ButtonKind.SECONDARY)
    }
}

@Composable
fun ReportScreen(nav: Nav, vm: TripViewModel = hiltViewModel()) {
    val feedback by vm.feedback.collectAsStateWithLifecycle()
    WithTrip(nav, R.string.report_title, vm) { trip ->
        Text(stringResource(R.string.safety_note), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        when (val f = feedback) {
            ActionFeedback.Queued -> SuccessMessage(stringResource(R.string.report_queued))
            is ActionFeedback.Refused -> ErrorMessage(stringResource(R.string.report_refused, reasonText(f.reasonCode)))
            null -> Unit
        }
        BigButton(stringResource(R.string.report_by_voice), { nav.voice(null, userInitiated = true) }, icon = Icons.Filled.Mic, kind = ButtonKind.ACCENT)

        if (trip.loadingStatus == LoadingStatus.NOT_REACHED) {
            SectionCard(title = stringResource(R.string.arrival_section)) {
                BigButton(stringResource(R.string.arrival_yes), { vm.confirmArrival(true) }, Modifier.testTag("arrival_yes"), icon = Icons.Filled.CheckCircle)
                BigButton(stringResource(R.string.arrival_no), { vm.confirmArrival(false) }, kind = ButtonKind.SECONDARY)
            }
            SectionCard(title = stringResource(R.string.eta_section)) {
                listOf(15 to R.string.eta_15, 30 to R.string.eta_30, 60 to R.string.eta_60, 120 to R.string.eta_120, 240 to R.string.eta_240)
                    .chunked(2)
                    .forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { (minutes, label) ->
                                BigButton(
                                    stringResource(label),
                                    { vm.reportEta(minutes) },
                                    Modifier.weight(1f).testTag("eta_$minutes"),
                                    icon = Icons.Filled.Schedule,
                                    kind = ButtonKind.SECONDARY,
                                )
                            }
                        }
                    }
            }
        }

        val options = trip.loadingStatus.allowedNext()
        if (options.isNotEmpty()) {
            SectionCard(title = stringResource(R.string.loading_section)) {
                LabeledValue(stringResource(R.string.label_loading_status), stringResource(loadingRes(trip.loadingStatus)))
                // Only transitions valid for the current state are offered.
                options.sortedBy { it.ordinal }.forEach { status ->
                    BigButton(
                        stringResource(loadingRes(status)),
                        { vm.reportLoading(status) },
                        Modifier.testTag("loading_${status.name}"),
                        kind = if (status == LoadingStatus.ISSUE_REPORTED) ButtonKind.DANGER else ButtonKind.PRIMARY,
                    )
                }
            }
        }
    }
}
