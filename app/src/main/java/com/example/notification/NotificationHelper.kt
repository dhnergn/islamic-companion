package com.example.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.PrayerTimesCalculator
import java.util.*

object NotificationHelper {

    private const val TAG = "NotificationHelper"
    const val CHANNEL_ID = "islamic_companion_prayer_channel"
    private const val CHANNEL_NAME = "Prayer Notifications"
    private const val CHANNEL_DESC = "Push notifications for the five daily prayers."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created successfully.")
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun schedulePrayerAlarms(
        context: Context,
        prayerTimes: PrayerTimesCalculator.PrayerTimes,
        latitude: Double,
        longitude: Double
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val prayers = listOf(
            "Fajr" to prayerTimes.fajr,
            "Dhuhr" to prayerTimes.dhuhr,
            "Asr" to prayerTimes.asr,
            "Maghrib" to prayerTimes.maghrib,
            "Isha" to prayerTimes.isha
        )

        val baseCal = Calendar.getInstance()

        prayers.forEachIndexed { index, (name, timeStr) ->
            try {
                val prayerCal = PrayerTimesCalculator.getPrayerTimeCalendar(timeStr, baseCal)
                
                // If the prayer time has already passed today, schedule it for tomorrow
                if (prayerCal.timeInMillis < System.currentTimeMillis()) {
                    prayerCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                    action = "com.example.islamiccompanion.ACTION_PRAYER_ALARM"
                    putExtra("PRAYER_NAME", name)
                    putExtra("PRAYER_TIME", timeStr)
                    putExtra("LATITUDE", latitude)
                    putExtra("LONGITUDE", longitude)
                }

                // Unique pending intent ID for each of the 5 prayers (0 to 4)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            prayerCal.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            prayerCal.timeInMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        prayerCal.timeInMillis,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Scheduled notification alarm for $name at ${prayerCal.time} (Index: $index)")
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling alarm for $name", e)
            }
        }
    }

    fun showNotification(context: Context, prayerName: String, prayerTime: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            99,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Inspirational reminders based on the specific prayer
        val message = when (prayerName.lowercase()) {
            "fajr" -> "Prayer is better than sleep. Time to connect with your Creator."
            "dhuhr" -> "A brief pause in your day to thank Allah for all blessings."
            "asr" -> "Guard your prayers, especially the middle prayer."
            "maghrib" -> "Day transitions into night. Thank Allah for today's journey."
            "isha" -> "Conclude your day in remembrance and peaceful devotion."
            else -> "It is time for $prayerName prayer. Success awaits."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // simple system fallback icon
            .setContentTitle("Adhan Reminder: $prayerName ($prayerTime)")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(prayerName.hashCode(), notification)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleEventAlarm(
        context: Context,
        eventName: String,
        eventTimeMs: Long,
        daysInAdvance: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Target time is eventTimeMs minus X days
        val reminderTimeMs = eventTimeMs - (daysInAdvance.toLong() * 24 * 60 * 60 * 1000)
        
        // If the reminder time has already passed, we don't schedule it
        if (reminderTimeMs < System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            action = "com.example.islamiccompanion.ACTION_EVENT_ALARM"
            putExtra("EVENT_NAME", eventName)
            putExtra("DAYS_ADVANCE", daysInAdvance)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventName.hashCode() + daysInAdvance,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMs,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMs,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTimeMs,
                pendingIntent
            )
        }
        Log.d(TAG, "Scheduled event alarm for $eventName ($daysInAdvance days in advance) at ${Date(reminderTimeMs)}")
    }

    fun showEventNotification(context: Context, eventName: String, daysAdvance: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            100 + eventName.hashCode() + daysAdvance,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = when (eventName) {
            "Eid al-Fitr" -> "Eid al-Fitr is approaching in $daysAdvance days! Prep your heart, prayers, and charity."
            "Eid al-Adha" -> "Eid al-Adha is coming in $daysAdvance days. May the days of sacrifice be blessed."
            "Laylat al-Qadr" -> "The Night of Power (Laylat al-Qadr) is approaching in $daysAdvance days. Seek out its immense blessings."
            else -> "$eventName is approaching in $daysAdvance days. Mark your calendar for devotion."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Islamic Event Reminder: $eventName")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(eventName.hashCode() + daysAdvance, notification)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleRamadanAlarms(
        context: Context,
        imsakTime: String,
        iftarTime: String,
        latitude: Double,
        longitude: Double
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val items = listOf("Imsak" to imsakTime, "Iftar" to iftarTime)

        items.forEachIndexed { index, (type, timeStr) ->
            try {
                val cal = PrayerTimesCalculator.getPrayerTimeCalendar(timeStr, Calendar.getInstance())
                if (cal.timeInMillis < System.currentTimeMillis()) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }

                val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                    action = "com.example.islamiccompanion.ACTION_RAMADAN_ALARM"
                    putExtra("RAMADAN_TYPE", type)
                    putExtra("RAMADAN_TIME", timeStr)
                    putExtra("LATITUDE", latitude)
                    putExtra("LONGITUDE", longitude)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    200 + index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                }
                Log.d(TAG, "Scheduled Ramadan alarm for $type at ${cal.time}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule Ramadan alarm for $type", e)
            }
        }
    }

    fun cancelRamadanAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0..1) {
            val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                action = "com.example.islamiccompanion.ACTION_RAMADAN_ALARM"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                200 + i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    fun showRamadanNotification(context: Context, type: String, timeStr: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            250 + type.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, text) = if (type.equals("Imsak", ignoreCase = true)) {
            "🌙 Imsak Reminder ($timeStr)" to "10 minutes before Fajr. Please conclude your Suhoor and renew your fasting intention."
        } else {
            "🌅 Iftar Mubarak! ($timeStr)" to "Maghrib has entered. The fast is complete—break your fast and offer your du'as."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(300 + type.hashCode(), notification)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleDhikrAlarm(
        context: Context,
        dhikrPhrase: String,
        intervalHours: Int,
        specificHour: Int = -1,
        specificMinute: Int = 0
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            action = "com.example.islamiccompanion.ACTION_DHIKR_ALARM"
            putExtra("DHIKR_PHRASE", dhikrPhrase)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            500,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTimeMs = if (specificHour >= 0) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, specificHour)
                set(Calendar.MINUTE, specificMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (cal.timeInMillis < System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            cal.timeInMillis
        } else {
            System.currentTimeMillis() + (intervalHours * 3600 * 1000L)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        }
        Log.d(TAG, "Scheduled Dhikr reminder alarm for '$dhikrPhrase' at ${Date(triggerTimeMs)}")
    }

    fun cancelDhikrAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            action = "com.example.islamiccompanion.ACTION_DHIKR_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            500,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun showDhikrNotification(context: Context, dhikrPhrase: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            550,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("📿 Dhikr Reminder")
            .setContentText("Take a mindful moment to remember Allah: $dhikrPhrase")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Take a mindful moment to remember Allah: \"$dhikrPhrase\". Unwind your mind and bring peace to your heart."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(500, notification)
    }
}
