package com.example.data

object ArticleRepository {

    val articles = listOf(
        // FIQH ARTICLES
        IslamicArticle(
            id = "fiqh_taharah",
            title = "The Fiqh of Purification: Outer Cleanliness, Inner Clarity",
            category = ArticleCategory.FIQH,
            readTime = "2 min read",
            summary = "Wudu is not merely physical washing; it is a spiritual reset washing away daily distractions before standing with Allah.",
            content = """
                In Islamic jurisprudence, taharah (purification) is the indispensable key to prayer. The Prophet ﷺ described cleanliness as 'half of faith.'
                
                Core essentials:
                1. Intention (Niyyah): Quietly resolve in your heart to perform wudu for Allah.
                2. Washing Key Limbs: Face, hands up to elbows, wiping head, and feet up to ankles.
                3. Water Moderation: Even when next to a flowing river, the Sunnah urges extreme water conservation.
                
                Beyond the physical cleansing, scholars note that every droplet of water metaphorically washes away stress and sins committed by the limbs, preparing the mind for peaceful contemplation.
            """.trimIndent(),
            keyTakeaway = "Treat Wudu as a mindful transition from worldly bustle into divine tranquility.",
            reference = "Sahih Muslim 223, Al-Majmu' Sharh al-Muhadhdhab",
            date = "Today's Feature"
        ),
        IslamicArticle(
            id = "fiqh_fasting",
            title = "Fiqh of Fasting: Guarding the Core of Sawm",
            category = ArticleCategory.FIQH,
            readTime = "3 min read",
            summary = "A concise breakdown of the essential rulings of fasting, intentionality, and avoiding spiritual invalidation.",
            content = """
                Fasting (Sawm) in Islamic Law consists of refraining from food, drink, and marital relations from true dawn (Fajr) until sunset (Maghrib), accompanied by the intention of worship.
                
                What invalidates the fast:
                - Intentionally eating, drinking, or smoking during fasting hours.
                - Deliberate vomiting.
                
                What does NOT invalidate the fast:
                - Unintentional eating or drinking due to forgetfulness.
                - Using eye drops, ear drops (without eardrum perforation), or applying fragrance.
                - Tasting food on the tip of the tongue for cooking without swallowing.
                - Brushing teeth with Miswak or toothpaste without swallowing.
                
                The Prophet ﷺ reminded: 'Whoever does not abandon false speech and acting upon it, Allah has no need for him to leave his food and drink.'
            """.trimIndent(),
            keyTakeaway = "Fasting of the limbs and speech is just as essential as fasting of the stomach.",
            reference = "Sahih al-Bukhari 1903, Fiqh us-Sunnah",
            date = "Daily Reading"
        ),
        IslamicArticle(
            id = "fiqh_zakat",
            title = "Zakat & Sadaqah: Purifying Wealth and Uplifting Hearts",
            category = ArticleCategory.FIQH,
            readTime = "2 min read",
            summary = "Understand the spiritual mathematics of Zakat: giving does not decrease wealth, it sanctifies it.",
            content = """
                Zakat is the third pillar of Islam, calculated at 2.5% on qualifying wealth above the Nisab threshold held for one lunar year.
                
                Who receives Zakat:
                The Quran specifies eight categories (Surah At-Tawbah 9:60), principally the poor, the needy, those in debt, and travelers in difficulty.
                
                The Fiqh difference between Zakat and Sadaqah:
                - Zakat is an obligatory right of the impoverished upon surplus wealth.
                - Sadaqah is voluntary charity, unrestricted by thresholds, encompassing financial gifts, smiles, kind words, and removing harm from paths.
            """.trimIndent(),
            keyTakeaway = "Charity extinguishes sin as water extinguishes fire.",
            reference = "Surah At-Tawbah 9:60, Jami' at-Tirmidhi",
            date = "Yesterday"
        ),

        // AQEEDAH ARTICLES
        IslamicArticle(
            id = "aqeedah_tawakkul",
            title = "Tawakkul: Reliance on Allah While Tying Your Camel",
            category = ArticleCategory.AQEEDAH,
            readTime = "3 min read",
            summary = "Balance sincere proactive striving with complete peaceful surrender to divine decree.",
            content = """
                In Islamic theology (Aqeedah), Tawakkul represents the harmonious unity between exerting maximum effort and trusting the ultimate outcome to Allah alone.
                
                When a nomad asked the Messenger of Allah ﷺ: 'Should I tie my camel and rely upon Allah, or leave her loose and rely upon Him?' The Prophet ﷺ replied: 'Tie her, then rely upon Allah.'
                
                Key principles:
                1. Causes (Asbab): We are commanded to use our intellect, seek medicine, pursue education, and work diligently.
                2. Reliance (Qalb): Our hearts must never rely solely on worldly means, but on the Creator of all causes.
                
                This mindset frees a believer from crippling anxiety over things outside their control.
            """.trimIndent(),
            keyTakeaway = "Exert your hands in diligent effort; leave your heart in complete peaceful surrender.",
            reference = "Sunan al-Tirmidhi 2517, Al-Aqeedah Al-Tahawiyyah",
            date = "Today's Feature"
        ),
        IslamicArticle(
            id = "aqeedah_names",
            title = "Living with Al-Rahman and Al-Wadud",
            category = ArticleCategory.AQEEDAH,
            readTime = "2 min read",
            summary = "Discovering the depths of Allah's Infinite Mercy and Loving-Kindness in your daily life.",
            content = """
                Belief in the Divine Names and Attributes (Asma wa Sifat) is central to building an intimate connection with our Creator.
                
                - Al-Rahman (The Entirely Merciful): His vast compassion envelops every created being, from the breathing sparrow to the galaxy.
                - Al-Wadud (The Most Loving): Unlike human love which can be conditional, Allah loves those who strive toward goodness, and rejoices when a servant returns in repentance.
                
                Reflecting on these names transforms prayer from a routine ritual into an affectionate sanctuary.
            """.trimIndent(),
            keyTakeaway = "Whenever you feel alone, remember Al-Wadud is closer to you than your jugular vein.",
            reference = "Surah Hud 11:90, Sharh Asma' Allah al-Husna",
            date = "Daily Reading"
        ),

        // SEERAH ARTICLES
        IslamicArticle(
            id = "seerah_character",
            title = "The Prophet's ﷺ Character: Gentle, Truthful, and Resilient",
            category = ArticleCategory.SEERAH,
            readTime = "3 min read",
            summary = "A glimpse into how the Prophet Muhammad ﷺ exemplified compassion, emotional intelligence, and integrity.",
            content = """
                When Aisha (may Allah be pleased with her) was asked about the character of the Prophet ﷺ, she replied: 'His character was the Quran.'
                
                Notable hallmarks of his leadership:
                - Empathy: He never rebuked a servant or raised his voice over minor shortcomings. Anas ibn Malik served him for 10 years and said: 'He never even said "Uff" to me.'
                - Active Listening: When speaking with someone, he would turn his entire body toward them, giving them complete, undivided attention.
                - Humility: He repaired his own shoes, milked his goats, and served his household like any ordinary person.
            """.trimIndent(),
            keyTakeaway = "True greatness lies in gentleness, active listening, and sincere humility with all people.",
            reference = "Sahih Muslim 746, Ash-Shama'il al-Muhammadiyyah",
            date = "Today's Feature"
        ),
        IslamicArticle(
            id = "seerah_hijrah",
            title = "The Hijrah: Strategic Planning and Unshakable Faith",
            category = ArticleCategory.SEERAH,
            readTime = "3 min read",
            summary = "The migration from Mecca to Medina provides a timeless blueprint for endurance, preparation, and brotherhood.",
            content = """
                The Hijrah (622 CE) was not a hasty retreat, but a meticulously planned spiritual and civic transformation.
                
                Lessons from the journey:
                1. Thorough Preparation: Hiring an expert desert guide, selecting non-obvious routes, and arranging covert supplies.
                2. Unshakable Calm: When enemies stood right outside the Cave of Thawr, Abu Bakr feared for the Prophet's safety. The Prophet ﷺ serenely said: 'Do not grieve; indeed Allah is with us.'
                3. The Charter of Medina: Establishing history's earliest written constitution ensuring mutual defense and freedom of religion for all tribes.
            """.trimIndent(),
            keyTakeaway = "Every new beginning demands practical strategy accompanied by unwavering faith.",
            reference = "Ar-Raheeq Al-Makhtum (The Sealed Nectar), Ibn Hisham",
            date = "Daily Reading"
        )
    )

    fun getArticlesByCategory(category: ArticleCategory): List<IslamicArticle> {
        return if (category == ArticleCategory.ALL) articles else articles.filter { it.category == category }
    }
}
