package id.maoneart.flow.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import id.maoneart.flow.ui.util.rememberFlowHaptics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import id.maoneart.flow.ui.components.AiAssistantBottomSheet
import id.maoneart.flow.ui.components.AlphabetWaveScroller
import id.maoneart.flow.ui.components.AppActionBottomSheet
import id.maoneart.flow.ui.components.AppListItem
import id.maoneart.flow.ui.components.CategoryChipBar
import id.maoneart.flow.ui.components.CreateCategoryDialog
import id.maoneart.flow.ui.components.DeleteCategoryDialog
import id.maoneart.flow.ui.components.DigitalClockHeader
import id.maoneart.flow.ui.components.FlowSearchBar
import id.maoneart.flow.ui.components.HiddenAppsBottomSheet
import id.maoneart.flow.ui.components.LauncherSettingsBottomSheet
import id.maoneart.flow.ui.components.SelectCategoryBottomSheet
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowTextSecondary
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isCreateCategoryOpen by viewModel.isCreateCategoryOpen.collectAsState()
    val categoryToDelete by viewModel.categoryToDelete.collectAsState()
    val appForCategoryChange by viewModel.appForCategoryChange.collectAsState()

    val listState = rememberLazyListState()
    val chipListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val haptics = rememberFlowHaptics()
    var scrollJob by remember { mutableStateOf<Job?>(null) }

    // Auto refresh notification access permission on launcher resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkNotificationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Smooth instant reset scroll position on category change to prevent layout freeze
    LaunchedEffect(uiState.selectedCategoryId) {
        try {
            listState.scrollToItem(0)
        } catch (_: Throwable) {}
    }

    // Back Navigation Hierarchy (Strict Zero Leakage Focus):
    // 1. If category dialog open -> close dialog
    // 2. If app action sheet open -> close sheet
    // 3. If settings open -> close settings
    // 4. If searching -> clear search query
    // 5. If in a specific category (e.g. Game) -> return to "all" (Semua) category & reset chip to index 0
    // 6. If scrolled down in "all" category -> smoothly scroll back to top
    // 7. If already at top and in "all" -> lock chip to 0 and consume event so it never leaks focus
    BackHandler(enabled = true) {
        try {
            when {
                appForCategoryChange != null -> {
                    viewModel.openCategoryChangeDialog(null)
                }
                categoryToDelete != null -> {
                    viewModel.openDeleteCategoryDialog(null)
                }
                isCreateCategoryOpen -> {
                    viewModel.openCreateCategoryDialog(false)
                }
                uiState.selectedApp != null -> {
                    viewModel.selectAppForAction(null)
                }
                uiState.isHiddenAppsSheetOpen -> {
                    viewModel.openHiddenAppsSheet(false)
                }
                uiState.isSettingsOpen -> {
                    viewModel.openSettings(false)
                }
                uiState.isSearching -> {
                    viewModel.setSearchActive(false)
                }
                uiState.selectedCategoryId != "all" -> {
                    viewModel.selectCategory("all")
                    scope.launch {
                        try {
                            chipListState.scrollToItem(0)
                            listState.scrollToItem(0)
                        } catch (_: Throwable) {}
                    }
                }
                else -> {
                    scope.launch {
                        try {
                            chipListState.scrollToItem(0)
                            if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
                                listState.animateScrollToItem(0)
                            }
                        } catch (_: Throwable) {}
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (uiState.pureTransparentBg) {
                    Modifier.background(Color.Transparent)
                } else {
                    Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x99000000),
                                Color(0xB3000000),
                                Color(0xCC000000)
                            )
                        )
                    )
                }
            )
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. Digital Clock Header (with Theme & Banner controls)
            AnimatedVisibility(
                visible = !uiState.isSearching,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                DigitalClockHeader(
                    is24Hour = uiState.is24Hour,
                    showBanner = uiState.showMaoneArtBanner,
                    widgetTheme = uiState.widgetTheme,
                    onOpenSettings = { viewModel.openSettings(true) }
                )
            }

            // 2. SEARCH BAR (Normal horizontal or hidden when searchAsIcon is true and not searching)
            AnimatedVisibility(
                visible = !uiState.searchAsIcon || uiState.isSearching,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                FlowSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    autoFocus = uiState.isSearching,
                    onCloseClick = if (uiState.searchAsIcon) { { viewModel.setSearchActive(false) } } else null,
                    onSearchAction = {
                        if (uiState.displayApps.isNotEmpty()) {
                            viewModel.launchApp(uiState.displayApps.first())
                        }
                    },
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // 3. Notification Access Setup Banner (auto-dismissed once granted)
            AnimatedVisibility(
                visible = !uiState.hasNotificationAccess && !uiState.isSearching,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1FFF9800))
                        .border(1.dp, Color(0x66FF9800), RoundedCornerShape(12.dp))
                        .clickable { viewModel.openNotificationSettings() }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔔",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aktifkan Akses Notifikasi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB74D)
                            )
                            Text(
                                text = "Sentuh untuk izinkan lencana pesan WhatsApp & aplikasi",
                                fontSize = 11.sp,
                                color = FlowTextSecondary
                            )
                        }
                    }
                }
            }

            // 4. Category Chip Bar (Customizable & Manageable - can be hidden via settings)
            if (uiState.showCategories) {
                AnimatedVisibility(
                    visible = !uiState.isSearching,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    CategoryChipBar(
                        categories = uiState.categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onCategorySelected = viewModel::selectCategory,
                        onAddCategoryClick = { viewModel.openCreateCategoryDialog(true) },
                        onCategoryLongClick = { cat -> viewModel.openDeleteCategoryDialog(cat) },
                        hapticsEnabled = uiState.hapticsEnabled,
                        chipListState = chipListState,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // Safe scroll handler with bounds checking & cancellation protection to prevent FC
            val safeScrollToLetter: (Char) -> Unit = { letter ->
                uiState.letterIndices[letter]?.let { appIndex ->
                    val headerOffset = if (uiState.selectedCategoryId == "all" && uiState.favorites.isNotEmpty()) {
                        uiState.favorites.size + 3
                    } else 1
                    val targetIndex = (headerOffset + appIndex).coerceAtLeast(0)
                    scrollJob?.cancel()
                    scrollJob = scope.launch {
                        try {
                            listState.scrollToItem(targetIndex)
                        } catch (_: Throwable) {}
                    }
                }
            }

            // 5. Main Content Row: Apps List + Alphabet Wave Scroller (Protected by navigationBarsPadding)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {

                // Left-Handed Alphabet Wave Scroller (if configured & enabled)
                if (uiState.showAlphabetScroller && uiState.alphabetSide == "left" && !uiState.isSearching) {
                    AlphabetWaveScroller(
                        availableLetters = uiState.availableLetters,
                        onLetterSelected = safeScrollToLetter,
                        isRightSide = false,
                        hapticsEnabled = uiState.hapticsEnabled
                    )
                }

                // Vertical Apps List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(bottom = if (uiState.searchAsIcon) 80.dp else 16.dp)
                ) {
                    if (!uiState.isSearching) {
                        // In "All" category, show Favorites section first
                        if (uiState.selectedCategoryId == "all") {
                            if (uiState.favorites.isNotEmpty()) {
                                item(key = "favorites_title") {
                                    Text(
                                        text = "FAVORIT",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FlowElectricBlue,
                                        letterSpacing = 1.2.sp,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                                    )
                                }

                                itemsIndexed(
                                    items = uiState.favorites,
                                    key = { index, app -> "fav_${app.id}_$index" }
                                ) { _, app ->
                                    AppListItem(
                                        app = app,
                                        iconDrawable = viewModel.getAppIcon(app.packageName),
                                        showIcon = uiState.showIcons,
                                        twoLineAppNames = uiState.twoLineAppNames,
                                        onClick = { viewModel.launchApp(app) },
                                        onLongClick = {
                                            if (uiState.hapticsEnabled) haptics.heavyClick(view)
                                            viewModel.selectAppForAction(app)
                                        }
                                    )
                                }

                                item(key = "favorites_divider") {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                            // All Apps Title
                            item(key = "all_apps_title") {
                                Text(
                                    text = "SEMUA APLIKASI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FlowTextSecondary,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                                )
                            }
                        } else {
                            // Category Specific Section Title
                            val currentCat = uiState.categories.find { it.id == uiState.selectedCategoryId }
                            val catTitle = currentCat?.title ?: "KATEGORI"
                            val catIcon = currentCat?.icon ?: "📁"

                            item(key = "cat_header_${uiState.selectedCategoryId}") {
                                Text(
                                    text = "$catIcon ${catTitle.uppercase()} (${uiState.displayApps.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FlowElectricBlue,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Display Apps
                    if (uiState.displayApps.isEmpty()) {
                        item(key = "empty_state") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (uiState.isSearching) "Aplikasi tidak ditemukan" else "Tidak ada aplikasi di kategori ini",
                                    fontSize = 15.sp,
                                    color = FlowTextSecondary
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = uiState.displayApps,
                            key = { index, app -> "${app.id}_$index" }
                        ) { _, app ->
                            AppListItem(
                                app = app,
                                iconDrawable = viewModel.getAppIcon(app.packageName),
                                showIcon = uiState.showIcons,
                                twoLineAppNames = uiState.twoLineAppNames,
                                onClick = { viewModel.launchApp(app) },
                                onLongClick = {
                                    if (uiState.hapticsEnabled) haptics.heavyClick(view)
                                    viewModel.selectAppForAction(app)
                                }
                            )
                        }
                    }
                }

                // Right-Handed Alphabet Wave Scroller (Default - can be hidden via settings)
                if (uiState.showAlphabetScroller && uiState.alphabetSide == "right" && !uiState.isSearching) {
                    AlphabetWaveScroller(
                        availableLetters = uiState.availableLetters,
                        onLetterSelected = safeScrollToLetter,
                        isRightSide = true,
                        hapticsEnabled = uiState.hapticsEnabled
                    )
                }
            }
        }

        // Floating Google/Search Button (Active when searchAsIcon is true and not searching, comfortably above system navbar)
        if (uiState.searchAsIcon && !uiState.isSearching) {
            val searchButtonEndPadding = if (uiState.showAlphabetScroller && uiState.alphabetSide == "right") 64.dp else 20.dp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = searchButtonEndPadding, bottom = 48.dp)
                    .size(52.dp)
                    .shadow(14.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xEE1A1B20))
                    .border(1.2.dp, Color(0x3300E5FF), CircleShape)
                    .combinedClickable(
                        onClick = {
                            if (uiState.hapticsEnabled) haptics.click(view)
                            viewModel.setSearchActive(true)
                        },
                        onLongClick = {
                            if (uiState.hapticsEnabled) haptics.heavyClick(view)
                            viewModel.onSearchButtonLongPress()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        // App Action Bottom Sheet
        uiState.selectedApp?.let { app ->
            AppActionBottomSheet(
                app = app,
                iconDrawable = viewModel.getAppIcon(app.packageName),
                onDismiss = { viewModel.selectAppForAction(null) },
                onLaunch = { viewModel.launchApp(app) },
                onOpenSettings = {
                    viewModel.selectAppForAction(null)
                    viewModel.openSettings(true)
                },
                onToggleFavorite = { viewModel.toggleFavorite(app) },
                onChangeCategory = { viewModel.openCategoryChangeDialog(app) },
                onOpenInfo = { viewModel.openAppDetails(app) },
                onUninstall = { viewModel.uninstallApp(app) },
                onToggleHidden = { viewModel.toggleHidden(app) }
            )
        }

        // Launcher Settings Bottom Sheet (Opened when clicking MaoneArt Flow app or header)
        if (uiState.isSettingsOpen) {
            LauncherSettingsBottomSheet(
                showBanner = uiState.showMaoneArtBanner,
                onToggleShowBanner = viewModel::setShowMaoneArtBanner,
                searchAsIcon = uiState.searchAsIcon,
                onToggleSearchAsIcon = viewModel::setSearchAsIcon,
                searchLongPressAction = uiState.searchLongPressAction,
                onSelectSearchLongPressAction = viewModel::setSearchLongPressAction,
                widgetTheme = uiState.widgetTheme,
                onSelectWidgetTheme = viewModel::setWidgetTheme,
                showAlphabetScroller = uiState.showAlphabetScroller,
                onToggleShowAlphabetScroller = viewModel::setShowAlphabetScroller,
                pureTransparentBg = uiState.pureTransparentBg,
                onTogglePureTransparentBg = viewModel::setPureTransparentBg,
                showCategories = uiState.showCategories,
                onToggleShowCategories = viewModel::setShowCategories,
                twoLineAppNames = uiState.twoLineAppNames,
                onToggleTwoLineAppNames = viewModel::setTwoLineAppNames,
                showIcons = uiState.showIcons,
                onToggleShowIcons = viewModel::setShowIcons,
                is24Hour = uiState.is24Hour,
                onToggleIs24Hour = viewModel::setIs24Hour,
                hapticsEnabled = uiState.hapticsEnabled,
                onToggleHaptics = { enabled ->
                    if (enabled) haptics.click(view)
                    viewModel.setHapticsEnabled(enabled)
                },
                hasNotificationAccess = uiState.hasNotificationAccess,
                onOpenNotificationSettings = viewModel::openNotificationSettings,
                hiddenAppsCount = uiState.hiddenApps.size,
                onOpenHiddenApps = { viewModel.openHiddenAppsSheet(true) },
                onDismiss = { viewModel.openSettings(false) }
            )
        }

        // Hidden Apps Management Bottom Sheet
        if (uiState.isHiddenAppsSheetOpen) {
            HiddenAppsBottomSheet(
                hiddenApps = uiState.hiddenApps,
                hasHiddenCategories = uiState.hasHiddenCategories,
                getAppIcon = viewModel::getAppIcon,
                onUnhideApp = viewModel::unhideApp,
                onRestoreCategories = viewModel::restoreDefaultCategories,
                onDismiss = { viewModel.openHiddenAppsSheet(false) }
            )
        }

        // Select / Change Category Bottom Sheet
        appForCategoryChange?.let { app ->
            SelectCategoryBottomSheet(
                app = app,
                availableCategories = uiState.categories,
                onCategorySelected = { catId -> viewModel.assignAppCategory(app, catId) },
                onCreateNewCategory = { viewModel.openCreateCategoryDialog(true) },
                onResetToDefault = { viewModel.resetAppCategory(app) },
                onDismiss = { viewModel.openCategoryChangeDialog(null) }
            )
        }

        // Create Custom Category Dialog
        if (isCreateCategoryOpen) {
            CreateCategoryDialog(
                onDismiss = { viewModel.openCreateCategoryDialog(false) },
                onConfirm = { name -> viewModel.createCustomCategory(name) }
            )
        }

        // Delete / Hide Category Confirmation Dialog
        categoryToDelete?.let { category ->
            DeleteCategoryDialog(
                category = category,
                onDismiss = { viewModel.openDeleteCategoryDialog(null) },
                onConfirm = { viewModel.deleteCategory(category) }
            )
        }

        // AI Assistant Selection Bottom Sheet
        if (uiState.isAiAssistantSheetOpen) {
            AiAssistantBottomSheet(
                isGeminiInstalled = uiState.isGeminiInstalled,
                isChatGptInstalled = uiState.isChatGptInstalled,
                onSelectGemini = { rememberChoice ->
                    if (uiState.hapticsEnabled) haptics.click(view)
                    viewModel.launchGemini(rememberChoice)
                },
                onSelectChatGpt = { rememberChoice ->
                    if (uiState.hapticsEnabled) haptics.click(view)
                    viewModel.launchChatGpt(rememberChoice)
                },
                onSelectAssistant = { rememberChoice ->
                    if (uiState.hapticsEnabled) haptics.click(view)
                    viewModel.launchVoiceAssistant(rememberChoice)
                },
                onDismiss = { viewModel.openAiAssistantSheet(false) }
            )
        }
    }
}
