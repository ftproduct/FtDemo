package com.freighttiger.driverassistant

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.freighttiger.driverassistant.platform.lifecycle.AppForegroundTracker
import com.freighttiger.driverassistant.platform.notifications.PromptNotifier
import com.freighttiger.driverassistant.runtime.AssistantRuntime
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DriverAssistantApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var runtime: AssistantRuntime
    @Inject lateinit var foreground: AppForegroundTracker
    @Inject lateinit var notifier: PromptNotifier

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        notifier.createChannels()
        foreground.start()
        runtime.start()
    }
}
