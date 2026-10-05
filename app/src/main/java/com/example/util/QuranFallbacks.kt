package com.example.util

import com.example.network.QuranVerseResponse

object QuranFallbacks {

    val fallbacks = listOf(
        QuranVerseResponse(
            surahName = "Al-Baqarah",
            surahNumber = 2,
            verseNumber = 152,
            arabic = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
            translation = "So remember Me; I will remember you. And be grateful to Me and do not deny Me.",
            reflection = "This profound verse teaches us that our connection with God is interactive and reciprocal. When you consciously turn your heart to Him, He actively welcomes you. Nurture mindfulness (Dhikr) and practice gratitude to build inner resilience against life's worries."
        ),
        QuranVerseResponse(
            surahName = "Ash-Sharh",
            surahNumber = 94,
            verseNumber = 5,
            arabic = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا",
            translation = "For indeed, with hardship [will be] ease.",
            reflection = "Allah does not say ease comes *after* hardship, but *with* it. Even in the midst of your heaviest trials, the seeds of peace, growth, and relief are already present. Train your mind to seek out these hidden blessings, and hold onto hope."
        ),
        QuranVerseResponse(
            surahName = "Ibrahim",
            surahNumber = 14,
            verseNumber = 7,
            arabic = "لَئِن شَكَرْتُمْ لَأَزِيدَنَّكُمْ",
            translation = "If you are grateful, I will surely increase you [in favor].",
            reflection = "Gratitude is a divine multiplier. By focusing on what you have rather than what you lack, you open the gates of your heart to receive more abundance. Start a daily gratitude journal to reflect on small favors."
        ),
        QuranVerseResponse(
            surahName = "Al-Baqarah",
            surahNumber = 2,
            verseNumber = 286,
            arabic = "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
            translation = "Allah does not burden a soul beyond that it can bear.",
            reflection = "You are stronger than you think. Every difficulty you encounter is specifically tailored to your spiritual stamina. Allah has full confidence in your capacity to endure and overcome; have that same confidence in yourself."
        ),
        QuranVerseResponse(
            surahName = "Az-Zumar",
            surahNumber = 39,
            verseNumber = 53,
            arabic = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ",
            translation = "Say, 'O My servants who have transgressed against themselves, do not despair of the mercy of Allah.'",
            reflection = "No mistake is too big for the mercy of Allah. No matter how far you feel you have wandered, the path to forgiveness and starting anew is always open. Forgive yourself, take a deep breath, and make today a turning point."
        ),
        QuranVerseResponse(
            surahName = "Al-Imran",
            surahNumber = 3,
            verseNumber = 139,
            arabic = "وَلَا تَهِنُوا وَلَا تَحْزَنُوا وَأَنتُمُ الْأَعْلَوْنَ إِن كُنتُم مُّؤْمِنِينَ",
            translation = "So do not weaken and do not grieve, and you will be superior if you are [true] believers.",
            reflection = "When setbacks weigh you down, remember your faith is your anchor. Do not let sorrow diminish your resolve or weaken your spiritual courage. Lift your head high and proceed with steady faith."
        )
    )

    fun getRandomFallback(): QuranVerseResponse {
        return fallbacks.random()
    }
}
