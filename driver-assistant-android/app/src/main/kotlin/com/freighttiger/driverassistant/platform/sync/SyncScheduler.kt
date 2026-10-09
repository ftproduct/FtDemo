package com.freighttiger.driverassistant.platform.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.freighttiger.driverassistant.di.ApplicationScope
import com.freighttiger.driverassistant.domain.sync.OutboxSyncEngine
import com.freighttiger.driverassistant.domain.sync.SyncReport
import com.freighttiger.driverassistant.domain.workflow.SyncTrigger
import com.freighttiger.driverassistant.runtime.PromptPresenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Delivers the outbox promptly (in-process) and durably (WorkManager with a network constraint,
 * which survives process death and reboots). Retries follow the engine's backoff schedule.
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val scope: CoroutineScope,
    private val engine: Provider<OutboxSyncEngine>,
    private val presenter: Provider<PromptPresenter>,
) : SyncTrigger {

    override fun requestSync() {
        scope.launch { runNow() }
        enqueue(UNIQUE_NOW, Duration.ZERO, ExistingWorkPolicy.APPEND_OR_REPLACE)
    }

    suspend fun runNow(): SyncReport {
        val report = engine.get().syncDue()
        report.nextRetryAt?.let { scheduleRetryAt(it) }
        // Sync can surface new informational prompts (e.g. consent validation failed).
        presenter.get().presentNewPrompts()
        if (report.exhausted > 0 || report.failedPermanently > 0) Log.w(TAG, "Sync: ${report.exhausted} exhausted, ${report.failedPermanently} rejected")
        return report
    }

    fun scheduleRetryAt(at: Instant) {
        val delay = Duration.between(Instant.now(), at).coerceAtLeast(Duration.ZERO)
        enqueue(UNIQUE_RETRY, delay, ExistingWorkPolicy.REPLACE)
    }

    private fun enqueue(name: String, delay: Duration, policy: ExistingWorkPolicy) {
        val request = OneTimeWorkRequestBuilder<OutboxSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(name, policy, request)
    }

    private companion object {
        const val TAG = "FtdaSync"
        const val UNIQUE_NOW = "ftda-outbox-sync"
        const val UNIQUE_RETRY = "ftda-outbox-retry"
    }
}

@HiltWorker
class OutboxSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val scheduler: SyncScheduler,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        scheduler.runNow()
        return Result.success()
    }
}

/** Re-presents deferred prompts when their "ask me later" delay has passed. */
@Singleton
class PromptReminderScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    fun remindAt(at: Instant) {
        val delay = Duration.between(Instant.now(), at).coerceAtLeast(Duration.ZERO)
        val request = OneTimeWorkRequestBuilder<PromptReminderWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("ftda-prompt-reminder-${at.toEpochMilli()}", ExistingWorkPolicy.KEEP, request)
    }
}

@HiltWorker
class PromptReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val presenter: PromptPresenter,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        presenter.presentDue(includeDeferred = true)
        return Result.success()
    }
}
