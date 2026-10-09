package com.freighttiger.driverassistant.platform.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.freighttiger.driverassistant.MainActivity
import com.freighttiger.driverassistant.R
import com.freighttiger.driverassistant.core.model.AssistantPrompt
import com.freighttiger.driverassistant.ui.text.promptTitleRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Posts actionable notifications for prompts. A notification never starts the microphone: the
 * driver must open the app ("Answer now") and the voice screen then handles the turn.
 */
@Singleton
class PromptNotifier @Inject constructor(@ApplicationContext private val context: Context) {

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_PROMPTS, context.getString(R.string.channel_prompts_name), NotificationManager.IMPORTANCE_HIGH).apply {
                description = context.getString(R.string.channel_prompts_desc)
            },
        )
    }

    fun canNotify(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** Returns false when notifications are not permitted (prompt stays visible in the app). */
    @SuppressLint("MissingPermission") // Checked by canNotify(); SecurityException is also handled.
    fun show(prompt: AssistantPrompt): Boolean {
        if (!canNotify()) return false
        val title = context.getString(promptTitleRes(prompt.type)).let {
            if (prompt.simulated) context.getString(R.string.notif_simulated_prefix, it) else it
        }
        val body = context.getString(if (prompt.type.expectsResponse) R.string.notif_body_question else R.string.notif_body_info)
        val requestCode = prompt.promptId.hashCode()

        val open = PendingIntent.getActivity(
            context, requestCode,
            MainActivity.promptIntent(context, prompt.promptId, userInitiated = false),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_PROMPTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(open)

        if (prompt.type.expectsResponse) {
            val answer = PendingIntent.getActivity(
                context, requestCode + 1,
                MainActivity.promptIntent(context, prompt.promptId, userInitiated = true),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val later = PendingIntent.getBroadcast(
                context, requestCode + 2,
                Intent(context, PromptActionReceiver::class.java)
                    .setAction(PromptActionReceiver.ACTION_DEFER)
                    .putExtra(PromptActionReceiver.EXTRA_PROMPT_ID, prompt.promptId),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            builder.addAction(0, context.getString(R.string.notif_answer_now), answer)
            builder.addAction(0, context.getString(R.string.notif_later), later)
        }
        return try {
            NotificationManagerCompat.from(context).notify(notificationId(prompt.promptId), builder.build())
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun cancel(promptId: String) = NotificationManagerCompat.from(context).cancel(notificationId(promptId))

    private fun notificationId(promptId: String) = promptId.hashCode()

    companion object {
        const val CHANNEL_PROMPTS = "trip_prompts"
    }
}
