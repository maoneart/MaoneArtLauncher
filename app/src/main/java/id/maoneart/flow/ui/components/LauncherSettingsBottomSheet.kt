package id.maoneart.flow.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maoneart.flow.ui.theme.FlowCyan
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherSettingsBottomSheet(
    showBanner: Boolean,
    onToggleShowBanner: (Boolean) -> Unit,
    searchAsIcon: Boolean,
    onToggleSearchAsIcon: (Boolean) -> Unit,
    widgetTheme: String,
    onSelectWidgetTheme: (String) -> Unit,
    showAlphabetScroller: Boolean,
    onToggleShowAlphabetScroller: (Boolean) -> Unit,
    pureTransparentBg: Boolean,
    onTogglePureTransparentBg: (Boolean) -> Unit,
    showCategories: Boolean,
    onToggleShowCategories: (Boolean) -> Unit,
    twoLineAppNames: Boolean,
    onToggleTwoLineAppNames: (Boolean) -> Unit,
    showIcons: Boolean,
    onToggleShowIcons: (Boolean) -> Unit,
    is24Hour: Boolean,
    onToggleIs24Hour: (Boolean) -> Unit,
    hapticsEnabled: Boolean,
    onToggleHaptics: (Boolean) -> Unit,
    hasNotificationAccess: Boolean,
    onOpenNotificationSettings: () -> Unit,
    hiddenAppsCount: Int = 0,
    onOpenHiddenApps: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xF2121316),
        tonalElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color(0x44FFFFFF))
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Pengaturan Flow",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlowTextPrimary
                    )
                    Text(
                        text = "Kustomisasi tampilan & fitur launcher",
                        fontSize = 12.sp,
                        color = FlowTextSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x18FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup",
                        tint = FlowTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // --- 1. WIDGET & HEADER THEME SECTION ---
            SettingsSectionHeader(title = "TEMA WIDGET & HEADER")

            Text(
                text = "Pilih Gaya Widget",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = FlowTextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WidgetThemeOptionCard(
                    title = "MaoneArt Classic",
                    subtitle = "Jam tipografi besar + kalender",
                    isSelected = widgetTheme == "classic",
                    onClick = { onSelectWidgetTheme("classic") },
                    modifier = Modifier.weight(1f)
                )

                WidgetThemeOptionCard(
                    title = "Niagara Trio Pills",
                    subtitle = "Kartu kapsul neon (Bug Theme)",
                    isSelected = widgetTheme == "niagara_pills",
                    onClick = { onSelectWidgetTheme("niagara_pills") },
                    modifier = Modifier.weight(1f)
                )
            }

            SettingsSwitchRow(
                title = "Tampilkan Banner Grafiti MaoneArt",
                subtitle = "Sembunyikan untuk tampilan minimalis bersih",
                checked = showBanner,
                onCheckedChange = onToggleShowBanner
            )

            Spacer(modifier = Modifier.height(14.dp))

            // --- 2. PENCARIAN (SEARCH BAR) SECTION ---
            SettingsSectionHeader(title = "GAYA PENCARIAN")

            SettingsSwitchRow(
                title = "Search Bar Jadi Tombol Logo",
                subtitle = "Tampilkan logo Google di kanan bawah (seperti di foto bug)",
                checked = searchAsIcon,
                onCheckedChange = onToggleSearchAsIcon
            )

            Spacer(modifier = Modifier.height(14.dp))

            // --- 3. TAMPILAN & LATAR BELAKANG ---
            SettingsSectionHeader(title = "TAMPILAN & LATAR BELAKANG")

            SettingsSwitchRow(
                title = "Background Murni Transparan",
                subtitle = "Hilangkan lapisan gelap agar wallpaper terlihat bening asli",
                checked = pureTransparentBg,
                onCheckedChange = onTogglePureTransparentBg
            )

            SettingsSwitchRow(
                title = "Tampilkan Baris Kategori",
                subtitle = "Sembunyikan untuk tampilan vertikal minimalis tanpa tab kategori",
                checked = showCategories,
                onCheckedChange = onToggleShowCategories
            )

            SettingsSwitchRow(
                title = "Tampilkan Scroller Alfabet A-Z",
                subtitle = "Sembunyikan jika ingin tampilan daftar penuh tanpa A-Z",
                checked = showAlphabetScroller,
                onCheckedChange = onToggleShowAlphabetScroller
            )

            SettingsSwitchRow(
                title = "Tampilkan Ikon Aplikasi",
                subtitle = "Mode minimalis teks saja jika dimatikan",
                checked = showIcons,
                onCheckedChange = onToggleShowIcons
            )

            SettingsSwitchRow(
                title = "Nama Aplikasi 2 Baris",
                subtitle = "Bungkus nama aplikasi panjang ke baris kedua agar tidak menabrak wallpaper",
                checked = twoLineAppNames,
                onCheckedChange = onToggleTwoLineAppNames
            )

            Spacer(modifier = Modifier.height(14.dp))

            // --- 4. SISTEM & NOTIFIKASI ---
            SettingsSectionHeader(title = "SISTEM & NOTIFIKASI")

            SettingsSwitchRow(
                title = "Format Waktu 24 Jam",
                subtitle = "Gunakan format jam 00:00 - 23:59",
                checked = is24Hour,
                onCheckedChange = onToggleIs24Hour
            )

            SettingsSwitchRow(
                title = "Getaran Haptik Tactile",
                subtitle = "Umpan balik getar saat scroll alfabet dan tombol",
                checked = hapticsEnabled,
                onCheckedChange = onToggleHaptics
            )

            // Akses Notifikasi Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x18FFFFFF))
                    .border(
                        1.dp,
                        if (hasNotificationAccess) FlowCyan.copy(alpha = 0.35f) else Color(0x66FF9800),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onOpenNotificationSettings() }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasNotificationAccess) "🔔" else "⚠️",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasNotificationAccess) "Akses Notifikasi: Aktif" else "Akses Notifikasi: Belum Aktif",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasNotificationAccess) FlowCyan else Color(0xFFFFB74D)
                        )
                        Text(
                            text = "Sentuh untuk membuka setelan akses notifikasi Android",
                            fontSize = 11.sp,
                            color = FlowTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- 5. PRIVASI & APLIKASI TERSEMBUNYI ---
            SettingsSectionHeader(title = "PRIVASI & APLIKASI TERSEMBUNYI")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x18FFFFFF))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                    .clickable { onOpenHiddenApps() }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VisibilityOff,
                            contentDescription = null,
                            tint = FlowElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Kelola Aplikasi Tersembunyi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = FlowTextPrimary
                            )
                            Text(
                                text = if (hiddenAppsCount > 0) "$hiddenAppsCount aplikasi disembunyikan" else "Tidak ada aplikasi tersembunyi",
                                fontSize = 11.sp,
                                color = if (hiddenAppsCount > 0) FlowCyan else FlowTextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Buka",
                        tint = FlowTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selesai Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FlowElectricBlue)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Selesai",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MaoneArt Flow v1.0.24 • By MaoneArt",
                fontSize = 11.sp,
                color = FlowTextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = FlowElectricBlue,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(vertical = 10.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = FlowTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = FlowTextSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = FlowElectricBlue,
                uncheckedThumbColor = Color(0xFFAAAAAA),
                uncheckedTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}

@Composable
private fun WidgetThemeOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) FlowElectricBlue else Color(0x22FFFFFF),
        label = "border_color"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) FlowElectricBlue.copy(alpha = 0.18f) else Color(0x14FFFFFF),
        label = "bg_color"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) FlowElectricBlue else FlowTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = FlowTextSecondary
            )
        }
    }
}
