package id.maoneart.flow.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "maoneart_flow_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val FAVORITES = stringSetPreferencesKey("favorites")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val ALPHABET_SIDE = stringPreferencesKey("alphabet_side")
        val SHOW_ICONS = booleanPreferencesKey("show_icons")
        val CUSTOM_CATEGORIES = stringSetPreferencesKey("custom_categories")
        val HIDDEN_CATEGORIES = stringSetPreferencesKey("hidden_categories")
        val APP_CATEGORY_OVERRIDES = stringPreferencesKey("app_category_overrides")
        val SHOW_MAONEART_BANNER = booleanPreferencesKey("show_maoneart_banner")
        val SEARCH_AS_ICON = booleanPreferencesKey("search_as_icon")
        val WIDGET_THEME = stringPreferencesKey("widget_theme")
        val SHOW_ALPHABET_SCROLLER = booleanPreferencesKey("show_alphabet_scroller")
        val PURE_TRANSPARENT_BG = booleanPreferencesKey("pure_transparent_bg")
        val SHOW_CATEGORIES = booleanPreferencesKey("show_categories")
    }

    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.FAVORITES] ?: setOf(
            "com.android.chrome",
            "com.whatsapp",
            "com.google.android.youtube",
            "com.termux"
        )
    }

    val hiddenAppsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.HIDDEN_APPS] ?: emptySet()
    }

    val hapticsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HAPTICS_ENABLED] ?: true
    }

    val alphabetSideFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.ALPHABET_SIDE] ?: "right"
    }

    val is24HourFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_24_HOUR] ?: true
    }

    val showIconsFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHOW_ICONS] ?: true
    }

    val showMaoneArtBannerFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHOW_MAONEART_BANNER] ?: true
    }

    val searchAsIconFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SEARCH_AS_ICON] ?: false
    }

    val widgetThemeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.WIDGET_THEME] ?: "classic"
    }

    val showAlphabetScrollerFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHOW_ALPHABET_SCROLLER] ?: true
    }

    val pureTransparentBgFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.PURE_TRANSPARENT_BG] ?: false
    }

    val showCategoriesFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SHOW_CATEGORIES] ?: true
    }

    val customCategoriesFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val set = prefs[Keys.CUSTOM_CATEGORIES] ?: emptySet()
        set.toList().sorted()
    }

    val hiddenCategoriesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.HIDDEN_CATEGORIES] ?: emptySet()
    }

    val appCategoryOverridesFlow: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        val rawJson = prefs[Keys.APP_CATEGORY_OVERRIDES] ?: "{}"
        try {
            val jsonObject = JSONObject(rawJson)
            val map = mutableMapOf<String, String>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val pkg = keys.next()
                map[pkg] = jsonObject.getString(pkg)
            }
            map
        } catch (_: Throwable) {
            emptyMap()
        }
    }

    suspend fun toggleFavorite(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES]?.toMutableSet() ?: mutableSetOf(
                "com.android.chrome", "com.whatsapp", "com.google.android.youtube", "com.termux"
            )
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            prefs[Keys.FAVORITES] = current
        }
    }

    suspend fun toggleHidden(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.HIDDEN_APPS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            prefs[Keys.HIDDEN_APPS] = current
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setAlphabetSide(side: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ALPHABET_SIDE] = side
        }
    }

    suspend fun set24Hour(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_24_HOUR] = enabled
        }
    }

    suspend fun setShowIcons(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SHOW_ICONS] = show
        }
    }

    suspend fun addCustomCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CUSTOM_CATEGORIES]?.toMutableSet() ?: mutableSetOf()
            current.add(trimmed)
            prefs[Keys.CUSTOM_CATEGORIES] = current
            // If it was hidden, unhide it
            val hidden = prefs[Keys.HIDDEN_CATEGORIES]?.toMutableSet() ?: mutableSetOf()
            if (hidden.remove(trimmed)) {
                prefs[Keys.HIDDEN_CATEGORIES] = hidden
            }
        }
    }

    suspend fun removeCustomCategory(name: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CUSTOM_CATEGORIES]?.toMutableSet() ?: mutableSetOf()
            current.remove(name)
            prefs[Keys.CUSTOM_CATEGORIES] = current

            // Clean up any app overrides pointing to this deleted category
            val rawJson = prefs[Keys.APP_CATEGORY_OVERRIDES] ?: "{}"
            try {
                val jsonObject = JSONObject(rawJson)
                val newObj = JSONObject()
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val v = jsonObject.getString(k)
                    if (v != name) {
                        newObj.put(k, v)
                    }
                }
                prefs[Keys.APP_CATEGORY_OVERRIDES] = newObj.toString()
            } catch (_: Throwable) {}
        }
    }

    suspend fun hideCategory(categoryId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.HIDDEN_CATEGORIES]?.toMutableSet() ?: mutableSetOf()
            current.add(categoryId)
            prefs[Keys.HIDDEN_CATEGORIES] = current
        }
    }

    suspend fun unhideCategory(categoryId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.HIDDEN_CATEGORIES]?.toMutableSet() ?: mutableSetOf()
            current.remove(categoryId)
            prefs[Keys.HIDDEN_CATEGORIES] = current
        }
    }

    suspend fun setAppCategoryOverride(packageName: String, categoryId: String) {
        context.dataStore.edit { prefs ->
            val rawJson = prefs[Keys.APP_CATEGORY_OVERRIDES] ?: "{}"
            val jsonObject = try {
                JSONObject(rawJson)
            } catch (_: Throwable) {
                JSONObject()
            }
            jsonObject.put(packageName, categoryId)
            prefs[Keys.APP_CATEGORY_OVERRIDES] = jsonObject.toString()
        }
    }

    suspend fun resetAppCategoryOverride(packageName: String) {
        context.dataStore.edit { prefs ->
            val rawJson = prefs[Keys.APP_CATEGORY_OVERRIDES] ?: "{}"
            try {
                val jsonObject = JSONObject(rawJson)
                jsonObject.remove(packageName)
                prefs[Keys.APP_CATEGORY_OVERRIDES] = jsonObject.toString()
            } catch (_: Throwable) {}
        }
    }

    suspend fun setShowMaoneArtBanner(show: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SHOW_MAONEART_BANNER] = show }
    }

    suspend fun setSearchAsIcon(asIcon: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SEARCH_AS_ICON] = asIcon }
    }

    suspend fun setWidgetTheme(theme: String) {
        context.dataStore.edit { prefs -> prefs[Keys.WIDGET_THEME] = theme }
    }

    suspend fun setShowAlphabetScroller(show: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SHOW_ALPHABET_SCROLLER] = show }
    }

    suspend fun setPureTransparentBg(transparent: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.PURE_TRANSPARENT_BG] = transparent }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setIs24Hour(is24H: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_24_HOUR] = is24H }
    }

    suspend fun setShowIcons(show: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SHOW_ICONS] = show }
    }

    suspend fun setShowCategories(show: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SHOW_CATEGORIES] = show }
    }
}
