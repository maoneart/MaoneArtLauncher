package id.maoneart.flow.ui.home

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.maoneart.flow.data.AppRepository
import id.maoneart.flow.data.PreferencesRepository
import id.maoneart.flow.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val allApps: List<AppInfo> = emptyList(),
    val favorites: List<AppInfo> = emptyList(),
    val displayApps: List<AppInfo> = emptyList(),
    val availableLetters: Set<Char> = emptySet(),
    val letterIndices: Map<Char, Int> = emptyMap(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val is24Hour: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val alphabetSide: String = "right",
    val showIcons: Boolean = true,
    val selectedApp: AppInfo? = null
)

private data class LauncherSettings(
    val is24Hour: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val alphabetSide: String = "right",
    val showIcons: Boolean = true
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)
    private val preferencesRepository = PreferencesRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp: StateFlow<AppInfo?> = _selectedApp.asStateFlow()

    private val settingsFlow = combine(
        preferencesRepository.is24HourFlow,
        preferencesRepository.hapticsEnabledFlow,
        preferencesRepository.alphabetSideFlow,
        preferencesRepository.showIconsFlow
    ) { is24H, haptics, side, showIcons ->
        LauncherSettings(is24H, haptics, side, showIcons)
    }

    private val appsFlow = combine(
        appRepository.installedAppsFlow,
        preferencesRepository.favoritesFlow,
        preferencesRepository.hiddenAppsFlow
    ) { apps, favs, hidden ->
        apps.filter { !hidden.contains(it.packageName) }
            .map { it.copy(isFavorite = favs.contains(it.packageName)) }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        appsFlow,
        settingsFlow,
        _searchQuery,
        _selectedApp
    ) { visibleApps, settings, query, selApp ->

        val favoritesList = visibleApps.filter { it.isFavorite }

        val filteredApps = if (query.isBlank()) {
            visibleApps
        } else {
            val q = query.trim().lowercase()
            visibleApps.filter {
                it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        }

        val letters = filteredApps.map { it.firstLetter }.toSet()

        // Compute index mapping for instant alphabet jump
        val indices = mutableMapOf<Char, Int>()
        filteredApps.forEachIndexed { index, app ->
            if (!indices.containsKey(app.firstLetter)) {
                indices[app.firstLetter] = index
            }
        }

        HomeUiState(
            allApps = visibleApps,
            favorites = favoritesList,
            displayApps = filteredApps,
            availableLetters = letters,
            letterIndices = indices,
            searchQuery = query,
            isSearching = query.isNotBlank(),
            is24Hour = settings.is24Hour,
            hapticsEnabled = settings.hapticsEnabled,
            alphabetSide = settings.alphabetSide,
            showIcons = settings.showIcons,
            selectedApp = selApp
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun selectAppForAction(app: AppInfo?) {
        _selectedApp.value = app
    }

    fun launchApp(app: AppInfo): Boolean {
        return appRepository.launchApp(app)
    }

    fun getAppIcon(packageName: String): Drawable? {
        return appRepository.getAppIcon(packageName)
    }

    fun toggleFavorite(app: AppInfo) {
        viewModelScope.launch {
            preferencesRepository.toggleFavorite(app.packageName)
        }
    }

    fun toggleHidden(app: AppInfo) {
        viewModelScope.launch {
            preferencesRepository.toggleHidden(app.packageName)
        }
    }

    fun openAppDetails(app: AppInfo) {
        appRepository.openAppDetails(app.packageName)
    }

    fun uninstallApp(app: AppInfo) {
        appRepository.uninstallApp(app.packageName)
    }
}
