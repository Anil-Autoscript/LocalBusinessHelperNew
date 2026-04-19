package com.localbusiness.helper.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.localbusiness.helper.LocalBusinessApp
import com.localbusiness.helper.R
import com.localbusiness.helper.ui.MainActivity

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Re-schedule reminders after reboot if needed
            }
            ACTION_FOLLOWUP -> {
                val orderId = intent.getLongExtra(EXTRA_ORDER_ID, -1L)
                val customerName = intent.getStringExtra(EXTRA_CUSTOMER) ?: ""
                val product = intent.getStringExtra(EXTRA_PRODUCT) ?: ""
                showFollowUpNotification(context, orderId, customerName, product)
            }
        }
    }

    private fun showFollowUpNotification(
        context: Context,
        orderId: Long,
        customerName: String,
        product: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "orders")
            putExtra("order_id", orderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, orderId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, LocalBusinessApp.CHANNEL_FOLLOWUP)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Follow-up Reminder")
            .setContentText("$customerName – $product")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Time to follow up with $customerName for: $product")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(orderId.toInt(), notification)
    }

    companion object {
        const val ACTION_FOLLOWUP = "com.localbusiness.helper.ACTION_FOLLOWUP"
        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_CUSTOMER = "customer_name"
        const val EXTRA_PRODUCT = "product"

        fun scheduleFollowUp(
            context: Context,
            orderId: Long,
            customerName: String,
            product: String,
            triggerTime: Long
        ) {
            if (triggerTime <= System.currentTimeMillis()) return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_FOLLOWUP
                putExtra(EXTRA_ORDER_ID, orderId)
                putExtra(EXTRA_CUSTOMER, customerName)
                putExtra(EXTRA_PRODUCT, product)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context, orderId.toInt(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                // Exact alarm permission not granted — fall back to inexact
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }

        fun cancelFollowUp(context: Context, orderId: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_FOLLOWUP
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, orderId.toInt(), intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }
}
