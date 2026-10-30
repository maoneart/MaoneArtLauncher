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
import id.maoneart.flow.service.NotificationService
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
    val hiddenApps: List<AppInfo> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val hasHiddenCategories: Boolean = false,
    val selectedCategoryId: String = "all",
    val availableLetters: Set<Char> = emptySet(),
    val letterIndices: Map<Char, Int> = emptyMap(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val is24Hour: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val alphabetSide: String = "right",
    val showIcons: Boolean = true,
    val hasNotificationAccess: Boolean = true,
    val showMaoneArtBanner: Boolean = true,
    val searchAsIcon: Boolean = false,
    val widgetTheme: String = "classic",
    val showAlphabetScroller: Boolean = true,
    val pureTransparentBg: Boolean = false,
    val showCategories: Boolean = true,
    val twoLineAppNames: Boolean = true,
    val isSettingsOpen: Boolean = false,
    val isHiddenAppsSheetOpen: Boolean = false,
    val selectedApp: AppInfo? = null
)

private data class AppearanceSettings(
    val showMaoneArtBanner: Boolean = true,
    val searchAsIcon: Boolean = false,
    val widgetTheme: String = "classic",
    val showAlphabetScroller: Boolean = true,
    val pureTransparentBg: Boolean = false,
    val showCategories: Boolean = true,
    val twoLineAppNames: Boolean = true
)

private data class GeneralSettings(
    val is24Hour: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val alphabetSide: String = "right",
    val showIcons: Boolean = true,
    val hasNotificationAccess: Boolean = true
)

private data class AppsAndCategoriesData(
    val apps: List<AppInfo> = emptyList(),
    val hiddenApps: List<AppInfo> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val hasHiddenCategories: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)
    private val preferencesRepository = PreferencesRepository(application)

    private val _hasNotificationAccess = MutableStateFlow(
        NotificationService.isNotificationAccessGranted(application)
    )
    val hasNotificationAccess: StateFlow<Boolean> = _hasNotificationAccess.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow("all")
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp: StateFlow<AppInfo?> = _selectedApp.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isHiddenAppsSheetOpen = MutableStateFlow(false)
    val isHiddenAppsSheetOpen: StateFlow<Boolean> = _isHiddenAppsSheetOpen.asStateFlow()

    // Dialog & bottom sheet states - directly observable for 0ms immediate response
    private val _appForCategoryChange = MutableStateFlow<AppInfo?>(null)
    val appForCategoryChange: StateFlow<AppInfo?> = _appForCategoryChange.asStateFlow()

    private val _isCreateCategoryOpen = MutableStateFlow(false)
    val isCreateCategoryOpen: StateFlow<Boolean> = _isCreateCategoryOpen.asStateFlow()

    private val _categoryToDelete = MutableStateFlow<CategoryItem?>(null)
    val categoryToDelete: StateFlow<CategoryItem?> = _categoryToDelete.asStateFlow()

    private val appearanceSettingsFlow = combine(
        preferencesRepository.showMaoneArtBannerFlow,
        preferencesRepository.searchAsIconFlow,
        preferencesRepository.widgetThemeFlow,
        preferencesRepository.showAlphabetScrollerFlow,
        combine(
            preferencesRepository.pureTransparentBgFlow,
            preferencesRepository.showCategoriesFlow,
            preferencesRepository.twoLineAppNamesFlow
        ) { pureBg, showCats, twoLine -> Triple(pureBg, showCats, twoLine) }
    ) { banner, asIcon, theme, scroller, (pureBg, showCats, twoLine) ->
        AppearanceSettings(banner, asIcon, theme, scroller, pureBg, showCats, twoLine)
    }

    private val generalSettingsFlow = combine(
        preferencesRepository.is24HourFlow,
        preferencesRepository.hapticsEnabledFlow,
        preferencesRepository.alphabetSideFlow,
        preferencesRepository.showIconsFlow,
        _hasNotificationAccess
    ) { is24H, haptics, side, showIcons, hasNotifAccess ->
        GeneralSettings(is24H, haptics, side, showIcons, hasNotifAccess)
    }

    private val settingsFlow = combine(
        appearanceSettingsFlow,
        generalSettingsFlow
    ) { appearance, general ->
        Pair(appearance, general)
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
        categoryConfigFlow,
        NotificationService.notificationCounts
    ) { rawApps, favs, hidden, (overrides, customCats, hiddenCats), notifCounts ->

        // 1. Filter out hidden apps and apply favorite & category overrides & notification counts
        val processedApps = rawApps
            .filter { !hidden.contains(it.packageName) }
            .map { app ->
                val notifCount = notifCounts[app.packageName] ?: 0
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
                        categoryIcon = icon,
                        notificationCount = notifCount
                    )
                } else {
                    app.copy(
                        isFavorite = favs.contains(app.packageName),
                        notificationCount = notifCount
                    )
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

        // Strictly deduplicate by category.id to eliminate Compose LazyRow duplicate key crashes
        val allCategories = (defaultList + customList).distinctBy { it.id }

        // Filter hidden apps list for recovery and management
        val hiddenAppsList = rawApps.filter { hidden.contains(it.packageName) }
        val hasHiddenCats = hiddenCats.isNotEmpty()

        AppsAndCategoriesData(
            apps = processedApps,
            hiddenApps = hiddenAppsList,
            categories = allCategories,
            hasHiddenCategories = hasHiddenCats
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        appsWithCategoriesFlow,
        settingsFlow,
        combine(_searchQuery, _selectedCategoryId, _isSearchActive) { q, c, active -> Triple(q, c, active) },
        _selectedApp,
        combine(_isSettingsOpen, _isHiddenAppsSheetOpen) { settings, hiddenApps -> Pair(settings, hiddenApps) }
    ) { appsData, (appearance, general), (query, catId, isSearchActive), selApp, (isSettingsOpen, isHiddenAppsOpen) ->

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
        val indices = computeAlphabetIndices(finalDisplayApps)

        HomeUiState(
            allApps = visibleApps,
            favorites = favoritesList,
            displayApps = finalDisplayApps,
            hiddenApps = appsData.hiddenApps,
            categories = appsData.categories,
            hasHiddenCategories = appsData.hasHiddenCategories,
            selectedCategoryId = catId,
            availableLetters = letters,
            letterIndices = indices,
            searchQuery = query,
            isSearching = query.isNotBlank() || isSearchActive,
            is24Hour = general.is24Hour,
            hapticsEnabled = general.hapticsEnabled,
            alphabetSide = general.alphabetSide,
            showIcons = general.showIcons,
            hasNotificationAccess = general.hasNotificationAccess,
            showMaoneArtBanner = appearance.showMaoneArtBanner,
            searchAsIcon = appearance.searchAsIcon,
            widgetTheme = appearance.widgetTheme,
            showAlphabetScroller = appearance.showAlphabetScroller,
            pureTransparentBg = appearance.pureTransparentBg,
            showCategories = appearance.showCategories,
            twoLineAppNames = appearance.twoLineAppNames,
            isSettingsOpen = isSettingsOpen,
            isHiddenAppsSheetOpen = isHiddenAppsOpen,
            selectedApp = selApp
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = run {
            val cached = AppRepository.getCachedApps() ?: emptyList()
            val letters = cached.map { it.firstLetter }.toSet()
            val indices = computeAlphabetIndices(cached)
            HomeUiState(
                allApps = cached,
                displayApps = cached,
                availableLetters = letters,
                letterIndices = indices
            )
        }
    )

    private fun computeAlphabetIndices(apps: List<AppInfo>): Map<Char, Int> {
        if (apps.isEmpty()) return emptyMap()
        val exactIndices = mutableMapOf<Char, Int>()
        apps.forEachIndexed { index, app ->
            if (!exactIndices.containsKey(app.firstLetter)) {
                exactIndices[app.firstLetter] = index
            }
        }

        val fullAlphabet = ('A'..'Z').toList() + listOf('#')
        val completeIndices = mutableMapOf<Char, Int>()

        for (i in fullAlphabet.indices) {
            val letter = fullAlphabet[i]
            val exact = exactIndices[letter]
            if (exact != null) {
                completeIndices[letter] = exact
            } else {
                // Find nearest forward letter with apps
                var foundIndex: Int? = null
                for (f in (i + 1) until fullAlphabet.size) {
                    val fLetter = fullAlphabet[f]
                    if (exactIndices.containsKey(fLetter)) {
                        foundIndex = exactIndices[fLetter]
                        break
                    }
                }
                // If no forward, find nearest backward letter with apps
                if (foundIndex == null) {
                    for (b in (i - 1) downTo 0) {
                        val bLetter = fullAlphabet[b]
                        if (exactIndices.containsKey(bLetter)) {
                            foundIndex = exactIndices[bLetter]
                            break
                        }
                    }
                }
                if (foundIndex != null) {
                    completeIndices[letter] = foundIndex
                }
            }
        }
        return completeIndices
    }

    fun resetToHomeScreen() {
        if (_searchQuery.value.isNotEmpty()) _searchQuery.value = ""
        if (_isSearchActive.value) _isSearchActive.value = false
        if (_isSettingsOpen.value) _isSettingsOpen.value = false
        if (_isHiddenAppsSheetOpen.value) _isHiddenAppsSheetOpen.value = false
        if (_selectedApp.value != null) _selectedApp.value = null
        if (_appForCategoryChange.value != null) _appForCategoryChange.value = null
        if (_categoryToDelete.value != null) _categoryToDelete.value = null
        if (_isCreateCategoryOpen.value) _isCreateCategoryOpen.value = false
        if (_selectedCategoryId.value != "all") _selectedCategoryId.value = "all"
    }

    fun checkNotificationPermission() {
        _hasNotificationAccess.value = NotificationService.isNotificationAccessGranted(getApplication())
    }

    fun openNotificationSettings() {
        NotificationService.openNotificationAccessSettings(getApplication())
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.isNotBlank()) {
            _isSearchActive.value = true
        }
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
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
        if (app.packageName == getApplication<Application>().packageName) {
            _isSettingsOpen.value = true
            return true
        }
        val launched = appRepository.launchApp(app)
        if (launched && _isSearchActive.value) {
            _isSearchActive.value = false
            _searchQuery.value = ""
        }
        return launched
    }

    fun openSettings(open: Boolean) {
        _isSettingsOpen.value = open
    }

    fun openHiddenAppsSheet(open: Boolean) {
        _isHiddenAppsSheetOpen.value = open
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            preferencesRepository.unhideApp(packageName)
        }
    }

    fun restoreDefaultCategories() {
        viewModelScope.launch {
            preferencesRepository.unhideAllCategories()
        }
    }

    fun setShowMaoneArtBanner(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowMaoneArtBanner(show) }
    }

    fun setSearchAsIcon(asIcon: Boolean) {
        viewModelScope.launch { preferencesRepository.setSearchAsIcon(asIcon) }
    }

    fun setWidgetTheme(theme: String) {
        viewModelScope.launch { preferencesRepository.setWidgetTheme(theme) }
    }

    fun setShowAlphabetScroller(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowAlphabetScroller(show) }
    }

    fun setPureTransparentBg(transparent: Boolean) {
        viewModelScope.launch { preferencesRepository.setPureTransparentBg(transparent) }
    }

    fun setShowCategories(show: Boolean) {
        if (!show) {
            _selectedCategoryId.value = "all"
        }
        viewModelScope.launch { preferencesRepository.setShowCategories(show) }
    }

    fun setShowIcons(show: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowIcons(show) }
    }

    fun setTwoLineAppNames(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setTwoLineAppNames(enabled) }
    }

    fun setIs24Hour(is24H: Boolean) {
        viewModelScope.launch { preferencesRepository.set24Hour(is24H) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setHapticsEnabled(enabled) }
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
