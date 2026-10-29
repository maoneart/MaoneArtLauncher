package id.maoneart.flow.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.maoneart.flow.ui.components.AlphabetWaveScroller
import id.maoneart.flow.ui.components.AppActionBottomSheet
import id.maoneart.flow.ui.components.AppListItem
import id.maoneart.flow.ui.components.DigitalClockHeader
import id.maoneart.flow.ui.components.FlowSearchBar
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowTextSecondary
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            // Subtle dark readability gradient over user wallpaper
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x99000000),
                        Color(0xB3000000),
                        Color(0xCC000000)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. Digital Clock (collapses smoothly when user starts typing search query)
            AnimatedVisibility(
                visible = !uiState.isSearching,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                DigitalClockHeader(is24Hour = uiState.is24Hour)
            }

            // 2. PERMANENT STABLE SEARCH BAR (Never disposed or recreated on typing!)
            FlowSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onSearchAction = {
                    if (uiState.displayApps.isNotEmpty()) {
                        viewModel.launchApp(uiState.displayApps.first())
                    }
                },
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // 3. Main Content Row: Apps List + Alphabet Wave Scroller
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                // Left-Handed Alphabet Wave Scroller (if configured)
                if (uiState.alphabetSide == "left" && !uiState.isSearching) {
                    AlphabetWaveScroller(
                        availableLetters = uiState.availableLetters,
                        onLetterSelected = { letter ->
                            uiState.letterIndices[letter]?.let { appIndex ->
                                val headerOffset = if (uiState.favorites.isNotEmpty()) uiState.favorites.size + 3 else 1
                                scope.launch {
                                    listState.scrollToItem(headerOffset + appIndex)
                                }
                            }
                        },
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
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    if (!uiState.isSearching) {
                        // Favorites Section
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
                                key = { _, app -> "fav_${app.id}" }
                            ) { _, app ->
                                AppListItem(
                                    app = app,
                                    iconDrawable = viewModel.getAppIcon(app.packageName),
                                    showIcon = uiState.showIcons,
                                    onClick = { viewModel.launchApp(app) },
                                    onLongClick = { viewModel.selectAppForAction(app) }
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
                    }

                    // Display Apps (Filtered Search Results or Complete Alphabetical List)
                    if (uiState.displayApps.isEmpty() && uiState.isSearching) {
                        item(key = "empty_state") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aplikasi tidak ditemukan",
                                    fontSize = 16.sp,
                                    color = FlowTextSecondary
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = uiState.displayApps,
                            key = { _, app -> app.id }
                        ) { _, app ->
                            AppListItem(
                                app = app,
                                iconDrawable = viewModel.getAppIcon(app.packageName),
                                showIcon = uiState.showIcons,
                                onClick = { viewModel.launchApp(app) },
                                onLongClick = { viewModel.selectAppForAction(app) }
                            )
                        }
                    }
                }

                // Right-Handed Alphabet Wave Scroller (Default)
                if (uiState.alphabetSide == "right" && !uiState.isSearching) {
                    AlphabetWaveScroller(
                        availableLetters = uiState.availableLetters,
                        onLetterSelected = { letter ->
                            uiState.letterIndices[letter]?.let { appIndex ->
                                val headerOffset = if (uiState.favorites.isNotEmpty()) uiState.favorites.size + 3 else 1
                                scope.launch {
                                    listState.scrollToItem(headerOffset + appIndex)
                                }
                            }
                        },
                        isRightSide = true,
                        hapticsEnabled = uiState.hapticsEnabled
                    )
                }
            }
        }

        // App Action Bottom Sheet
        uiState.selectedApp?.let { app ->
            AppActionBottomSheet(
                app = app,
                iconDrawable = viewModel.getAppIcon(app.packageName),
                onDismiss = { viewModel.selectAppForAction(null) },
                onLaunch = { viewModel.launchApp(app) },
                onToggleFavorite = { viewModel.toggleFavorite(app) },
                onOpenInfo = { viewModel.openAppDetails(app) },
                onUninstall = { viewModel.uninstallApp(app) },
                onToggleHidden = { viewModel.toggleHidden(app) }
            )
        }
    }
}
