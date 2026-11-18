package id.maoneart.flow.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shortcut
import androidx.compose.material.icons.rounded.SwipeRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import id.maoneart.flow.data.AppShortcutManager
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.model.FlowShortcutItem
import id.maoneart.flow.ui.theme.FlowDarkBackground
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowOrange
import id.maoneart.flow.ui.theme.FlowRed
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary
import id.maoneart.flow.ui.util.rememberFlowHaptics

/**
 * Niagara-style Pop-up Bottom Sheet displaying:
 * 1. App Shortcuts (Android Shortcuts API: e.g. "Chat Baru", "Kamera", "Tab Penyamaran")
 * 2. Secondary Swipe Action configuration (Swipe Right on App -> Launch Assigned App)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShortcutsBottomSheet(
    app: AppInfo,
    allApps: List<AppInfo>,
    iconDrawable: Drawable?,
    assignedSwipeApp: AppInfo?,
    onDismiss: () -> Unit,
    onLaunchShortcut: (shortcutId: String) -> Unit,
    onAssignSwipeAction: (targetPackage: String?) -> Unit,
    getAppIconDrawable: (packageName: String) -> Drawable?
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptics = rememberFlowHaptics()

    var shortcuts by remember { mutableStateOf<List<FlowShortcutItem>>(emptyList()) }
    var isQueryingShortcuts by remember { mutableStateOf(true) }
    var hasShortcutHostPermission by remember { mutableStateOf(true) }
    var isSelectingSecondaryApp by remember { mutableStateOf(false) }

    LaunchedEffect(app.packageName) {
        hasShortcutHostPermission = AppShortcutManager.hasShortcutPermission(context)
        shortcuts = AppShortcutManager.getShortcutsForApp(context, app.packageName)
        isQueryingShortcuts = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FlowDarkBackground,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x55FFFFFF))
            )
        }
    ) {
        if (isSelectingSecondaryApp) {
            // Sub-view: Quick App Picker for Secondary Swipe Action
            SecondaryAppPickerView(
                currentApp = app,
                allApps = allApps,
                onSelectApp = { targetApp ->
                    haptics.heavyClick()
                    onAssignSwipeAction(targetApp.packageName)
                    isSelectingSecondaryApp = false
                },
                onCancel = { isSelectingSecondaryApp = false },
                getAppIconDrawable = getAppIconDrawable
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                // 1. App Header with Icon & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (iconDrawable != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(iconDrawable)
                                .crossfade(false)
                                .build(),
                            contentDescription = app.label,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x22FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = app.firstLetter.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = FlowElectricBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.label,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FlowTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pintasan & Aksi Geser",
                                fontSize = 12.sp,
                                color = FlowElectricBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup",
                        tint = FlowTextSecondary,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { onDismiss() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0x22FFFFFF))
                Spacer(modifier = Modifier.height(16.dp))

                // 2. SECTION: PINTASAN APLIKASI (Android Shortcuts API)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shortcut,
                        contentDescription = null,
                        tint = FlowElectricBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PINTASAN CEPAT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlowElectricBlue,
                        letterSpacing = 1.sp
                    )
                }

                if (shortcuts.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x1AFFFFFF))
                            .border(1.dp, Color(0x2600E5FF), RoundedCornerShape(14.dp))
                    ) {
                        shortcuts.forEachIndexed { index, shortcut ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        haptics.heavyClick()
                                        onDismiss()
                                        onLaunchShortcut(shortcut.id)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (shortcut.icon != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(shortcut.icon)
                                            .crossfade(false)
                                            .build(),
                                        contentDescription = shortcut.shortLabel,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(FlowElectricBlue.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "⚡",
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = shortcut.shortLabel,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FlowTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (!shortcut.longLabel.isNullOrBlank() && shortcut.longLabel != shortcut.shortLabel) {
                                        Text(
                                            text = shortcut.longLabel,
                                            fontSize = 11.5.sp,
                                            color = FlowTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Text(
                                    text = "➔",
                                    fontSize = 14.sp,
                                    color = FlowElectricBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (index < shortcuts.lastIndex) {
                                HorizontalDivider(
                                    color = Color(0x18FFFFFF),
                                    modifier = Modifier.padding(start = 58.dp)
                                )
                            }
                        }
                    }
                } else if (!isQueryingShortcuts) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x12FFFFFF))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = if (!hasShortcutHostPermission) {
                                "💡 Pintasan langsung aktif saat MaoneArt Flow dijadikan peluncur default."
                            } else {
                                "Tidak ada pintasan khusus yang didaftarkan oleh aplikasi ini."
                            },
                            fontSize = 12.5.sp,
                            color = FlowTextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. SECTION: AKSI GESER KANAN (SWIPE ACTION SECONDARY APP)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SwipeRight,
                        contentDescription = null,
                        tint = FlowOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AKSI GESER KANAN (SWIPE ACTION)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlowOrange,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Geser ke kanan pada ${app.label} di layar utama untuk langsung meluncurkan aksi ini:",
                    fontSize = 12.sp,
                    color = FlowTextSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                if (assignedSwipeApp != null) {
                    // Assigned Secondary App Card
                    val targetIcon = remember(assignedSwipeApp.packageName) {
                        getAppIconDrawable(assignedSwipeApp.packageName)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x1AFFFFFF))
                            .border(1.dp, FlowOrange.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (targetIcon != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(targetIcon)
                                        .crossfade(false)
                                        .build(),
                                    contentDescription = assignedSwipeApp.label,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FlowOrange.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = assignedSwipeApp.firstLetter.toString(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FlowOrange
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = assignedSwipeApp.label,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FlowTextPrimary
                                )
                                Text(
                                    text = "Akan terbuka otomatis saat digeser kanan",
                                    fontSize = 11.5.sp,
                                    color = FlowOrange
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Button Ganti
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x28FFFFFF))
                                    .clickable {
                                        haptics.click()
                                        isSelectingSecondaryApp = true
                                    }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = null,
                                        tint = FlowTextPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ganti Aplikasi",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FlowTextPrimary
                                    )
                                }
                            }

                            // Button Hapus
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x22FF334B))
                                    .clickable {
                                        haptics.heavyClick()
                                        onAssignSwipeAction(null)
                                    }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = null,
                                        tint = FlowRed,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Hapus Aksi",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FlowRed
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // No Assigned Secondary App: Button to choose one
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x18FFFFFF))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .clickable {
                                haptics.click()
                                isSelectingSecondaryApp = true
                            }
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "➕",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pilih Aplikasi untuk Aksi Geser Kanan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FlowElectricBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Filterable app selector view embedded within the bottom sheet
 */
@Composable
private fun SecondaryAppPickerView(
    currentApp: AppInfo,
    allApps: List<AppInfo>,
    onSelectApp: (AppInfo) -> Unit,
    onCancel: () -> Unit,
    getAppIconDrawable: (packageName: String) -> Drawable?
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filteredApps = remember(searchQuery, allApps, currentApp) {
        val query = searchQuery.trim().lowercase()
        allApps
            .filter { it.packageName != currentApp.packageName }
            .filter {
                if (query.isEmpty()) true
                else it.label.lowercase().contains(query) || it.packageName.lowercase().contains(query)
            }
            .sortedBy { it.label.lowercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.78f)
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Pilih Aplikasi Sekunder",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = FlowTextPrimary
                )
                Text(
                    text = "Akan dibuka saat ${currentApp.label} digeser kanan",
                    fontSize = 12.sp,
                    color = FlowTextSecondary
                )
            }

            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Batal",
                tint = FlowTextSecondary,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onCancel() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x22FFFFFF))
                .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = FlowElectricBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = FlowTextPrimary
                    ),
                    cursorBrush = SolidColor(FlowElectricBlue),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Cari nama aplikasi...",
                                fontSize = 14.sp,
                                color = FlowTextSecondary
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Apps List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(
                items = filteredApps,
                key = { it.packageName }
            ) { appItem ->
                val icon = remember(appItem.packageName) {
                    getAppIconDrawable(appItem.packageName)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectApp(appItem) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (icon != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(icon)
                                .crossfade(false)
                                .build(),
                            contentDescription = appItem.label,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x22FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = appItem.firstLetter.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FlowElectricBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appItem.label,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = FlowTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = appItem.packageName,
                            fontSize = 11.sp,
                            color = FlowTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
