package com.freighttiger.driverassistant

import android.Manifest
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.freighttiger.driverassistant.core.database.AssistantDatabase
import com.freighttiger.driverassistant.core.model.DriverProfile
import com.freighttiger.driverassistant.core.model.DriverSession
import com.freighttiger.driverassistant.core.network.mock.MockFreightTigerBackend
import com.freighttiger.driverassistant.data.SettingsRepository
import com.freighttiger.driverassistant.di.BackendSelection
import com.freighttiger.driverassistant.domain.ports.SessionRepository
import com.freighttiger.driverassistant.platform.notifications.PromptNotifier
import com.freighttiger.driverassistant.runtime.AssistantRuntime
import dagger.hilt.android.testing.HiltAndroidRule
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import java.time.Instant
import javax.inject.Inject

/**
 * Runs the real app (Room, simulated backend, real ViewModels) on a device/emulator with scripted
 * speech. Requires a debug build (demo mode on).
 */
abstract class BaseUiTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createEmptyComposeRule()
    @get:Rule(order = 2) val permissions: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var selection: BackendSelection
    @Inject lateinit var runtime: AssistantRuntime
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var sessions: SessionRepository
    @Inject lateinit var notifier: PromptNotifier
    @Inject lateinit var speechIn: FakeSpeechInput
    @Inject lateinit var speechOut: FakeSpeechOutput

    protected val context: Context get() = ApplicationProvider.getApplicationContext()
    protected val mock: MockFreightTigerBackend get() = selection.mock ?: error("UI tests require demo mode (debug build)")
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun baseSetUp() {
        context.deleteDatabase(AssistantDatabase.NAME)
        context.getSharedPreferences("ftda_settings", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("ftda_secure_session", Context.MODE_PRIVATE).edit().clear().commit()
        hilt.inject()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).setWorkerFactory(workerFactory).build(),
        )
        notifier.createChannels()
        runtime.start()
    }

    protected fun launch() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    /** Skips onboarding UI by seeding a (simulated) session. */
    protected fun launchOnboarded() {
        runBlocking {
            sessions.save(DriverSession(DriverProfile(MockFreightTigerBackend.DEMO_DRIVER_ID, "Test", "******3210"), Instant.now(), simulated = true))
        }
        settings.update { it.copy(onboardingComplete = true, autoSpeakPrompts = false) }
        launch()
    }

    protected fun str(@StringRes id: Int, vararg args: Any): String = context.getString(id, *args)

    protected fun waitForTag(tag: String, timeoutMs: Long = 5_000) =
        compose.waitUntil(timeoutMs) { compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }

    protected fun waitForText(text: String, timeoutMs: Long = 5_000) =
        compose.waitUntil(timeoutMs) { compose.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty() }

    protected fun tag(tag: String): SemanticsNodeInteraction {
        waitForTag(tag)
        return compose.onNodeWithTag(tag)
    }

    protected fun clickTag(tag: String) = tag(tag).performScrollTo().performClick()

    /** Clicks the node whose text is exactly [text]. */
    protected fun clickText(text: String) {
        compose.waitUntil(5_000) { compose.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(text).performScrollTo().performClick()
    }
}
