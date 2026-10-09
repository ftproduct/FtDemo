package com.freighttiger.driverassistant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.freighttiger.driverassistant.feature.activity.ActivityScreen
import com.freighttiger.driverassistant.feature.consent.ConsentScreen
import com.freighttiger.driverassistant.feature.demo.DemoScreen
import com.freighttiger.driverassistant.feature.home.HomeScreen
import com.freighttiger.driverassistant.feature.onboarding.OnboardingScreen
import com.freighttiger.driverassistant.feature.onboarding.PrivacyInfoScreen
import com.freighttiger.driverassistant.feature.settings.SettingsScreen
import com.freighttiger.driverassistant.feature.support.SupportScreen
import com.freighttiger.driverassistant.feature.sync.SyncScreen
import com.freighttiger.driverassistant.feature.trips.ReportScreen
import com.freighttiger.driverassistant.feature.trips.TrackingStatusScreen
import com.freighttiger.driverassistant.feature.trips.TripDetailsScreen
import com.freighttiger.driverassistant.feature.voice.VoiceScreen
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val TRIP = "trip"
    const val TRACKING = "tracking"
    const val REPORT = "report"
    const val CONSENT = "consent"
    const val VOICE = "voice?promptId={promptId}&user={user}"
    const val ACTIVITY = "activity"
    const val SETTINGS = "settings"
    const val SYNC = "sync"
    const val SUPPORT = "support"
    const val DEMO = "demo"
    const val PRIVACY = "privacy"

    fun voice(promptId: String?, userInitiated: Boolean) = "voice?promptId=${promptId.orEmpty()}&user=$userInitiated"
}

data class PromptOpen(val promptId: String, val userInitiated: Boolean)

/** Navigation callbacks shared by screens. */
class Nav(private val controller: NavHostController) {
    fun back() { controller.popBackStack() }
    fun to(route: String) = controller.navigate(route) { launchSingleTop = true }
    fun voice(promptId: String?, userInitiated: Boolean) = to(Routes.voice(promptId, userInitiated))
    fun home() = controller.navigate(Routes.HOME) {
        popUpTo(controller.graph.id) { inclusive = true }
        launchSingleTop = true
    }
    fun restartOnboarding() = controller.navigate(Routes.ONBOARDING) {
        popUpTo(controller.graph.id) { inclusive = true }
    }
}

@Composable
fun DriverAssistantNavHost(
    openRequests: SharedFlow<String>,
    intentPrompt: StateFlow<PromptOpen?>,
    onIntentConsumed: () -> Unit,
) {
    val statusVm: AppStatusViewModel = hiltViewModel()
    val status by statusVm.status.collectAsStateWithLifecycle()
    val controller = rememberNavController()
    val nav = Nav(controller)
    val onboarded by rememberUpdatedState(status.onboardingComplete)
    // Decided once; later changes navigate explicitly (finish onboarding / log out).
    val startDestination = remember { if (status.onboardingComplete) Routes.HOME else Routes.ONBOARDING }

    CompositionLocalProvider(LocalAppStatus provides status) {
        NavHost(controller, startDestination = startDestination) {
            composable(Routes.ONBOARDING) { OnboardingScreen(onFinished = nav::home) }
            composable(Routes.HOME) { HomeScreen(nav) }
            composable(Routes.TRIP) { TripDetailsScreen(nav) }
            composable(Routes.TRACKING) { TrackingStatusScreen(nav) }
            composable(Routes.REPORT) { ReportScreen(nav) }
            composable(Routes.CONSENT) { ConsentScreen(nav) }
            composable(
                Routes.VOICE,
                arguments = listOf(
                    navArgument("promptId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("user") { type = NavType.BoolType; defaultValue = false },
                ),
            ) { VoiceScreen(nav) }
            composable(Routes.ACTIVITY) { ActivityScreen(nav) }
            composable(Routes.SETTINGS) { SettingsScreen(nav) }
            composable(Routes.SYNC) { SyncScreen(nav) }
            composable(Routes.SUPPORT) { SupportScreen(nav) }
            composable(Routes.DEMO) { DemoScreen(nav) }
            composable(Routes.PRIVACY) { PrivacyInfoScreen(nav) }
        }
    }

    // Foreground prompt presentation requested by the runtime.
    LaunchedEffect(openRequests) {
        openRequests.collect { promptId -> if (onboarded) nav.voice(promptId, userInitiated = false) }
    }

    // Prompt opened from a notification.
    val pending by intentPrompt.collectAsStateWithLifecycle()
    LaunchedEffect(pending, status.onboardingComplete) {
        val open = pending ?: return@LaunchedEffect
        if (status.onboardingComplete) {
            nav.voice(open.promptId, open.userInitiated)
            onIntentConsumed()
        }
    }
}
