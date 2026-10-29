package id.maoneart.flow.model

data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val firstLetter: Char = computeFirstLetter(label),
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
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
