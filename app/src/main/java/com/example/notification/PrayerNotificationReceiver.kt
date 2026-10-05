package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.util.PrayerTimesCalculator
import java.util.Calendar
import java.util.TimeZone

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive triggered with action: $action")

        if (action == "com.example.islamiccompanion.ACTION_PRAYER_ALARM") {
            val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Prayer"
            val prayerTime = intent.getStringExtra("PRAYER_TIME") ?: "--:--"
            
            Log.d(TAG, "Triggering notification for $prayerName at $prayerTime")
            NotificationHelper.showNotification(context, prayerName, prayerTime)

            // Reschedule tomorrow's alarms automatically to keep the loop active
            val lat = intent.getDoubleExtra("LATITUDE", 21.4225)
            val lon = intent.getDoubleExtra("LONGITUDE", 39.8262)
            rescheduleAll(context, lat, lon)

        } else if (action == "com.example.islamiccompanion.ACTION_EVENT_ALARM") {
            val eventName = intent.getStringExtra("EVENT_NAME") ?: "Islamic Event"
            val daysAdvance = intent.getIntExtra("DAYS_ADVANCE", 3)
            Log.d(TAG, "Triggering notification for event: $eventName in $daysAdvance days")
            NotificationHelper.showEventNotification(context, eventName, daysAdvance)

        } else if (action == "com.example.islamiccompanion.ACTION_RAMADAN_ALARM") {
            val type = intent.getStringExtra("RAMADAN_TYPE") ?: "Iftar"
            val timeStr = intent.getStringExtra("RAMADAN_TIME") ?: ""
            Log.d(TAG, "Triggering Ramadan notification for $type at $timeStr")
            NotificationHelper.showRamadanNotification(context, type, timeStr)

        } else if (action == "com.example.islamiccompanion.ACTION_DHIKR_ALARM") {
            val dhikrPhrase = intent.getStringExtra("DHIKR_PHRASE") ?: "SubhanAllah"
            Log.d(TAG, "Triggering Dhikr notification for $dhikrPhrase")
            NotificationHelper.showDhikrNotification(context, dhikrPhrase)

        } else if (action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device rebooted. Rescheduling all prayer alarms.")
            // Use standard coordinates (Mecca as default fallback)
            rescheduleAll(context, 21.4225, 39.8262)
        }
    }

    private fun rescheduleAll(context: Context, lat: Double, lon: Double) {
        val calendar = Calendar.getInstance()
        val timezoneOffset = TimeZone.getDefault().rawOffset / (1000.0 * 60.0 * 60.0)
        val times = PrayerTimesCalculator.calculateTimes(
            calendar = calendar,
            latitude = lat,
            longitude = lon,
            timezoneOffset = timezoneOffset
        )
        NotificationHelper.schedulePrayerAlarms(context, times, lat, lon)
    }

    companion object {
        private const val TAG = "PrayerReceiver"
    }
}
