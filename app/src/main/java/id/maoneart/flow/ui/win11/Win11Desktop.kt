package id.maoneart.flow.ui.win11

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.ui.util.rememberFlowHaptics

/**
 * Windows 11 Desktop surface featuring iconic Desktop shortcuts:
 * - This PC, Recycle Bin, Microsoft Edge
 * - User Favorite / Pinned shortcuts
 * - Tap to dismiss flyouts & Long-press to trigger Win11 Context Menu
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Win11Desktop(
    isLandscape: Boolean,
    desktopApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onRecycleBinClick: () -> Unit,
    onThisPcClick: () -> Unit,
    onEdgeClick: () -> Unit,
    onBackgroundClick: () -> Unit,
    onBackgroundLongClick: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberFlowHaptics()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onBackgroundClick()
                    },
                    onLongPress = { offset ->
                        haptics.heavyClick()
                        onBackgroundLongClick(offset)
                    }
                )
            }
            .statusBarsPadding()
    ) {
        // Grid columns: 4 columns in portrait, 8 columns in landscape
        val columns = if (isLandscape) 8 else 4

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentPadding = PaddingValues(bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. THIS PC ICON
            item(key = "win11_desktop_this_pc") {
                DesktopShortcutItem(
                    title = "This PC",
                    icon = { Win11Icons.ThisPcIcon(size = 40.dp) },
                    onClick = {
                        haptics.letterTick()
                        onThisPcClick()
                    },
                    onLongClick = {}
                )
            }

            // 2. RECYCLE BIN ICON
            item(key = "win11_desktop_recycle_bin") {
                DesktopShortcutItem(
                    title = "Recycle Bin",
                    icon = { Win11Icons.RecycleBinIcon(size = 40.dp) },
                    onClick = {
                        haptics.letterTick()
                        onRecycleBinClick()
                    },
                    onLongClick = {}
                )
            }

            // 3. MICROSOFT EDGE / BROWSER
            item(key = "win11_desktop_edge") {
                DesktopShortcutItem(
                    title = "Microsoft Edge",
                    icon = { Win11Icons.EdgeBrowserIcon(size = 40.dp) },
                    onClick = {
                        haptics.letterTick()
                        onEdgeClick()
                    },
                    onLongClick = {}
                )
            }

            // 4. USER APPS SHORTCUTS
            items(desktopApps, key = { "win11_desktop_${it.id}" }) { app ->
                DesktopShortcutItem(
                    title = app.label,
                    icon = {
                        AsyncImage(
                            model = app.iconDrawable,
                            contentDescription = app.label,
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    onClick = {
                        haptics.letterTick()
                        onAppClick(app)
                    },
                    onLongClick = {
                        haptics.heavyClick()
                        onAppLongClick(app)
                    }
                )
            }
        }
    }
}

/**
 * Individual Windows 11 Desktop Shortcut Tile
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DesktopShortcutItem(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(6.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = TextStyle(
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = Color(0xCC000000),
                    offset = Offset(1f, 1f),
                    blurRadius = 4f
                )
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
