package com.example.util

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

object PrayerTimesCalculator {

    enum class CalculationMethod(val fajrAngle: Double, val ishaAngle: Double) {
        MWL(18.0, 17.0),       // Muslim World League
        ISNA(15.0, 15.0),      // Islamic Society of North America
        EGYPT(19.5, 17.5),     // Egyptian General Authority of Survey
        MAKKAH(18.5, 90.0),    // Umm al-Qura (Isha is 90 min after Maghrib)
        KARACHI(18.0, 18.0)    // University of Islamic Sciences, Karachi
    }

    enum class AsrJuristic(val shadowFactor: Double) {
        STANDARD(1.0), // Shafi'i, Maliki, Hanbali
        HANAFI(2.0)    // Hanafi
    }

    data class PrayerTimes(
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String
    )

    // Helper functions for math
    private fun dtr(d: Double): Double = d * PI / 180.0
    private fun rtd(r: Double): Double = r * 180.0 / PI
    private fun fixHour(h: Double): Double = (h % 24.0 + 24.0) % 24.0
    private fun fixAngle(a: Double): Double = (a % 360.0 + 360.0) % 360.0

    /**
     * Calculates Julian Date from Calendar
     */
    private fun getJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    /**
     * Compute prayer times for a given day, coordinates, and calculation params
     */
    fun calculateTimes(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        timezoneOffset: Double, // in hours, e.g. +5.5, -4.0
        method: CalculationMethod = CalculationMethod.MWL,
        asrJuristic: AsrJuristic = AsrJuristic.STANDARD
    ): PrayerTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 1-indexed
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val julianDate = getJulianDate(year, month, day) - longitude / (15.0 * 24.0)
        val d = julianDate - 2451545.0 // days since epoch Jan 1, 2000

        // Solar coordinates
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(dtr(g)) + 0.020 * sin(dtr(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val declination = rtd(asin(sin(dtr(e)) * sin(dtr(l))))
        
        var ra = rtd(atan2(cos(dtr(e)) * sin(dtr(l)), cos(dtr(l)))) / 15.0
        ra = (ra % 24.0 + 24.0) % 24.0

        val q_deg = q / 15.0
        val eqt = q_deg - ra // Equation of time in hours

        // Mid day (Dhuhr)
        val transit = 12.0 + timezoneOffset - longitude / 15.0 - eqt
        val dhuhrTime = fixHour(transit)

        // Sunrise and Sunset hour angles
        val latRad = dtr(latitude)
        val decRad = dtr(declination)

        // Sunrise / Sunset elevation angle is -0.833
        val sunriseCos = (sin(dtr(-0.833)) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val sunriseH = if (sunriseCos in -1.0..1.0) rtd(acos(sunriseCos)) / 15.0 else 6.0 // fallback
        val sunriseTime = fixHour(dhuhrTime - sunriseH)
        val maghribTime = fixHour(dhuhrTime + sunriseH)

        // Fajr
        val fajrCos = (sin(dtr(-method.fajrAngle)) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val fajrH = if (fajrCos in -1.0..1.0) rtd(acos(fajrCos)) / 15.0 else 5.0
        val fajrTime = fixHour(dhuhrTime - fajrH)

        // Isha
        val ishaTime = if (method == CalculationMethod.MAKKAH) {
            fixHour(maghribTime + 1.5) // 90 mins after Maghrib
        } else {
            val ishaCos = (sin(dtr(-method.ishaAngle)) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
            val ishaH = if (ishaCos in -1.0..1.0) rtd(acos(ishaCos)) / 15.0 else 7.0
            fixHour(dhuhrTime + ishaH)
        }

        // Asr
        val shadowDiff = asrJuristic.shadowFactor + tan(abs(latRad - decRad))
        val asrElevation = atan(1.0 / shadowDiff)
        val asrCos = (sin(asrElevation) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val asrH = if (asrCos in -1.0..1.0) rtd(acos(asrCos)) / 15.0 else 4.0
        val asrTime = fixHour(dhuhrTime + asrH)

        return PrayerTimes(
            fajr = formatTime(fajrTime),
            sunrise = formatTime(sunriseTime),
            dhuhr = formatTime(dhuhrTime),
            asr = formatTime(asrTime),
            maghrib = formatTime(maghribTime),
            isha = formatTime(ishaTime)
        )
    }

    private fun formatTime(timeInHours: Double): String {
        val totalMinutes = (timeInHours * 60.0).roundToInt()
        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }

    /**
     * Converts "HH:mm" time string into a Calendar object for the same day
     */
    fun getPrayerTimeCalendar(prayerTimeString: String, baseCalendar: Calendar): Calendar {
        val parts = prayerTimeString.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        val cal = baseCalendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, hours)
        cal.set(Calendar.MINUTE, minutes)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }
}
