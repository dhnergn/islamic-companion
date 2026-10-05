package com.example.data

enum class ArticleCategory(val title: String) {
    ALL("All Topics"),
    FIQH("Fiqh (Jurisprudence)"),
    AQEEDAH("Aqeedah (Creed)"),
    SEERAH("Seerah (Prophetic Biography)")
}

data class IslamicArticle(
    val id: String,
    val title: String,
    val category: ArticleCategory,
    val readTime: String,
    val summary: String,
    val content: String,
    val keyTakeaway: String,
    val reference: String,
    val date: String
)
