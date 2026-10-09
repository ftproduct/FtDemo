package com.freighttiger.driverassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// High-contrast palette (WCAG AA for text on its container). No dynamic colour: status colours must
// mean the same thing on every phone.
private val Navy = Color(0xFF0D2B4E)
private val NavyLight = Color(0xFFD5E3F6)
private val Amber = Color(0xFFFFB300)
private val AmberDark = Color(0xFF7A4F00)
private val Ink = Color(0xFF101418)
private val InkSoft = Color(0xFF3A4350)

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = NavyLight,
    onPrimaryContainer = Navy,
    secondary = AmberDark,
    onSecondary = Color.White,
    secondaryContainer = Amber,
    onSecondaryContainer = Color(0xFF231700),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFDDAD6),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFF5F7FA),
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE7ECF2),
    onSurfaceVariant = InkSoft,
    outline = Color(0xFF6A7380),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C8F0),
    onPrimary = Color(0xFF00213F),
    primaryContainer = Color(0xFF1C3F68),
    onPrimaryContainer = Color(0xFFD5E3F6),
    secondary = Amber,
    onSecondary = Color(0xFF231700),
    secondaryContainer = Color(0xFF5C3B00),
    onSecondaryContainer = Color(0xFFFFDEA6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0E1218),
    onBackground = Color(0xFFE6E9EE),
    surface = Color(0xFF161B22),
    onSurface = Color(0xFFE6E9EE),
    surfaceVariant = Color(0xFF26303C),
    onSurfaceVariant = Color(0xFFC4CCD6),
    outline = Color(0xFF8C96A3),
)

@Immutable
data class StatusColors(
    val success: Color, val onSuccess: Color, val successContainer: Color, val onSuccessContainer: Color,
    val warning: Color, val warningContainer: Color, val onWarningContainer: Color,
    val dangerContainer: Color, val onDangerContainer: Color,
    val infoContainer: Color, val onInfoContainer: Color,
    val neutralContainer: Color, val onNeutralContainer: Color,
)

private val LightStatus = StatusColors(
    success = Color(0xFF1B6E34), onSuccess = Color.White, successContainer = Color(0xFFD3F2DA), onSuccessContainer = Color(0xFF0B3A19),
    warning = AmberDark, warningContainer = Color(0xFFFFE6B0), onWarningContainer = Color(0xFF3A2600),
    dangerContainer = Color(0xFFFDDAD6), onDangerContainer = Color(0xFF5C1410),
    infoContainer = NavyLight, onInfoContainer = Navy,
    neutralContainer = Color(0xFFE7ECF2), onNeutralContainer = InkSoft,
)

private val DarkStatus = StatusColors(
    success = Color(0xFF7DDA94), onSuccess = Color(0xFF003914), successContainer = Color(0xFF14522A), onSuccessContainer = Color(0xFFD3F2DA),
    warning = Amber, warningContainer = Color(0xFF5C3B00), onWarningContainer = Color(0xFFFFDEA6),
    dangerContainer = Color(0xFF93000A), onDangerContainer = Color(0xFFFFDAD6),
    infoContainer = Color(0xFF1C3F68), onInfoContainer = Color(0xFFD5E3F6),
    neutralContainer = Color(0xFF26303C), onNeutralContainer = Color(0xFFC4CCD6),
)

val LocalStatusColors = staticCompositionLocalOf { LightStatus }

// Larger-than-default type scale for readability at arm's length / in a cab.
private val FtTypography = Typography(
    headlineMedium = TextStyle(fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 30.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, lineHeight = 26.sp),
    bodySmall = TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    labelLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun FtTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(LocalStatusColors provides if (darkTheme) DarkStatus else LightStatus) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = FtTypography,
            content = content,
        )
    }
}
