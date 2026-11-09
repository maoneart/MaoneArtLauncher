package id.maoneart.flow.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.service.ActiveNotification
import id.maoneart.flow.service.NotificationService
import id.maoneart.flow.ui.theme.FlowDarkBackground
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowOrange
import id.maoneart.flow.ui.theme.FlowRed
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary
import id.maoneart.flow.ui.util.rememberFlowHaptics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppActionBottomSheet(
    app: AppInfo,
    iconDrawable: Drawable?,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onChangeCategory: () -> Unit,
    onOpenInfo: () -> Unit,
    onUninstall: () -> Unit,
    onToggleHidden: () -> Unit,
    onOpenSettings: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptics = rememberFlowHaptics()
    val isSelf = (app.packageName == context.packageName)

    // Observe active notifications for this specific app (WhatsApp, Telegram, SMS, etc.)
    val allActiveNotifs by NotificationService.activeNotificationList.collectAsState()
    val appNotifications = remember(allActiveNotifs, app.packageName) {
        allActiveNotifs.filter { it.packageName == app.packageName }.take(4)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FlowDarkBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Header: Icon + App Label + Package + Category Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                if (iconDrawable != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(iconDrawable)
                            .crossfade(true)
                            .build(),
                        contentDescription = app.label,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.label,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlowTextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "${app.categoryIcon} ${app.categoryTitle}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = FlowElectricBlue,
                            modifier = Modifier
                                .background(FlowElectricBlue.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = app.packageName,
                            fontSize = 11.sp,
                            color = FlowTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // --- NIAGARA-STYLE INTERACTIVE CHAT & DIRECT REPLY SECTION ---
            if (appNotifications.isNotEmpty()) {
                Text(
                    text = "💬 PESAN MASUK & BALAS LANGSUNG (${appNotifications.size})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = FlowElectricBlue,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    appNotifications.forEach { notif ->
                        NiagaraNotificationReplyCard(
                            notif = notif,
                            onOpenChat = {
                                haptics.letterTick()
                                try {
                                    if (notif.contentIntent != null) {
                                        notif.contentIntent.send()
                                        onDismiss()
                                    } else {
                                        onDismiss()
                                        onLaunch()
                                    }
                                } catch (_: Throwable) {
                                    onDismiss()
                                    onLaunch()
                                }
                            },
                            onDismissNotif = {
                                haptics.letterTick()
                                NotificationService.currentInstance?.dismissNotification(notif.key)
                            }
                        )
                    }
                }

                HorizontalDivider(
                    color = Color(0x1FFFFFFF),
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (isSelf) {
                ActionItem(
                    icon = Icons.Rounded.Settings,
                    title = "Buka Pengaturan Flow",
                    iconTint = FlowElectricBlue,
                    onClick = {
                        onDismiss()
                        onOpenSettings?.invoke()
                    }
                )

                ActionItem(
                    icon = Icons.Rounded.Info,
                    title = "Info Aplikasi",
                    iconTint = FlowElectricBlue,
                    onClick = {
                        onDismiss()
                        onOpenInfo()
                    }
                )
            } else {
                // Action: Launch
                ActionItem(
                    icon = Icons.Rounded.PlayArrow,
                    title = "Buka Aplikasi",
                    iconTint = FlowElectricBlue,
                    onClick = {
                        onDismiss()
                        onLaunch()
                    }
                )

                // Action: Favorite Toggle
                ActionItem(
                    icon = if (app.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    title = if (app.isFavorite) "Hapus dari Favorit" else "Jadikan Favorit",
                    iconTint = FlowOrange,
                    onClick = {
                        onToggleFavorite()
                        onDismiss()
                    }
                )

                // Action: Change Category
                ActionItem(
                    icon = Icons.Rounded.Folder,
                    title = "Ubah Kategori (${app.categoryTitle})",
                    iconTint = FlowElectricBlue,
                    onClick = {
                        onDismiss()
                        onChangeCategory()
                    }
                )

                // Action: App Info
                ActionItem(
                    icon = Icons.Rounded.Info,
                    title = "Info Aplikasi",
                    iconTint = FlowElectricBlue,
                    onClick = {
                        onDismiss()
                        onOpenInfo()
                    }
                )

                // Action: Hide
                ActionItem(
                    icon = Icons.Rounded.VisibilityOff,
                    title = "Sembunyikan dari Daftar",
                    iconTint = FlowTextSecondary,
                    onClick = {
                        onToggleHidden()
                        onDismiss()
                    }
                )

                // Action: Uninstall
                ActionItem(
                    icon = Icons.Rounded.Delete,
                    title = "Hapus Instalasi (Uninstall)",
                    iconTint = FlowRed,
                    onClick = {
                        onDismiss()
                        onUninstall()
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Niagara Launcher-inspired Notification & Direct Reply Card
 * Allows reading full WhatsApp/Telegram/SMS messages and sending inline replies
 * without leaving the launcher.
 */
@Composable
private fun NiagaraNotificationReplyCard(
    notif: ActiveNotification,
    onOpenChat: () -> Unit,
    onDismissNotif: () -> Unit
) {
    val context = LocalContext.current
    val haptics = rememberFlowHaptics()
    var replyText by remember(notif.key) { mutableStateOf("") }
    val sentReplies = remember(notif.key) { mutableStateListOf<String>() }
    val timeStr = remember(notif.postTime) {
        try {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(notif.postTime))
        } catch (_: Throwable) {
            ""
        }
    }

    fun submitReply(messageToSend: String) {
        val clean = messageToSend.trim()
        if (clean.isEmpty()) return
        val ok = NotificationService.sendDirectReply(context, notif, clean)
        if (ok) {
            haptics.heavyClick()
            sentReplies.add(clean)
            replyText = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1D222C))
            .border(1.dp, Color(0x2E00E5FF), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // 1. Sender Title + Time + Dismiss button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenChat)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notif.title.ifEmpty { "Pesan Baru" },
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (timeStr.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $timeStr",
                        fontSize = 11.sp,
                        color = FlowTextSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Hapus Notifikasi",
                tint = FlowTextSecondary,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDismissNotif)
                    .padding(3.dp)
            )
        }

        // 2. Incoming Message Body
        if (notif.text.isNotEmpty()) {
            Text(
                text = notif.text,
                fontSize = 13.5.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenChat)
                    .padding(top = 6.dp, bottom = 8.dp)
            )
        }

        // 3. Sent Replies History (Niagara conversation thread style)
        if (sentReplies.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                sentReplies.forEach { sentMsg ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background( Color(0xFF005C4B))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Terkirim",
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Kamu: $sentMsg",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 4. Direct Reply Box + Quick Reply Chips (When notification supports RemoteInput)
        if (notif.canReply) {
            val quickChips = listOf("Oke siap 👍", "Sebentar ya 🙏", "OTW 🚀", "Terima kasih 🙏")
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp)
            ) {
                quickChips.forEach { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF283040))
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(8.dp))
                            .clickable { submitReply(chip) }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = chip,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF93C5FD)
                        )
                    }
                }
            }

            // Custom Direct Reply Input Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF12151C))
                    .border(1.dp, Color(0x3300E5FF), RoundedCornerShape(12.dp))
                    .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Reply,
                    contentDescription = "Reply",
                    tint = FlowElectricBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (replyText.isEmpty()) {
                        Text(
                            text = "Balas langsung ke ${notif.title.take(18)}...",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 13.5.sp
                        ),
                        cursorBrush = SolidColor(FlowElectricBlue),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = { submitReply(replyText) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (replyText.isNotBlank()) FlowElectricBlue else Color(0xFF262D3B)
                        )
                        .clickable(enabled = replyText.isNotBlank()) {
                            submitReply(replyText)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Kirim Balasan",
                        tint = if (replyText.isNotBlank()) Color(0xFF090D14) else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    title: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = FlowTextPrimary
        )
    }
}
