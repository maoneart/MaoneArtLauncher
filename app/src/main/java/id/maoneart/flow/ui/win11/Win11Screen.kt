package id.maoneart.flow.ui.win11

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.ui.components.AppActionBottomSheet
import id.maoneart.flow.ui.components.HiddenAppsBottomSheet
import id.maoneart.flow.ui.components.LauncherSettingsBottomSheet
import id.maoneart.flow.ui.home.HomeViewModel

/**
 * Windows 11 Mode Master Screen
 * Provides 100% authentic Windows 11 experience with Desktop,
 * Centered Taskbar, Start Menu, Action Center, and Context Menu.
 * Supports both Portrait (Win11 Mobile) and Landscape (Win11 PC/Tablet).
 */
@Composable
fun Win11Screen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val density = LocalDensity.current

    // Win11 Flyout States
    var isStartMenuOpen by remember { mutableStateOf(false) }
    var isActionCenterOpen by remember { mutableStateOf(false) }
    var isCalendarNotificationOpen by remember { mutableStateOf(false) }
    var isContextMenuOpen by remember { mutableStateOf(false) }
    var isAboutDialogOpen by remember { mutableStateOf(false) }
    var autoFocusSearch by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(Offset.Zero) }

    // Close flyouts helper
    fun dismissAllFlyouts() {
        isStartMenuOpen = false
        isActionCenterOpen = false
        isCalendarNotificationOpen = false
        isContextMenuOpen = false
    }

    // Android Hardware / Gesture Back Button Handler
    BackHandler(enabled = isAboutDialogOpen || isStartMenuOpen || isActionCenterOpen || isCalendarNotificationOpen || isContextMenuOpen || uiState.isSettingsOpen || uiState.selectedApp != null) {
        when {
            isAboutDialogOpen -> isAboutDialogOpen = false
            isContextMenuOpen -> isContextMenuOpen = false
            isCalendarNotificationOpen -> isCalendarNotificationOpen = false
            isActionCenterOpen -> isActionCenterOpen = false
            isStartMenuOpen -> isStartMenuOpen = false
            uiState.selectedApp != null -> viewModel.selectAppForAction(null)
            uiState.isSettingsOpen -> viewModel.openSettings(false)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        // --- LAYER 0: WIN 11 BLOOM WALLPAPER ---
        Win11BloomWallpaper()

        // --- LAYER 1: WIN 11 DESKTOP SURFACE ---
        Win11Desktop(
            isLandscape = isLandscape,
            desktopApps = uiState.favorites.ifEmpty { uiState.allApps.take(16) },
            getAppIcon = viewModel::getAppIcon,
            onAppClick = { app ->
                dismissAllFlyouts()
                viewModel.launchApp(app)
            },
            onAppLongClick = { app ->
                dismissAllFlyouts()
                viewModel.selectAppForAction(app)
            },
            onThisPcClick = {
                dismissAllFlyouts()
                try {
                    context.startActivity(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                } catch (_: Throwable) {
                    try {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    } catch (_: Throwable) {}
                }
            },
            onRecycleBinClick = {
                dismissAllFlyouts()
                viewModel.openHiddenAppsSheet(true)
            },
            onEdgeClick = {
                dismissAllFlyouts()
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(browserIntent)
                } catch (_: Throwable) {}
            },
            onBackgroundClick = {
                dismissAllFlyouts()
            },
            onBackgroundLongClick = { offset ->
                dismissAllFlyouts()
                contextMenuOffset = offset
                isContextMenuOpen = true
            }
        )

        // --- LAYER 2: WIN 11 DOCKED TASKBAR ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            Win11Taskbar(
                isLandscape = isLandscape,
                isStartMenuOpen = isStartMenuOpen,
                isActionCenterOpen = isActionCenterOpen,
                isCalendarOpen = isCalendarNotificationOpen,
                pinnedApps = uiState.favorites,
                getAppIcon = viewModel::getAppIcon,
                onStartClick = {
                    isActionCenterOpen = false
                    isCalendarNotificationOpen = false
                    isContextMenuOpen = false
                    autoFocusSearch = false
                    isStartMenuOpen = !isStartMenuOpen
                },
                onSearchClick = {
                    isActionCenterOpen = false
                    isCalendarNotificationOpen = false
                    isContextMenuOpen = false
                    autoFocusSearch = true
                    isStartMenuOpen = true
                },
                onActionCenterClick = {
                    isStartMenuOpen = false
                    isCalendarNotificationOpen = false
                    isContextMenuOpen = false
                    isActionCenterOpen = !isActionCenterOpen
                },
                onClockClick = {
                    isStartMenuOpen = false
                    isActionCenterOpen = false
                    isContextMenuOpen = false
                    isCalendarNotificationOpen = !isCalendarNotificationOpen
                },
                onAppClick = { app ->
                    dismissAllFlyouts()
                    viewModel.launchApp(app)
                },
                onShowDesktopClick = {
                    dismissAllFlyouts()
                },
                onExplorerClick = {
                    dismissAllFlyouts()
                    try {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "*/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Throwable) {}
                },
                onBrowserClick = {
                    dismissAllFlyouts()
                    try {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bing.com"))
                        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(browserIntent)
                    } catch (_: Throwable) {}
                }
            )
        }

        // --- LAYER 3A: FLOATING WIN 11 START MENU ---
        AnimatedVisibility(
            visible = isStartMenuOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 56.dp)
        ) {
            Win11StartMenu(
                isLandscape = isLandscape,
                allApps = uiState.allApps,
                pinnedApps = uiState.favorites,
                getAppIcon = viewModel::getAppIcon,
                searchQuery = uiState.searchQuery,
                autoFocusSearch = autoFocusSearch,
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                onAppClick = { app ->
                    dismissAllFlyouts()
                    viewModel.launchApp(app)
                },
                onAppLongClick = { app ->
                    dismissAllFlyouts()
                    viewModel.selectAppForAction(app)
                },
                onOpenSettings = {
                    dismissAllFlyouts()
                    viewModel.openSettings(true)
                },
                onSwitchToFlowMode = {
                    dismissAllFlyouts()
                    viewModel.setLauncherMode("flow")
                },
                onDismiss = { isStartMenuOpen = false }
            )
        }

        // --- LAYER 3B: FLOATING WIN 11 ACTION CENTER (QUICK SETTINGS) ---
        AnimatedVisibility(
            visible = isActionCenterOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 }),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 54.dp, end = 10.dp)
        ) {
            Win11ActionCenter(
                isLandscape = isLandscape,
                onOpenLauncherSettings = {
                    dismissAllFlyouts()
                    viewModel.openSettings(true)
                }
            )
        }

        // --- LAYER 3C: FLOATING WIN 11 NOTIFICATION & CALENDAR CENTER ---
        AnimatedVisibility(
            visible = isCalendarNotificationOpen,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 }),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 54.dp, end = 10.dp)
        ) {
            Win11NotificationCalendar(
                isLandscape = isLandscape,
                getAppIcon = viewModel::getAppIcon,
                onOpenNotificationSettings = {
                    dismissAllFlyouts()
                    viewModel.openNotificationSettings()
                },
                onLaunchApp = { pkg ->
                    dismissAllFlyouts()
                    val app = uiState.allApps.firstOrNull { it.packageName == pkg }
                    if (app != null) {
                        viewModel.launchApp(app)
                    } else {
                        try {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            }
                        } catch (_: Throwable) {}
                    }
                }
            )
        }

        // --- LAYER 3D: FLOATING WIN 11 CONTEXT MENU ---
        if (isContextMenuOpen) {
            val offsetX = with(density) { contextMenuOffset.x.toDp() }.coerceIn(12.dp, (configuration.screenWidthDp - 232).dp)
            val offsetY = with(density) { contextMenuOffset.y.toDp() }.coerceIn(40.dp, (configuration.screenHeightDp - 220).dp)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isContextMenuOpen = false
                    }
            ) {
                Win11ContextMenu(
                    onRefresh = {
                        // Refresh apps cache
                    },
                    onPersonalize = {
                        isContextMenuOpen = false
                        viewModel.openSettings(true)
                    },
                    onSwitchToFlow = {
                        isContextMenuOpen = false
                        viewModel.setLauncherMode("flow")
                    },
                    onAboutClick = {
                        isContextMenuOpen = false
                        isAboutDialogOpen = true
                    },
                    onDismiss = { isContextMenuOpen = false },
                    modifier = Modifier.offset(x = offsetX, y = offsetY)
                )
            }
        }

        // --- LAYER 3E: WIN 11 ABOUT DIALOG (WINVER) ---
        if (isAboutDialogOpen) {
            Win11AboutDialog(
                onDismiss = { isAboutDialogOpen = false }
            )
        }

        // --- LAYER 4: BOTTOM SHEETS (SETTINGS, ACTIONS, HIDDEN APPS) ---
        if (uiState.isSettingsOpen) {
            LauncherSettingsBottomSheet(
                launcherMode = uiState.launcherMode,
                onSelectLauncherMode = { viewModel.setLauncherMode(it) },
                showBanner = uiState.showMaoneArtBanner,
                onToggleShowBanner = { viewModel.setShowMaoneArtBanner(it) },
                searchAsIcon = uiState.searchAsIcon,
                onToggleSearchAsIcon = { viewModel.setSearchAsIcon(it) },
                searchLongPressAction = uiState.searchLongPressAction,
                onSelectSearchLongPressAction = { viewModel.setSearchLongPressAction(it) },
                widgetTheme = uiState.widgetTheme,
                onSelectWidgetTheme = { viewModel.setWidgetTheme(it) },
                showAlphabetScroller = uiState.showAlphabetScroller,
                onToggleShowAlphabetScroller = { viewModel.setShowAlphabetScroller(it) },
                pureTransparentBg = uiState.pureTransparentBg,
                onTogglePureTransparentBg = { viewModel.setPureTransparentBg(it) },
                showCategories = uiState.showCategories,
                onToggleShowCategories = { viewModel.setShowCategories(it) },
                twoLineAppNames = uiState.twoLineAppNames,
                onToggleTwoLineAppNames = { viewModel.setTwoLineAppNames(it) },
                showIcons = uiState.showIcons,
                onToggleShowIcons = { viewModel.setShowIcons(it) },
                is24Hour = uiState.is24Hour,
                onToggleIs24Hour = { viewModel.setIs24Hour(it) },
                hapticsEnabled = uiState.hapticsEnabled,
                onToggleHaptics = { viewModel.setHapticsEnabled(it) },
                hasNotificationAccess = uiState.hasNotificationAccess,
                onOpenNotificationSettings = { viewModel.openNotificationSettings() },
                hiddenAppsCount = uiState.hiddenApps.size,
                onOpenHiddenApps = {
                    viewModel.openSettings(false)
                    viewModel.openHiddenAppsSheet(true)
                },
                onDismiss = { viewModel.openSettings(false) }
            )
        }

        // App Action Bottom Sheet (Uninstall, hide, details)
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

        // Hidden Apps Management Sheet
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
    }
}
