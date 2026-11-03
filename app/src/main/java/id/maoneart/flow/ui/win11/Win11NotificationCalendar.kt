package id.maoneart.flow.ui.win11

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.maoneart.flow.service.ActiveNotification
import id.maoneart.flow.service.NotificationService
import id.maoneart.flow.ui.util.FlowHaptics
import id.maoneart.flow.ui.util.rememberFlowHaptics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Windows 11 Notification & Calendar Flyout
 * Opens when the Clock/Date in the Taskbar is tapped.
 * Responsive:
 * - Portrait: Compact width (315dp) stacked vertically (400dp height)
 * - Landscape: Compact height (270dp) side-by-side (540dp width)
 */
@Composable
fun Win11NotificationCalendar(
    isLandscape: Boolean,
    getAppIcon: (String) -> Drawable?,
    onOpenNotificationSettings: () -> Unit,
    onLaunchApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberFlowHaptics()
    val context = LocalContext.current

    val activeNotifications by NotificationService.activeNotificationList.collectAsState()
    val hasNotificationAccess = remember(context) { NotificationService.isNotificationAccessGranted(context) }

    // Calendar month offset from current month
    var monthOffset by remember { mutableIntStateOf(0) }

    val calendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
        }
    }

    val todayCalendar = remember { Calendar.getInstance() }
    val isCurrentMonth = monthOffset == 0

    val monthYearFormat = remember { SimpleDateFormat("MMM yyyy", Locale("id", "ID")) }
    val monthYearText = remember(calendar) { monthYearFormat.format(calendar.time).replaceFirstChar { it.uppercase() } }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = remember(calendar) {
        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        tempCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
    }

    val todayDate = todayCalendar.get(Calendar.DAY_OF_MONTH)

    val cardModifier = if (isLandscape) {
        Modifier
            .width(380.dp)
            .height(255.dp)
    } else {
        Modifier
            .width(195.dp)
            .height(410.dp)
    }

    Box(
        modifier = modifier
            .then(cardModifier)
            .shadow(24.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xF21C222E)) // Win11 Dark Mica
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
            .clickable(enabled = false) {}
            .padding(8.dp)
    ) {
        if (isLandscape) {
            // --- LANDSCAPE: SIDE-BY-SIDE (NOTIFICATIONS LEFT | CALENDAR RIGHT) ---
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Column: Notifications
                Column(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                ) {
                    NotificationsHeader(
                        activeCount = activeNotifications.size,
                        haptics = haptics,
                        onClearAll = { NotificationService.currentInstance?.clearAllUserNotifications() },
                        onOpenSettings = onOpenNotificationSettings
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    NotificationsContent(
                        hasNotificationAccess = hasNotificationAccess,
                        activeNotifications = activeNotifications,
                        getAppIcon = getAppIcon,
                        haptics = haptics,
                        onOpenNotificationSettings = onOpenNotificationSettings,
                        onLaunchApp = onLaunchApp,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                        .background(Color(0x22FFFFFF))
                )

                // Right Column: Monthly Calendar
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    CalendarMonthHeader(
                        monthYearText = monthYearText,
                        haptics = haptics,
                        onPrevMonth = { monthOffset-- },
                        onNextMonth = { monthOffset++ }
                    )
                    CalendarWeekdayHeader()
                    CalendarDaysGrid(
                        firstDayOfWeek = firstDayOfWeek,
                        daysInMonth = daysInMonth,
                        isCurrentMonth = isCurrentMonth,
                        todayDate = todayDate,
                        haptics = haptics,
                        gridHeight = 150.dp
                    )
                }
            }
        } else {
            // --- PORTRAIT: COMPACT STACKED COLUMN ---
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Notifications Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    NotificationsHeader(
                        activeCount = activeNotifications.size,
                        haptics = haptics,
                        onClearAll = { NotificationService.currentInstance?.clearAllUserNotifications() },
                        onOpenSettings = onOpenNotificationSettings
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    NotificationsContent(
                        hasNotificationAccess = hasNotificationAccess,
                        activeNotifications = activeNotifications,
                        getAppIcon = getAppIcon,
                        haptics = haptics,
                        onOpenNotificationSettings = onOpenNotificationSettings,
                        onLaunchApp = onLaunchApp,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0x22FFFFFF), thickness = 1.dp)
                Spacer(modifier = Modifier.height(6.dp))

                // 2. Calendar Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    CalendarMonthHeader(
                        monthYearText = monthYearText,
                        haptics = haptics,
                        onPrevMonth = { monthOffset-- },
                        onNextMonth = { monthOffset++ }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CalendarWeekdayHeader()
                    Spacer(modifier = Modifier.height(2.dp))
                    CalendarDaysGrid(
                        firstDayOfWeek = firstDayOfWeek,
                        daysInMonth = daysInMonth,
                        isCurrentMonth = isCurrentMonth,
                        todayDate = todayDate,
                        haptics = haptics,
                        gridHeight = 145.dp
                    )
                }
            }
        }
    }
}

/**
 * Notifications Header with Clear All & Settings action
 */
@Composable
private fun NotificationsHeader(
    activeCount: Int,
    haptics: FlowHaptics,
    onClearAll: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = "Notifications",
                tint = Color(0xFF60A5FA),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Notifikasi",
                color = Color(0xFFF1F5F9),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (activeCount > 0) {
                Text(
                    text = "Hapus",
                    color = Color(0xFF60A5FA),
                    fontSize = 9.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            haptics.letterTick()
                            onClearAll()
                        }
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                )
            }

            Text(
                text = "Atur",
                color = Color(0xFF94A3B8),
                fontSize = 9.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        haptics.letterTick()
                        onOpenSettings()
                    }
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            )
        }
    }
}

/**
 * Notifications Body Content (Permission Request, Empty Placeholder, or Notification List)
 */
@Composable
private fun NotificationsContent(
    hasNotificationAccess: Boolean,
    activeNotifications: List<ActiveNotification>,
    getAppIcon: (String) -> Drawable?,
    haptics: FlowHaptics,
    onOpenNotificationSettings: () -> Unit,
    onLaunchApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!hasNotificationAccess) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x33FF9800))
                .border(1.dp, Color(0x66FF9800), RoundedCornerShape(8.dp))
                .clickable {
                    haptics.letterTick()
                    onOpenNotificationSettings()
                }
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Akses Notifikasi Belum Aktif",
                        color = Color(0xFFF1F5F9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Ketuk untuk memberikan izin notifikasi",
                        color = Color(0xFFCBD5E1),
                        fontSize = 9.5.sp
                    )
                }
            }
        }
    } else if (activeNotifications.isEmpty()) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x1AFFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsNone,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tidak ada notifikasi",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(activeNotifications, key = { it.key }) { notif ->
                NotificationItemCard(
                    notif = notif,
                    getAppIcon = getAppIcon,
                    onDismiss = {
                        haptics.letterTick()
                        NotificationService.currentInstance?.dismissNotification(notif.key)
                    },
                    onClick = {
                        haptics.letterTick()
                        onLaunchApp(notif.packageName)
                    }
                )
            }
        }
    }
}

/**
 * Calendar Month Navigation Header (< Month Year >)
 */
@Composable
private fun CalendarMonthHeader(
    monthYearText: String,
    haptics: FlowHaptics,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = monthYearText,
            color = Color(0xFFF1F5F9),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            IconButton(
                onClick = {
                    haptics.letterTick()
                    onPrevMonth()
                },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Bulan Lalu",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(12.dp)
                )
            }

            IconButton(
                onClick = {
                    haptics.letterTick()
                    onNextMonth()
                },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Bulan Berikut",
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Weekday labels (M, S, S, R, K, J, S)
 */
@Composable
private fun CalendarWeekdayHeader() {
    val weekdays = listOf("M", "S", "S", "R", "K", "J", "S")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        weekdays.forEach { day ->
            Text(
                text = day,
                color = Color(0xFF64748B),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Calendar Monthly Days Grid
 */
@Composable
private fun CalendarDaysGrid(
    firstDayOfWeek: Int,
    daysInMonth: Int,
    isCurrentMonth: Boolean,
    todayDate: Int,
    haptics: FlowHaptics,
    gridHeight: Dp
) {
    val totalCells = firstDayOfWeek + daysInMonth
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items((0 until totalCells).toList()) { index ->
            if (index < firstDayOfWeek) {
                Box(modifier = Modifier.size(18.dp))
            } else {
                val dayNumber = index - firstDayOfWeek + 1
                val isToday = isCurrentMonth && dayNumber == todayDate

                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (isToday) Color(0xFF0078D7) else Color.Transparent)
                        .clickable {
                            haptics.letterTick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$dayNumber",
                        color = if (isToday) Color.White else Color(0xFFCBD5E1),
                        fontSize = 8.5.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Windows 11 Notification Item Card
 */
@Composable
private fun NotificationItemCard(
    notif: ActiveNotification,
    getAppIcon: (String) -> Drawable?,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(notif.postTime) {
        if (notif.postTime > 0) timeFormat.format(Date(notif.postTime)) else ""
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x22FFFFFF))
            .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            val iconDrawable = remember(notif.packageName) { getAppIcon(notif.packageName) }
            if (iconDrawable != null) {
                AsyncImage(
                    model = iconDrawable,
                    contentDescription = null,
                    modifier = Modifier
                        .size(15.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(5.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notif.title.ifEmpty { "Notifikasi" },
                        color = Color(0xFFF1F5F9),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (formattedTime.isNotEmpty()) {
                        Text(
                            text = formattedTime,
                            color = Color(0xFF94A3B8),
                            fontSize = 7.5.sp,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }

                if (notif.text.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = notif.text,
                        color = Color(0xFFCBD5E1),
                        fontSize = 8.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(13.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Hapus",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(9.dp)
                )
            }
        }
    }
}
