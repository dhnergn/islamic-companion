package com.example.util

import com.example.network.HadithResponse

object HadithRepository {

    val fallbacks = listOf(
        HadithResponse(
            text = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
            translation = "Actions are judged by intentions, and every person will get what they intended.",
            source = "Sahih al-Bukhari 1, Sahih Muslim 1907",
            reflection = "Sincerity is the bedrock of any deed. Before initiating any action—whether in your personal life, prayer, or daily work—pause to purify your intention. Aligning your motives with a pure purpose elevates ordinary acts into spiritual milestones."
        ),
        HadithResponse(
            text = "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
            translation = "None of you truly believes until he wishes for his brother what he wishes for himself.",
            source = "Sahih al-Bukhari 13, Sahih Muslim 45",
            reflection = "True faith expands beyond self-interest. To cultivate a peaceful mind and healthy community, live with empathy. Celebrate the success of others and actively seek to uplift those around you just as you would seek your own comfort."
        ),
        HadithResponse(
            text = "عَجَبًا لأَمْرِ الْمُؤْمِنِ إِنَّ أَمْرَهُ كُلَّهُ خَيْرٌ",
            translation = "How wonderful is the affair of the believer, for there is good for him in every matter.",
            source = "Sahih Muslim 2999",
            reflection = "Resilience and gratitude define a believer. If prosperity comes, you give thanks and it is good for you. If adversity strikes, you show patience and it is good for you. Embrace every situation knowing it carries hidden grace."
        ),
        HadithResponse(
            text = "اتَّقِ اللَّهَ حَيْثُمَا كُنْتَ، وَأَتْبِعِ السَّيِّئَةَ الْحَسَنَةَ تَمْحُهَا",
            translation = "Fear Allah wherever you are, and follow up a bad deed with a good deed which will wipe it out, and behave well towards the people.",
            source = "Sunan al-Tirmidhi 1987",
            reflection = "Life is a journey of continuous course correction. When you make a mistake or fall short, do not dwell in despair; actively wash it away by performing a constructive, compassionate act. Let kindness guide all your human connections."
        ),
        HadithResponse(
            text = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
            translation = "Whoever believes in Allah and the Last Day should speak what is good or keep silent.",
            source = "Sahih al-Bukhari 6018, Sahih Muslim 47",
            reflection = "Our words have powerful energy and shape our internal peace. Practice speech awareness: if your contribution doesn't heal, help, or inspire, prioritize quiet reflection. Silence is a profound sanctuary."
        ),
        HadithResponse(
            text = "يَسِّرُوا وَلاَ تُعَسِّرُوا، وَبَشِّرُوا وَلاَ تُنَفِّرُوا",
            translation = "Make things easy for people and do not make them difficult, and give good tidings and do not turn people away.",
            source = "Sahih al-Bukhari 69, Sahih Muslim 1734",
            reflection = "This beautiful instruction highlights the essence of simplicity. Whether you are explaining your faith, working on a team, or dealing with family, choose the path of facilitation and hope. Be a source of calm clarity."
        )
    )

    fun getRandomHadith(): HadithResponse {
        return fallbacks.random()
    }
}
