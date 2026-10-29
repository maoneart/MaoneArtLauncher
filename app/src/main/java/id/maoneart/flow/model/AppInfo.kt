package id.maoneart.flow.model

import androidx.compose.runtime.Immutable

@Immutable
data class CategoryItem(
    val id: String,
    val title: String,
    val icon: String = "📁",
    val isCustom: Boolean = false,
    val appCount: Int = 0
)

object DefaultCategories {
    val ALL = CategoryItem("all", "Semua", "✦")
    val SOCIAL = CategoryItem("social", "Sosial", "💬")
    val MEDIA = CategoryItem("media", "Media", "🎬")
    val PRODUCTIVITY = CategoryItem("productivity", "Alat & Kerja", "⚡")
    val GAMES = CategoryItem("games", "Game", "🎮")
    val SYSTEM = CategoryItem("system", "Sistem", "⚙️")

    val LIST = listOf(ALL, SOCIAL, MEDIA, PRODUCTIVITY, GAMES, SYSTEM)

    fun getTitle(id: String): String {
        return LIST.firstOrNull { it.id == id }?.title ?: id
    }

    fun getIcon(id: String): String {
        return LIST.firstOrNull { it.id == id }?.icon ?: "📁"
    }
}

@Immutable
data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val firstLetter: Char = computeFirstLetter(label),
    val categoryId: String = "productivity",
    val categoryTitle: String = "Alat & Kerja",
    val categoryIcon: String = "⚡",
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val notificationCount: Int = 0,
    val installTime: Long = 0L
) {
    val id: String get() = "$packageName/$activityName"

    companion object {
        fun computeFirstLetter(name: String): Char {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return '#'
            val first = trimmed.first().uppercaseChar()
            return if (first in 'A'..'Z') first else '#'
        }
    }
}
