package com.localbusiness.helper.workers

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.localbusiness.helper.LocalBusinessApp
import com.localbusiness.helper.R
import com.localbusiness.helper.data.repository.BusinessRepository
import com.localbusiness.helper.data.repository.Result

class SheetsSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val repo = BusinessRepository(context)

        showSyncNotification("Syncing data from Google Sheets...")

        return when (val result = repo.syncFromGoogleSheets()) {
            is com.localbusiness.helper.data.repository.Result.Success -> {
                showSyncNotification("Sync complete: ${result.data} records updated", done = true)
                Result.success()
            }
            is com.localbusiness.helper.data.repository.Result.Error -> {
                // Don't show error notification for unconfigured state
                if (!result.message.contains("not configured")) {
                    showSyncNotification("Sync failed: ${result.message}", done = true)
                }
                Result.failure()
            }
            else -> Result.failure()
        }
    }

    private fun showSyncNotification(message: String, done: Boolean = false) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, LocalBusinessApp.CHANNEL_SYNC)
            .setSmallIcon(R.drawable.ic_sync)
            .setContentTitle("Local Business Helper")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(!done)
            .setAutoCancel(done)
            .build()
        manager.notify(SYNC_NOTIFICATION_ID, notification)
        if (done) {
            // Auto-dismiss after 3 seconds
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                manager.cancel(SYNC_NOTIFICATION_ID)
            }, 3000)
        }
    }

    companion object {
        const val SYNC_NOTIFICATION_ID = 9999
    }
}
