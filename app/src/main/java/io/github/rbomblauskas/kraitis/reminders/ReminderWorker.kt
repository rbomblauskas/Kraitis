package io.github.rbomblauskas.kraitis.reminders

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.rbomblauskas.kraitis.MainActivity
import io.github.rbomblauskas.kraitis.R
import io.github.rbomblauskas.kraitis.data.ClothingItem
import io.github.rbomblauskas.kraitis.data.ClothingStatus
import io.github.rbomblauskas.kraitis.data.KraitisDatabase
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

private const val CHANNEL_ID = "wardrobe_reminders"
private const val NOTIFICATION_ID = 1
private const val WORK_NAME = "wardrobe_reminder"

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val items = KraitisDatabase.getDatabase(applicationContext)
            .clothingDao()
            .getAllItems()
            .first()

        val text = reminderText(items)
        if (text != null) {
            showNotification(text)
        }
        return Result.success()
    }

    private fun showNotification(text: String) {
        val context = applicationContext
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("Wardrobe reminders")
                .build()
        )

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Your wardrobe is waiting")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }
}

// one short summary line, nothing when the wardrobe is fine
fun reminderText(items: List<ClothingItem>): String? {
    val parts = mutableListOf<String>()
    val rewear = items.count { it.status == ClothingStatus.REWEAR }
    val repair = items.count { it.status == ClothingStatus.REPAIR }
    val sell = items.count { it.status == ClothingStatus.SELL }
    val donate = items.count { it.status == ClothingStatus.DONATE }

    if (rewear > 0) parts.add("$rewear to rewear")
    if (repair > 0) parts.add("$repair waiting for repair")
    if (sell > 0) parts.add("$sell to sell")
    if (donate > 0) parts.add("$donate to donate")

    if (parts.isEmpty()) return null
    return "You have " + parts.joinToString(", ")
}

fun scheduleDailyReminder(context: Context) {
    val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}
