package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.network.Content
import com.example.network.GeminiRequest
import com.example.network.GenerationConfig
import com.example.network.Part
import com.example.network.QuranVerseResponse
import com.example.network.RetrofitClient
import com.example.util.QuranFallbacks
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface QuranRepository {
    suspend fun getDailyVerse(topic: String): DailyQuranVerse
    suspend fun generateDeepExplanation(surahName: String, verseNumber: Int, arabicText: String, translation: String): String
    fun getFallbackVerse(topic: String): DailyQuranVerse
}

class QuranRepositoryImpl : QuranRepository {

    private val tag = "QuranRepository"

    override suspend fun getDailyVerse(topic: String): DailyQuranVerse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getFallbackVerse(topic)
        }

        try {
            val prompt = """
                Please provide an authentic, inspiring Quranic verse regarding the theme: "$topic".
                Return a JSON object exactly matching this schema:
                {
                  "surahName": "Surah Name in English (e.g., Al-Baqarah)",
                  "surahNumber": 2,
                  "verseNumber": 152,
                  "arabic": "The exact Arabic text with vowels (e.g. فَاذْكُرُونِي أَذْكُرْكُمْ)",
                  "translation": "Standard clear English translation",
                  "reflection": "A thoughtful, inspiring, modern spiritual reflection (2-3 sentences) on how to live this verse today."
                }
                Return ONLY raw JSON, without markdown formatting.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.7
                ),
                systemInstruction = Content(parts = listOf(Part(
                    text = "You are a knowledgeable Islamic scholar dedicated to inspiring believers with authentic Quranic wisdom and deeply relevant reflections."
                )))
            )

            val response = RetrofitClient.service.generateContent(apiKey, request)
            val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (rawJson != null) {
                val adapter: JsonAdapter<QuranVerseResponse> = RetrofitClient.moshiParser.adapter(QuranVerseResponse::class.java)
                val parsed = adapter.fromJson(rawJson)
                if (parsed != null) {
                    return@withContext DailyQuranVerse(
                        surahName = parsed.surahName,
                        surahNumber = parsed.surahNumber,
                        verseNumber = parsed.verseNumber,
                        arabicText = parsed.arabic,
                        translation = parsed.translation,
                        explanation = parsed.reflection,
                        theme = topic,
                        revelationType = if (parsed.surahNumber in listOf(2, 3, 4, 5, 8, 9, 22, 24, 33, 47, 48, 49, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 110)) "Medinan" else "Meccan"
                    )
                }
            }
            getFallbackVerse(topic)
        } catch (e: Exception) {
            Log.e(tag, "Failed to fetch verse from Gemini API", e)
            getFallbackVerse(topic)
        }
    }

    override suspend fun generateDeepExplanation(
        surahName: String,
        verseNumber: Int,
        arabicText: String,
        translation: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "In this verse ($surahName : $verseNumber), Allah calls believers to continuous spiritual consciousness, gratitude, and moral excellence. Pondering upon its message nurtures serenity and patience."
        }

        try {
            val prompt = """
                Provide a warm, profound, and accessible spiritual explanation (Tafsir & reflection) of this Quranic verse:
                Surah: $surahName, Verse: $verseNumber
                Arabic: $arabicText
                Translation: "$translation"

                Break down:
                1. Core spiritual message and context.
                2. Practical daily action for mindfulness and inner peace.
                Keep it concise (120-150 words), engaging, and spiritually uplifting.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(temperature = 0.7)
            )

            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Contemplate this verse deeply to discover personal solace and renewed dedication to truth and kindness."
        } catch (e: Exception) {
            Log.e(tag, "Error generating explanation", e)
            "Reflect on this noble verse throughout your day. May it grant peace, courage, and direction in your affairs."
        }
    }

    override fun getFallbackVerse(topic: String): DailyQuranVerse {
        val filtered = QuranFallbacks.fallbacks.filter {
            it.reflection.contains(topic.split(" ")[0], ignoreCase = true) ||
            it.translation.contains(topic.split(" ")[0], ignoreCase = true)
        }
        val fallback = if (filtered.isNotEmpty()) filtered.random() else QuranFallbacks.getRandomFallback()
        return DailyQuranVerse(
            surahName = fallback.surahName,
            surahNumber = fallback.surahNumber,
            verseNumber = fallback.verseNumber,
            arabicText = fallback.arabic,
            translation = fallback.translation,
            explanation = fallback.reflection,
            theme = topic,
            revelationType = if (fallback.surahNumber in listOf(2, 3, 4, 5, 8, 9, 22, 24, 33, 47, 48, 49, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 110)) "Medinan" else "Meccan"
        )
    }
}
