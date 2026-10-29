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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "maoneart_flow_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val FAVORITES = stringSetPreferencesKey("favorites")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val ALPHABET_SIDE = stringPreferencesKey("alphabet_side")
        val SHOW_ICONS = booleanPreferencesKey("show_icons")
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
}
