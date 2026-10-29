package id.maoneart.flow.ui.home

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.maoneart.flow.data.AppRepository
import id.maoneart.flow.data.PreferencesRepository
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.model.CategoryItem
import id.maoneart.flow.model.DefaultCategories
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
    val categories: List<CategoryItem> = emptyList(),
    val selectedCategoryId: String = "all",
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

private data class AppsAndCategoriesData(
    val apps: List<AppInfo> = emptyList(),
    val categories: List<CategoryItem> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)
    private val preferencesRepository = PreferencesRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("all")
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp: StateFlow<AppInfo?> = _selectedApp.asStateFlow()

    // Dialog & bottom sheet states - directly observable for 0ms immediate response
    private val _appForCategoryChange = MutableStateFlow<AppInfo?>(null)
    val appForCategoryChange: StateFlow<AppInfo?> = _appForCategoryChange.asStateFlow()

    private val _isCreateCategoryOpen = MutableStateFlow(false)
    val isCreateCategoryOpen: StateFlow<Boolean> = _isCreateCategoryOpen.asStateFlow()

    private val _categoryToDelete = MutableStateFlow<CategoryItem?>(null)
    val categoryToDelete: StateFlow<CategoryItem?> = _categoryToDelete.asStateFlow()

    private val settingsFlow = combine(
        preferencesRepository.is24HourFlow,
        preferencesRepository.hapticsEnabledFlow,
        preferencesRepository.alphabetSideFlow,
        preferencesRepository.showIconsFlow
    ) { is24H, haptics, side, showIcons ->
        LauncherSettings(is24H, haptics, side, showIcons)
    }

    private val categoryConfigFlow = combine(
        preferencesRepository.appCategoryOverridesFlow,
        preferencesRepository.customCategoriesFlow,
        preferencesRepository.hiddenCategoriesFlow
    ) { overrides, customCats, hiddenCats ->
        Triple(overrides, customCats, hiddenCats)
    }

    private val appsWithCategoriesFlow = combine(
        appRepository.installedAppsFlow,
        preferencesRepository.favoritesFlow,
        preferencesRepository.hiddenAppsFlow,
        categoryConfigFlow
    ) { rawApps, favs, hidden, (overrides, customCats, hiddenCats) ->

        // 1. Filter out hidden apps and apply favorite & category overrides
        val processedApps = rawApps
            .filter { !hidden.contains(it.packageName) }
            .map { app ->
                val overrideCat = overrides[app.packageName]
                if (overrideCat != null) {
                    val title = if (customCats.contains(overrideCat)) {
                        overrideCat
                    } else {
                        DefaultCategories.getTitle(overrideCat)
                    }
                    val icon = if (customCats.contains(overrideCat)) {
                        "📁"
                    } else {
                        DefaultCategories.getIcon(overrideCat)
                    }
                    app.copy(
                        isFavorite = favs.contains(app.packageName),
                        categoryId = overrideCat,
                        categoryTitle = title,
                        categoryIcon = icon
                    )
                } else {
                    app.copy(isFavorite = favs.contains(app.packageName))
                }
            }

        // 2. Count apps per category
        val categoryCounts = mutableMapOf<String, Int>()
        categoryCounts["all"] = processedApps.size
        for (app in processedApps) {
            categoryCounts[app.categoryId] = (categoryCounts[app.categoryId] ?: 0) + 1
        }

        // 3. Construct category items (filter out hidden ones, 'all' is always preserved)
        val defaultList = DefaultCategories.LIST
            .filter { it.id == "all" || !hiddenCats.contains(it.id) }
            .map { def -> def.copy(appCount = categoryCounts[def.id] ?: 0) }

        val customList = customCats
            .filter { !hiddenCats.contains(it) }
            .map { catName ->
                CategoryItem(
                    id = catName,
                    title = catName,
                    icon = "📁",
                    isCustom = true,
                    appCount = categoryCounts[catName] ?: 0
                )
            }

        val allCategories = defaultList + customList
        AppsAndCategoriesData(apps = processedApps, categories = allCategories)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        appsWithCategoriesFlow,
        settingsFlow,
        _searchQuery,
        _selectedCategoryId,
        _selectedApp
    ) { appsData, settings, query, catId, selApp ->

        val visibleApps = appsData.apps
        val favoritesList = visibleApps.filter { it.isFavorite }

        // Filter apps by category
        val categoryFilteredApps = if (catId == "all") {
            visibleApps
        } else {
            visibleApps.filter { it.categoryId == catId }
        }

        val finalDisplayApps = if (query.isBlank()) {
            categoryFilteredApps
        } else {
            val q = query.trim().lowercase()
            // When searching, query across all visible apps
            visibleApps.filter {
                it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        }

        val letters = finalDisplayApps.map { it.firstLetter }.toSet()

        // Compute index mapping for instant alphabet jump
        val indices = mutableMapOf<Char, Int>()
        finalDisplayApps.forEachIndexed { index, app ->
            if (!indices.containsKey(app.firstLetter)) {
                indices[app.firstLetter] = index
            }
        }

        HomeUiState(
            allApps = visibleApps,
            favorites = favoritesList,
            displayApps = finalDisplayApps,
            categories = appsData.categories,
            selectedCategoryId = catId,
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
        started = SharingStarted.Eagerly,
        initialValue = run {
            val cached = AppRepository.getCachedApps() ?: emptyList()
            val letters = cached.map { it.firstLetter }.toSet()
            val indices = mutableMapOf<Char, Int>()
            cached.forEachIndexed { idx, app ->
                if (!indices.containsKey(app.firstLetter)) indices[app.firstLetter] = idx
            }
            HomeUiState(
                allApps = cached,
                displayApps = cached,
                availableLetters = letters,
                letterIndices = indices
            )
        }
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun selectAppForAction(app: AppInfo?) {
        _selectedApp.value = app
    }

    fun openCategoryChangeDialog(app: AppInfo?) {
        _appForCategoryChange.value = app
    }

    fun openCreateCategoryDialog(open: Boolean) {
        _isCreateCategoryOpen.value = open
    }

    fun openDeleteCategoryDialog(category: CategoryItem?) {
        _categoryToDelete.value = category
    }

    fun createCustomCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            preferencesRepository.addCustomCategory(trimmed)
            _selectedCategoryId.value = trimmed
            _isCreateCategoryOpen.value = false
        }
    }

    fun deleteCategory(category: CategoryItem) {
        viewModelScope.launch {
            if (category.isCustom) {
                preferencesRepository.removeCustomCategory(category.id)
            } else {
                preferencesRepository.hideCategory(category.id)
            }
            if (_selectedCategoryId.value == category.id) {
                _selectedCategoryId.value = "all"
            }
            _categoryToDelete.value = null
        }
    }

    fun assignAppCategory(app: AppInfo, categoryId: String) {
        viewModelScope.launch {
            preferencesRepository.setAppCategoryOverride(app.packageName, categoryId)
            _appForCategoryChange.value = null
        }
    }

    fun resetAppCategory(app: AppInfo) {
        viewModelScope.launch {
            preferencesRepository.resetAppCategoryOverride(app.packageName)
            _appForCategoryChange.value = null
        }
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
