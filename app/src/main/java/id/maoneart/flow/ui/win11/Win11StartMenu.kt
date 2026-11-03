package id.maoneart.flow.ui.win11

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.ui.util.rememberFlowHaptics

/**
 * Authentic Windows 11 Start Menu with Mica Acrylic design,
 * Search Bar, Pinned Apps Grid, Recommended section, All Apps drawer,
 * and User/Power Footer with 1-tap switch to MaoneArt Flow mode.
 */
@Composable
fun Win11StartMenu(
    isLandscape: Boolean,
    allApps: List<AppInfo>,
    pinnedApps: List<AppInfo>,
    getAppIcon: (String) -> Drawable?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onOpenSettings: () -> Unit,
    onSwitchToFlowMode: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberFlowHaptics()
    var isAllAppsView by remember { mutableStateOf(false) }
    var isPowerMenuOpen by remember { mutableStateOf(false) }

    // Width and height adapt based on orientation
    val cardModifier = if (isLandscape) {
        Modifier
            .width(480.dp)
            .height(490.dp)
    } else {
        Modifier
            .fillMaxWidth(0.95f)
            .height(540.dp)
    }

    Box(
        modifier = modifier
            .then(cardModifier)
            .shadow(24.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xF21C222E)) // Win11 Dark Acrylic Mica
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
            .clickable(enabled = false) {} // Prevent click-through to desktop
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- 1. WIN 11 SEARCH BAR ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(Color(0x33000000))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(19.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFA0AEC0),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Ketik di sini untuk mencari...",
                                color = Color(0xFF718096),
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color(0xFFF1F5F9),
                            unfocusedTextColor = Color(0xFFF1F5F9),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFFA0AEC0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // --- 2. MAIN BODY (SEARCH RESULTS / PINNED / ALL APPS) ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                if (searchQuery.isNotBlank()) {
                    // SEARCH RESULTS VIEW
                    val filteredApps = allApps.filter {
                        it.label.contains(searchQuery.trim(), ignoreCase = true) ||
                                it.packageName.contains(searchQuery.trim(), ignoreCase = true)
                    }

                    if (filteredApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada hasil untuk \"$searchQuery\"",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredApps, key = { it.id }) { app ->
                                StartMenuAppRow(
                                    app = app,
                                    getAppIcon = getAppIcon,
                                    onClick = {
                                        haptics.letterTick()
                                        onAppClick(app)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // SWITCH BETWEEN PINNED AND ALL APPS
                    AnimatedContent(
                        targetState = isAllAppsView,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "StartMenuContent"
                    ) { showAllApps ->
                        if (showAllApps) {
                            // --- ALL APPS VIEW ---
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                haptics.letterTick()
                                                isAllAppsView = false
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color(0xFF60A5FA),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Disematkan",
                                            color = Color(0xFF60A5FA),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    Text(
                                        text = "Semua Aplikasi",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                val groupedApps = allApps.groupBy { it.firstLetter }
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    groupedApps.toSortedMap().forEach { (letter, appsInLetter) ->
                                        item(key = "header_$letter") {
                                            Text(
                                                text = letter.toString(),
                                                color = Color(0xFF60A5FA),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
                                            )
                                        }
                                        items(appsInLetter, key = { it.id }) { app ->
                                            StartMenuAppRow(
                                                app = app,
                                                getAppIcon = getAppIcon,
                                                onClick = {
                                                    haptics.letterTick()
                                                    onAppClick(app)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // --- PINNED & RECOMMENDED VIEW ---
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Pinned Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Disematkan",
                                        color = Color(0xFFF1F5F9),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable {
                                                haptics.letterTick()
                                                isAllAppsView = true
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Semua aplikasi",
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 12.sp
                                        )
                                        Icon(
                                            imageVector = Icons.Rounded.ChevronRight,
                                            contentDescription = "All Apps",
                                            tint = Color(0xFFCBD5E1),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Pinned Grid: 3 rows x 6 cols (or 4 cols in portrait)
                                val columns = if (isLandscape) 6 else 4
                                val pinnedToShow = pinnedApps.ifEmpty { allApps.take(12) }.take(if (isLandscape) 12 else 8)

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(columns),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isLandscape) 180.dp else 190.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(pinnedToShow, key = { it.id }) { app ->
                                        StartMenuGridItem(
                                            app = app,
                                            getAppIcon = getAppIcon,
                                            onClick = {
                                                haptics.letterTick()
                                                onAppClick(app)
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Recommended Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Disarankan",
                                        color = Color(0xFFF1F5F9),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Recommended List (Recent 4 apps)
                                val recommendedApps = allApps.shuffled().take(if (isLandscape) 4 else 3)
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    recommendedApps.forEachIndexed { idx, app ->
                                        val subtitle = when (idx) {
                                            0 -> "Baru saja digunakan"
                                            1 -> "Kemarin"
                                            2 -> "Sering dibuka"
                                            else -> "Baru ditambahkan"
                                        }
                                        RecommendedAppRow(
                                            app = app,
                                            getAppIcon = getAppIcon,
                                            subtitle = subtitle,
                                            onClick = {
                                                haptics.letterTick()
                                                onAppClick(app)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. FOOTER BAR (USER AVATAR & POWER BUTTON) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color(0x33000000))
                    .padding(horizontal = 24.dp)
            ) {
                // User Profile (Left)
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            haptics.letterTick()
                            onOpenSettings()
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0078D7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "MaoneArt",
                        color = Color(0xFFF1F5F9),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Power Button (Right)
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    IconButton(
                        onClick = {
                            haptics.letterTick()
                            isPowerMenuOpen = !isPowerMenuOpen
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PowerSettingsNew,
                            contentDescription = "Power",
                            tint = Color(0xFFE2E8F0),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isPowerMenuOpen,
                        onDismissRequest = { isPowerMenuOpen = false },
                        modifier = Modifier
                            .background(Color(0xF21C222E))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "🔄 Beralih ke MaoneArt Flow (Niagara)",
                                    color = Color(0xFF60A5FA),
                                    fontSize = 13.sp
                                )
                            },
                            onClick = {
                                isPowerMenuOpen = false
                                onSwitchToFlowMode()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "⚙️ Pengaturan Launcher",
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 13.sp
                                )
                            },
                            onClick = {
                                isPowerMenuOpen = false
                                onOpenSettings()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Grid Item for Pinned Apps inside Windows 11 Start Menu
 */
@Composable
private fun StartMenuGridItem(
    app: AppInfo,
    getAppIcon: (String) -> Drawable?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = getAppIcon(app.packageName),
            contentDescription = app.label,
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = app.label,
            color = Color(0xFFF1F5F9),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Row Item for Recommended Section inside Start Menu
 */
@Composable
private fun RecommendedAppRow(
    app: AppInfo,
    getAppIcon: (String) -> Drawable?,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = getAppIcon(app.packageName),
            contentDescription = app.label,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                color = Color(0xFFF1F5F9),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Full Row Item for All Apps list and Search Results
 */
@Composable
private fun StartMenuAppRow(
    app: AppInfo,
    getAppIcon: (String) -> Drawable?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = getAppIcon(app.packageName),
            contentDescription = app.label,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = app.label,
            color = Color(0xFFF1F5F9),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
