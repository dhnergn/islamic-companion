package com.example.util

import kotlin.math.*

object QiblaCalculator {

    // Kaaba / Mecca coordinates
    const val MECCA_LATITUDE = 21.422487
    const val MECCA_LONGITUDE = 39.826206

    /**
     * Calculates the Qibla bearing (angle from True North in degrees clockwise: 0 to 360)
     * from any latitude and longitude on Earth.
     */
    fun calculateQiblaDirection(latitude: Double, longitude: Double): Double {
        val latRad = Math.toRadians(latitude)
        val lonRad = Math.toRadians(longitude)
        val meccaLatRad = Math.toRadians(MECCA_LATITUDE)
        val meccaLonRad = Math.toRadians(MECCA_LONGITUDE)

        val deltaLon = meccaLonRad - lonRad

        val y = sin(deltaLon) * cos(meccaLatRad)
        val x = cos(latRad) * sin(meccaLatRad) - sin(latRad) * cos(meccaLatRad) * cos(deltaLon)

        var qiblaRad = atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad)

        // Normalize bearing to 0..360
        qiblaDeg = (qiblaDeg + 360.0) % 360.0
        return qiblaDeg
    }

    /**
     * Standard distance calculation to Mecca in kilometers
     */
    fun calculateDistanceToMecca(latitude: Double, longitude: Double): Double {
        val earthRadius = 6371.0 // kilometers
        val dLat = Math.toRadians(MECCA_LATITUDE - latitude)
        val dLon = Math.toRadians(MECCA_LONGITUDE - longitude)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(latitude)) * cos(Math.toRadians(MECCA_LATITUDE)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
