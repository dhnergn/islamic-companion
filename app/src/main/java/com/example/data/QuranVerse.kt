package com.example.data

/**
 * Data model representing a Quran verse alongside its translation and Gemini-generated explanation.
 */
data class DailyQuranVerse(
    val surahName: String,
    val surahNumber: Int,
    val verseNumber: Int,
    val arabicText: String,
    val translation: String,
    val explanation: String,
    val theme: String = "Spiritual Mindfulness",
    val revelationType: String = "Meccan"
)
