package com.freighttiger.driverassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.ui.theme.LocalStatusColors

enum class Tone { SUCCESS, WARNING, DANGER, INFO, NEUTRAL }

enum class ButtonKind { PRIMARY, SECONDARY, ACCENT, DANGER }

/** Large, full-width touch target (min 64dp) for use with gloves / in a moving cab. */
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    kind: ButtonKind = ButtonKind.PRIMARY,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(16.dp)
    val content: @Composable () -> Unit = {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
    val m = modifier.fillMaxWidth().heightIn(min = 64.dp)
    when (kind) {
        ButtonKind.SECONDARY -> OutlinedButton(onClick = onClick, modifier = m, shape = shape, enabled = enabled) { content() }
        else -> {
            val colors = when (kind) {
                ButtonKind.ACCENT -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                ButtonKind.DANGER -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                )
                else -> ButtonDefaults.buttonColors()
            }
            Button(onClick = onClick, modifier = m, shape = shape, colors = colors, enabled = enabled) { content() }
        }
    }
}

@Composable
fun StatusPill(text: String, tone: Tone, modifier: Modifier = Modifier) {
    val s = LocalStatusColors.current
    val (bg, fg) = when (tone) {
        Tone.SUCCESS -> s.successContainer to s.onSuccessContainer
        Tone.WARNING -> s.warningContainer to s.onWarningContainer
        Tone.DANGER -> s.dangerContainer to s.onDangerContainer
        Tone.INFO -> s.infoContainer to s.onInfoContainer
        Tone.NEUTRAL -> s.neutralContainer to s.onNeutralContainer
    }
    Surface(color = bg, contentColor = fg, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(text, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth().let { if (onClick != null) it.clickable(role = Role.Button, onClick = onClick) else it },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            }
            content()
        }
    }
}

@Composable
fun LabeledValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

/** Always-visible label so simulated data is never mistaken for real Freight Tiger updates. */
@Composable
fun DemoBanner(modifier: Modifier = Modifier) {
    val s = LocalStatusColors.current
    Row(
        modifier.fillMaxWidth().background(s.warningContainer).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Science, contentDescription = null, tint = s.onWarningContainer)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.demo_banner), color = s.onWarningContainer, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ConnectivityBanner(online: Boolean, pending: Int, failed: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (online && pending == 0 && failed == 0) return
    val s = LocalStatusColors.current
    val (bg, fg, icon, text) = when {
        !online -> Quad(s.dangerContainer, s.onDangerContainer, Icons.Filled.CloudOff, stringResource(R.string.offline_banner))
        failed > 0 -> Quad(s.dangerContainer, s.onDangerContainer, Icons.Filled.SyncProblem, stringResource(R.string.failed_sync_count, failed))
        else -> Quad(s.infoContainer, s.onInfoContainer, Icons.Filled.Sync, stringResource(R.string.pending_sync_count, pending))
    }
    Row(
        modifier.fillMaxWidth().background(bg).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = fg)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(text, color = fg, style = MaterialTheme.typography.bodySmall)
            if (!online && pending > 0) {
                Text(stringResource(R.string.pending_sync_count, pending), color = fg, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private data class Quad(val bg: Color, val fg: Color, val icon: ImageVector, val text: String)

enum class MicState { OFF, LISTENING, SPEAKING }

/** Clear, always-labelled microphone / audio status. */
@Composable
fun MicIndicator(state: MicState, modifier: Modifier = Modifier) {
    val s = LocalStatusColors.current
    val (bg, fg, icon, label) = when (state) {
        MicState.LISTENING -> Quad(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError, Icons.Filled.Mic, stringResource(R.string.voice_mic_on))
        MicState.SPEAKING -> Quad(s.infoContainer, s.onInfoContainer, Icons.AutoMirrored.Filled.VolumeUp, stringResource(R.string.voice_speaking))
        MicState.OFF -> Quad(s.neutralContainer, s.onNeutralContainer, Icons.Filled.MicOff, stringResource(R.string.voice_mic_off))
    }
    Row(
        modifier.background(bg, RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 8.dp).semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(10.dp).background(fg, CircleShape))
        Spacer(Modifier.width(8.dp))
        Icon(icon, contentDescription = null, tint = fg)
        Spacer(Modifier.width(6.dp))
        Text(label, color = fg, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, body: String? = null, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Column(
        modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        if (body != null) Text(body, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        action?.invoke()
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
        Spacer(Modifier.size(16.dp))
        Text(stringResource(R.string.loading), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ErrorMessage(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    val s = LocalStatusColors.current
    Column(
        modifier.fillMaxWidth().background(s.dangerContainer, RoundedCornerShape(16.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = s.onDangerContainer)
            Spacer(Modifier.width(8.dp))
            Text(message, color = s.onDangerContainer, style = MaterialTheme.typography.bodyMedium)
        }
        if (onRetry != null) BigButton(stringResource(R.string.action_retry), onRetry, kind = ButtonKind.SECONDARY)
    }
}

@Composable
fun SuccessMessage(message: String, modifier: Modifier = Modifier) {
    val s = LocalStatusColors.current
    Text(
        message,
        color = s.onSuccessContainer,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier.fillMaxWidth().background(s.successContainer, RoundedCornerShape(16.dp)).padding(16.dp),
    )
}
