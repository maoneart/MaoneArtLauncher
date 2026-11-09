package id.maoneart.flow.ui.win11

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.ui.util.rememberFlowHaptics
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Authentic Windows 11 Taskbar with centered icons, Mica acrylic blur,
 * System Tray (Clock, Battery, Wi-Fi, Audio), and Show Desktop peek button.
 * Adapts fluidly to both Portrait (Win11 Mobile) and Landscape (Win11 PC/Tablet).
 */
@Composable
fun Win11Taskbar(
    isLandscape: Boolean,
    isStartMenuOpen: Boolean,
    isActionCenterOpen: Boolean,
    isCalendarOpen: Boolean = false,
    isExplorerOpen: Boolean = false,
    pinnedApps: List<AppInfo>,
    getAppIcon: (String) -> Drawable?,
    onStartClick: () -> Unit,
    onSearchClick: () -> Unit,
    onActionCenterClick: () -> Unit,
    onClockClick: () -> Unit = {},
    onAppClick: (AppInfo) -> Unit,
    onShowDesktopClick: () -> Unit,
    onExplorerClick: () -> Unit,
    onBrowserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberFlowHaptics()
    val context = LocalContext.current

    // Live Clock & Date formatted ala Windows 11
    var timeText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            timeText = timeFormat.format(now)
            dateText = dateFormat.format(now)
            val nowMs = System.currentTimeMillis()
            val delayMs = 60000L - (nowMs % 60000L)
            delay(delayMs.coerceAtLeast(1000L))
        }
    }

    // Battery Monitor
    var batteryPct by remember { mutableIntStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    if (level != -1 && scale > 0) {
                        batteryPct = (level * 100) / scale
                    }
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        try {
            androidx.core.content.ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                androidx.core.content.ContextCompat.RECEIVER_EXPORTED
            )
        } catch (_: Throwable) {
            try {
                context.registerReceiver(receiver, filter)
            } catch (_: Throwable) {}
        }
        onDispose {
            try { context.unregisterReceiver(receiver) } catch (_: Throwable) {}
        }
    }

    // Taskbar layout: Separate Portrait and Landscape to strictly prevent overlap
    if (!isLandscape) {
        // --- 1. PORTRAIT: DEDICATED MOBILE ROW (100% ZERO OVERLAP) ---
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color(0xE6181C24)) // Windows 11 Dark Acrylic Mica
                .border(
                    width = 1.dp,
                    color = Color(0x22FFFFFF),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT: Windows 11 Core Navigation & App Shortcuts
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Windows Start Button
                TaskbarIconButton(
                    isSelected = isStartMenuOpen,
                    onClick = {
                        haptics.letterTick()
                        onStartClick()
                    }
                ) {
                    Win11Icons.WindowsLogo(size = 20.dp)
                }

                // Search Button
                TaskbarIconButton(
                    isSelected = false,
                    onClick = {
                        haptics.letterTick()
                        onSearchClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // File Explorer
                TaskbarIconButton(
                    isSelected = isExplorerOpen,
                    hasRunningIndicator = true,
                    onClick = {
                        haptics.letterTick()
                        onExplorerClick()
                    }
                ) {
                    Win11Icons.FileExplorerIcon(size = 22.dp)
                }

                // Browser (Edge / Chrome)
                TaskbarIconButton(
                    isSelected = false,
                    hasRunningIndicator = true,
                    onClick = {
                        haptics.letterTick()
                        onBrowserClick()
                    }
                ) {
                    Win11Icons.EdgeBrowserIcon(size = 22.dp)
                }

                // 1 Favorite App if available
                pinnedApps.firstOrNull()?.let { firstApp ->
                    TaskbarAppButton(
                        app = firstApp,
                        getAppIcon = getAppIcon,
                        onClick = {
                            haptics.letterTick()
                            onAppClick(firstApp)
                        }
                    )
                }
            }

            // RIGHT: Compact Mobile System Tray split into Quick Settings Pill & Clock Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // 1. QUICK SETTINGS PILL (Wi-Fi + Battery)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActionCenterOpen) Color(0x33FFFFFF) else Color.Transparent)
                        .clickable {
                            haptics.letterTick()
                            onActionCenterClick()
                        }
                        .padding(horizontal = 5.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = "Wi-Fi & Quick Settings",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(13.dp)
                    )

                    if (isCharging) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryChargingFull,
                            contentDescription = "Battery Charging",
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(13.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.BatteryFull,
                            contentDescription = "Battery",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Text(
                        text = "$batteryPct%",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 2. CLOCK PILL (Time -> Opens Notification & Calendar Center)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCalendarOpen) Color(0x33FFFFFF) else Color.Transparent)
                        .clickable {
                            haptics.letterTick()
                            onClockClick()
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeText.ifEmpty { "12:00" },
                        color = Color(0xFFF1F5F9),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    } else {
        // --- 2. LANDSCAPE: FULL DESKTOP TASKBAR (CENTERED ICONS & RIGHT TRAY) ---
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xE6181C24)) // Windows 11 Dark Acrylic Mica
                .border(
                    width = 1.dp,
                    color = Color(0x22FFFFFF),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                )
        ) {
            // CENTERED ICONS ROW
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // WINDOWS 11 START BUTTON
                TaskbarIconButton(
                    isSelected = isStartMenuOpen,
                    onClick = {
                        haptics.letterTick()
                        onStartClick()
                    }
                ) {
                    Win11Icons.WindowsLogo(size = 22.dp)
                }

                // SEARCH BUTTON
                TaskbarIconButton(
                    isSelected = false,
                    onClick = {
                        haptics.letterTick()
                        onSearchClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // FILE EXPLORER
                TaskbarIconButton(
                    isSelected = isExplorerOpen,
                    hasRunningIndicator = true,
                    onClick = {
                        haptics.letterTick()
                        onExplorerClick()
                    }
                ) {
                    Win11Icons.FileExplorerIcon(size = 24.dp)
                }

                // BROWSER (EDGE / CHROME)
                TaskbarIconButton(
                    isSelected = false,
                    hasRunningIndicator = true,
                    onClick = {
                        haptics.letterTick()
                        onBrowserClick()
                    }
                ) {
                    Win11Icons.EdgeBrowserIcon(size = 24.dp)
                }

                // PINNED / FAVORITE APPS (Up to 6 in landscape)
                pinnedApps.take(6).forEach { app ->
                    TaskbarAppButton(
                        app = app,
                        getAppIcon = getAppIcon,
                        onClick = {
                            haptics.letterTick()
                            onAppClick(app)
                        }
                    )
                }
            }

            // SYSTEM TRAY (BOTTOM RIGHT)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Hidden Icons Caret
                Box(
                    modifier = Modifier
                        .size(width = 20.dp, height = 36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { haptics.letterTick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Hidden Icons",
                        tint = Color(0xFFA0AEC0),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // QUICK SETTINGS GROUP PILL (Wi-Fi, Volume, Battery)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActionCenterOpen) Color(0x33FFFFFF) else Color.Transparent)
                        .clickable {
                            haptics.letterTick()
                            onActionCenterClick()
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = "Wi-Fi",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(14.dp)
                    )

                    Icon(
                        imageVector = Icons.Rounded.VolumeUp,
                        contentDescription = "Audio",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(14.dp)
                    )

                    if (isCharging) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryChargingFull,
                            contentDescription = "Battery Charging",
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.BatteryFull,
                            contentDescription = "Battery",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = "$batteryPct%",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // CLOCK & DATE (2 Stacked Lines ala Windows 11 -> Opens Notification & Calendar Center)
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCalendarOpen) Color(0x33FFFFFF) else Color.Transparent)
                        .clickable {
                            haptics.letterTick()
                            onClockClick()
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeText.ifEmpty { "12:00" },
                        color = Color(0xFFF1F5F9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                        lineHeight = 13.sp
                    )
                    Text(
                        text = dateText.ifEmpty { "01/01/2026" },
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.End,
                        lineHeight = 11.sp
                    )
                }

                // SHOW DESKTOP PEEK LINE
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(6.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptics.heavyClick()
                            onShowDesktopClick()
                        }
                        .padding(start = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(0.6f)
                            .width(1.dp)
                            .background(Color(0x33FFFFFF))
                    )
                }
            }
        }
    }
}

/**
 * Taskbar Icon Button with Windows 11 Hover / Active rounded highlight
 * and running app blue dot/pill underneath.
 */
@Composable
private fun TaskbarIconButton(
    isSelected: Boolean,
    hasRunningIndicator: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isSelected) Color(0x33FFFFFF) else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()

        if (hasRunningIndicator || isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .size(width = if (isSelected) 16.dp else 5.dp, height = 2.5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0078D7))
            )
        }
    }
}

/**
 * Taskbar App Icon Button with app icon from Coil
 */
@Composable
private fun TaskbarAppButton(
    app: AppInfo,
    getAppIcon: (String) -> Drawable?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 40.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = getAppIcon(app.packageName),
            contentDescription = app.label,
            modifier = Modifier.size(24.dp)
        )

        // Underline pill indicating it's an open/pinned app
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
                .size(width = 5.dp, height = 2.5.dp)
                .clip(CircleShape)
                .background(Color(0xFF60A5FA))
        )
    }
}
