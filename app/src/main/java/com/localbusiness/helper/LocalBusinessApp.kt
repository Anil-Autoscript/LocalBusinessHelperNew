package com.localbusiness.helper

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.work.*
import com.jakewharton.threetenabp.AndroidThreeTen
import com.localbusiness.helper.workers.SheetsSyncWorker
import java.util.concurrent.TimeUnit

class LocalBusinessApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Init ThreeTenABP date library first
        AndroidThreeTen.init(this)
        createNotificationChannels()
        scheduleBackgroundSync()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java) ?: return

            NotificationChannel(
                CHANNEL_FOLLOWUP,
                "Follow-up Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for customer follow-up dates"
                enableVibration(true)
                manager.createNotificationChannel(this)
            }

            NotificationChannel(
                CHANNEL_SYNC,
                "Sync Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background sync status notifications"
                manager.createNotificationChannel(this)
            }
        }
    }

    private fun scheduleBackgroundSync() {
        try {
            val syncRequest = PeriodicWorkRequestBuilder<SheetsSyncWorker>(
                6, TimeUnit.HOURS,
                30, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .addTag(TAG_SYNC)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WORK_SYNC,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        } catch (e: Exception) {
            Log.e("LocalBusinessApp", "Failed to schedule sync: ${e.message}")
        }
    }

    companion object {
        const val CHANNEL_FOLLOWUP = "followup_reminders"
        const val CHANNEL_SYNC = "sync_status"
        const val TAG_SYNC = "sheets_sync"
        const val WORK_SYNC = "periodic_sheets_sync"
    }
}
