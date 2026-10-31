package id.maoneart.flow.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.io.IOException

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
        val TWO_LINE_APP_NAMES = booleanPreferencesKey("two_line_app_names")
        val SEARCH_LONG_PRESS_ACTION = stringPreferencesKey("search_long_press_action")
    }

    // IO-Safe preferences flow that catches any disk corruption gracefully without crashing the app
    private val safePreferences: Flow<Preferences> = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }

    // IO-Safe preferences edit helper to ensure background write locks or storage pressure never crash the app
    private suspend fun safeEdit(action: (MutablePreferences) -> Unit) {
        try {
            context.dataStore.edit { prefs ->
                action(prefs)
            }
        } catch (_: Throwable) {}
    }

    val favoritesFlow: Flow<Set<String>> = safePreferences.map { prefs ->
        if (prefs.contains(Keys.FAVORITES)) {
            prefs[Keys.FAVORITES] ?: emptySet()
        } else {
            setOf(
                "com.android.chrome",
                "com.whatsapp",
                "com.google.android.youtube",
                "com.termux"
            )
        }
    }

    val hiddenAppsFlow: Flow<Set<String>> = safePreferences.map { prefs ->
        prefs[Keys.HIDDEN_APPS] ?: emptySet()
    }

    val hapticsEnabledFlow: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[Keys.HAPTICS_ENABLED] ?: true
    }

    val alphabetSideFlow: Flow<String> = safePreferences.map { prefs ->
        prefs[Keys.ALPHABET_SIDE] ?: "right"
    }

    val is24HourFlow: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[Keys.IS_24_HOUR] ?: true
    }

    val showIconsFlow: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[Keys.SHOW_ICONS] ?: true
    }

    val showMaoneArtBannerFlow: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[Keys.SHOW_MAONEART_BANNER] ?: true
    }

    val searchAsIconFlow: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[Keys.SEARCH_AS_ICON] ?: false
    }

    val widgetThemeFlow: Flow<String> = safePreferences.map { prefs ->
        prefs[Keys.WIDGET_THEME] ?: "classic"
    }

    val showAlphabetScrollerFlow: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[Keys.SHOW_ALPHABET_SCROLLER] ?: true
    }

    val pureTransparentBgFlow: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[Keys.PURE_TRANSPARENT_BG] ?: false
    }

    val showCategoriesFlow: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[Keys.SHOW_CATEGORIES] ?: true
    }

    val twoLineAppNamesFlow: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[Keys.TWO_LINE_APP_NAMES] ?: true
    }

    val searchLongPressActionFlow: Flow<String> = safePreferences.map { prefs ->
        prefs[Keys.SEARCH_LONG_PRESS_ACTION] ?: "menu"
    }

    val customCategoriesFlow: Flow<List<String>> = safePreferences.map { prefs ->
        val set = prefs[Keys.CUSTOM_CATEGORIES] ?: emptySet()
        set.toList().sorted()
    }

    val hiddenCategoriesFlow: Flow<Set<String>> = safePreferences.map { prefs ->
        prefs[Keys.HIDDEN_CATEGORIES] ?: emptySet()
    }

    val appCategoryOverridesFlow: Flow<Map<String, String>> = safePreferences.map { prefs ->
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
        safeEdit { prefs ->
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
        safeEdit { prefs ->
            val current = prefs[Keys.HIDDEN_APPS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            prefs[Keys.HIDDEN_APPS] = current
        }
    }

    suspend fun unhideApp(packageName: String) {
        safeEdit { prefs ->
            val current = prefs[Keys.HIDDEN_APPS]?.toMutableSet() ?: mutableSetOf()
            current.remove(packageName)
            prefs[Keys.HIDDEN_APPS] = current
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        safeEdit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setAlphabetSide(side: String) {
        safeEdit { prefs ->
            prefs[Keys.ALPHABET_SIDE] = side
        }
    }

    suspend fun set24Hour(enabled: Boolean) {
        safeEdit { prefs ->
            prefs[Keys.IS_24_HOUR] = enabled
        }
    }

    suspend fun setShowIcons(show: Boolean) {
        safeEdit { prefs ->
            prefs[Keys.SHOW_ICONS] = show
        }
    }

    suspend fun addCustomCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        safeEdit { prefs ->
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
        safeEdit { prefs ->
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
        safeEdit { prefs ->
            val current = prefs[Keys.HIDDEN_CATEGORIES]?.toMutableSet() ?: mutableSetOf()
            current.add(categoryId)
            prefs[Keys.HIDDEN_CATEGORIES] = current
        }
    }

    suspend fun unhideCategory(categoryId: String) {
        safeEdit { prefs ->
            val current = prefs[Keys.HIDDEN_CATEGORIES]?.toMutableSet() ?: mutableSetOf()
            current.remove(categoryId)
            prefs[Keys.HIDDEN_CATEGORIES] = current
        }
    }

    suspend fun unhideAllCategories() {
        safeEdit { prefs ->
            prefs[Keys.HIDDEN_CATEGORIES] = emptySet()
        }
    }

    suspend fun setAppCategoryOverride(packageName: String, categoryId: String) {
        safeEdit { prefs ->
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
        safeEdit { prefs ->
            val rawJson = prefs[Keys.APP_CATEGORY_OVERRIDES] ?: "{}"
            try {
                val jsonObject = JSONObject(rawJson)
                jsonObject.remove(packageName)
                prefs[Keys.APP_CATEGORY_OVERRIDES] = jsonObject.toString()
            } catch (_: Throwable) {}
        }
    }

    suspend fun setShowMaoneArtBanner(show: Boolean) {
        safeEdit { prefs -> prefs[Keys.SHOW_MAONEART_BANNER] = show }
    }

    suspend fun setSearchAsIcon(asIcon: Boolean) {
        safeEdit { prefs -> prefs[Keys.SEARCH_AS_ICON] = asIcon }
    }

    suspend fun setWidgetTheme(theme: String) {
        safeEdit { prefs -> prefs[Keys.WIDGET_THEME] = theme }
    }

    suspend fun setShowAlphabetScroller(show: Boolean) {
        safeEdit { prefs -> prefs[Keys.SHOW_ALPHABET_SCROLLER] = show }
    }

    suspend fun setPureTransparentBg(transparent: Boolean) {
        safeEdit { prefs -> prefs[Keys.PURE_TRANSPARENT_BG] = transparent }
    }

    suspend fun setShowCategories(show: Boolean) {
        safeEdit { prefs -> prefs[Keys.SHOW_CATEGORIES] = show }
    }

    suspend fun setTwoLineAppNames(enabled: Boolean) {
        safeEdit { prefs -> prefs[Keys.TWO_LINE_APP_NAMES] = enabled }
    }

    suspend fun setSearchLongPressAction(action: String) {
        safeEdit { prefs -> prefs[Keys.SEARCH_LONG_PRESS_ACTION] = action }
    }
}
