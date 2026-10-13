package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.preferences.UserPreferences
import java.util.Calendar

enum class ReminderType(val requestCode: Int, val channelId: String) {
    START_OF_DAY(1001, "channel_start_of_day"),
    END_OF_DAY(1002, "channel_end_of_day"),
    TEST(1003, "channel_test")
}

class TransactionReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // Reschedule alarms after device reboot or app update
            TransactionReminderScheduler.scheduleAllReminders(context)
            return
        }

        val reminderTypeName = intent.getStringExtra(TransactionReminderScheduler.EXTRA_REMINDER_TYPE)
            ?: ReminderType.START_OF_DAY.name
        val reminderType = try {
            ReminderType.valueOf(reminderTypeName)
        } catch (e: Exception) {
            ReminderType.START_OF_DAY
        }

        showReminderNotification(context, reminderType)

        // Reschedule recurring alarm for the next day
        if (reminderType == ReminderType.START_OF_DAY) {
            TransactionReminderScheduler.scheduleStartOfDay(context)
        } else if (reminderType == ReminderType.END_OF_DAY) {
            TransactionReminderScheduler.scheduleEndOfDay(context)
        }
    }

    private fun showReminderNotification(context: Context, type: ReminderType) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        TransactionReminderScheduler.createNotificationChannels(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "ADD_TRANSACTION")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            type.requestCode,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, message) = when (type) {
            ReminderType.START_OF_DAY -> Pair(
                "☀️ Good Morning! Plan Today's Expenses",
                "Start fresh today! Don't forget to track your morning coffee, commute, or daily budget."
            )
            ReminderType.END_OF_DAY -> Pair(
                "🌙 Evening Wrap-up: Log Today's Transactions",
                "Did you have any expenses or income today? Take 30 seconds to update BudgetWise before you rest."
            )
            ReminderType.TEST -> Pair(
                "🔔 BudgetWise Reminder Test",
                "Notifications are working great! You'll receive daily morning and evening reminders."
            )
        }

        val notification = NotificationCompat.Builder(context, TransactionReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            .build()

        notificationManager.notify(type.requestCode, notification)
    }
}

object TransactionReminderScheduler {
    const val CHANNEL_ID = "budgetwise_daily_reminders"
    const val EXTRA_REMINDER_TYPE = "extra_reminder_type"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Transaction Reminders"
            val descriptionText = "Morning and evening alerts to remind you to log daily income and expenses"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleAllReminders(context: Context) {
        createNotificationChannels(context)
        val prefs = UserPreferences(context)
        if (prefs.isMorningReminderEnabled()) {
            scheduleStartOfDay(context)
        } else {
            cancelStartOfDay(context)
        }

        if (prefs.isEveningReminderEnabled()) {
            scheduleEndOfDay(context)
        } else {
            cancelEndOfDay(context)
        }
    }

    fun scheduleStartOfDay(context: Context) {
        val prefs = UserPreferences(context)
        val hour = prefs.getMorningHour()
        val minute = prefs.getMorningMinute()
        scheduleAlarm(context, ReminderType.START_OF_DAY, hour, minute)
    }

    fun cancelStartOfDay(context: Context) {
        cancelAlarm(context, ReminderType.START_OF_DAY)
    }

    fun scheduleEndOfDay(context: Context) {
        val prefs = UserPreferences(context)
        val hour = prefs.getEveningHour()
        val minute = prefs.getEveningMinute()
        scheduleAlarm(context, ReminderType.END_OF_DAY, hour, minute)
    }

    fun cancelEndOfDay(context: Context) {
        cancelAlarm(context, ReminderType.END_OF_DAY)
    }

    fun triggerImmediateTestReminder(context: Context) {
        createNotificationChannels(context)
        val receiver = TransactionReminderReceiver()
        val intent = Intent(context, TransactionReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, ReminderType.TEST.name)
        }
        receiver.onReceive(context, intent)
    }

    private fun scheduleAlarm(context: Context, type: ReminderType, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, TransactionReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, type.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            type.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If scheduled time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback for non-exact alarm if exact alarm permission isn't granted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm(context: Context, type: ReminderType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, TransactionReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, type.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            type.requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
