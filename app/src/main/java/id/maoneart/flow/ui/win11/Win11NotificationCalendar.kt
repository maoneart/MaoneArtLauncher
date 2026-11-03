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
import androidx.compose.material.icons.rounded.CalendarMonth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import id.maoneart.flow.service.ActiveNotification
import id.maoneart.flow.service.NotificationService
import id.maoneart.flow.ui.util.rememberFlowHaptics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Windows 11 Notification & Calendar Flyout
 * Opens when the Clock/Date in the Taskbar is tapped.
 * Displays real-time calendar and active notification management.
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

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("id", "ID")) }
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
            .height(490.dp)
    } else {
        Modifier
            .fillMaxWidth(0.94f)
            .height(490.dp)
    }

    Box(
        modifier = modifier
            .then(cardModifier)
            .shadow(24.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xF21C222E)) // Win11 Dark Mica
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
            .clickable(enabled = false) {}
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- 1. NOTIFICATIONS SECTION (HEADER + LIST) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Notifications Header
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
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pemberitahuan",
                            color = Color(0xFFF1F5F9),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (activeNotifications.isNotEmpty()) {
                            Text(
                                text = "Hapus Semua",
                                color = Color(0xFF60A5FA),
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        haptics.letterTick()
                                        NotificationService.currentInstance?.clearAllUserNotifications()
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "Pengaturan",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    haptics.letterTick()
                                    onOpenNotificationSettings()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Content: Permission Alert, Empty State, or Active Notifications List
                if (!hasNotificationAccess) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Akses Notifikasi Belum Aktif",
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Ketuk untuk memberikan izin notifikasi",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                } else if (activeNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1AFFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsNone,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tidak ada pemberitahuan baru",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0x22FFFFFF), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // --- 2. MONTHLY CALENDAR WIDGET ---
            Column(modifier = Modifier.fillMaxWidth()) {
                // Calendar Month Navigation Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthYearText,
                        color = Color(0xFFF1F5F9),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                haptics.letterTick()
                                monthOffset--
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronLeft,
                                contentDescription = "Prev Month",
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                haptics.letterTick()
                                monthOffset++
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = "Next Month",
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Weekday Headers (Min, Sen, Sel, Rab, Kam, Jum, Sab)
                val weekdays = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    weekdays.forEach { day ->
                        Text(
                            text = day,
                            color = Color(0xFF64748B),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Days Grid (Total cells = blank cells + days)
                val totalCells = firstDayOfWeek + daysInMonth
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items((0 until totalCells).toList()) { index ->
                        if (index < firstDayOfWeek) {
                            Box(modifier = Modifier.size(26.dp))
                        } else {
                            val dayNumber = index - firstDayOfWeek + 1
                            val isToday = isCurrentMonth && dayNumber == todayDate

                            Box(
                                modifier = Modifier
                                    .size(26.dp)
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
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
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
            .padding(8.dp)
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
                        .size(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notif.title.ifEmpty { "Pemberitahuan" },
                        color = Color(0xFFF1F5F9),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (formattedTime.isNotEmpty()) {
                        Text(
                            text = formattedTime,
                            color = Color(0xFF94A3B8),
                            fontSize = 9.5.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                if (notif.text.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = notif.text,
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Hapus",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
