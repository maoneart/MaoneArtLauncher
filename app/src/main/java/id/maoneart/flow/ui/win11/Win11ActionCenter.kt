package id.maoneart.flow.ui.win11

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.DoNotDisturb
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maoneart.flow.ui.util.rememberFlowHaptics

/**
 * Authentic Windows 11 Action Center & Quick Settings Flyout
 * Features quick toggles, volume & brightness sliders, battery info,
 * and direct links to Android system settings.
 */
@Composable
fun Win11ActionCenter(
    isLandscape: Boolean,
    onOpenLauncherSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberFlowHaptics()
    val context = LocalContext.current

    // Audio Manager for Volume control
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 15f }
    val currentVolume = remember { audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 8f }
    var volumeValue by remember { mutableFloatStateOf(currentVolume / maxVolume) }

    // Screen Brightness state simulation
    var brightnessValue by remember { mutableFloatStateOf(0.75f) }

    // Quick Toggles state
    var isWifiOn by remember { mutableStateOf(true) }
    var isBluetoothOn by remember { mutableStateOf(false) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var isAirplaneOn by remember { mutableStateOf(false) }
    var isDndOn by remember { mutableStateOf(false) }
    var isRotationOn by remember { mutableStateOf(true) }

    val cardModifier = if (isLandscape) {
        Modifier
            .width(360.dp)
            .height(340.dp)
    } else {
        Modifier
            .fillMaxWidth(0.92f)
            .height(350.dp)
    }

    Box(
        modifier = modifier
            .then(cardModifier)
            .shadow(24.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xF21C222E)) // Win11 Dark Mica
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
            .clickable(enabled = false) {}
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- 1. QUICK TOGGLES (2 ROWS x 3 COLUMNS) ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionCenterTile(
                        icon = Icons.Rounded.Wifi,
                        label = "Wi-Fi",
                        isActive = isWifiOn,
                        onClick = {
                            haptics.letterTick()
                            isWifiOn = !isWifiOn
                            try { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) } catch (_: Throwable) {}
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCenterTile(
                        icon = Icons.Rounded.Bluetooth,
                        label = "Bluetooth",
                        isActive = isBluetoothOn,
                        onClick = {
                            haptics.letterTick()
                            isBluetoothOn = !isBluetoothOn
                            try { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) } catch (_: Throwable) {}
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCenterTile(
                        icon = Icons.Rounded.FlashlightOn,
                        label = "Senter",
                        isActive = isFlashlightOn,
                        onClick = {
                            haptics.letterTick()
                            isFlashlightOn = !isFlashlightOn
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionCenterTile(
                        icon = Icons.Rounded.AirplanemodeActive,
                        label = "Pesawat",
                        isActive = isAirplaneOn,
                        onClick = {
                            haptics.letterTick()
                            isAirplaneOn = !isAirplaneOn
                            try { context.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)) } catch (_: Throwable) {}
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCenterTile(
                        icon = Icons.Rounded.DoNotDisturb,
                        label = "Jangan Ganggu",
                        isActive = isDndOn,
                        onClick = {
                            haptics.letterTick()
                            isDndOn = !isDndOn
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ActionCenterTile(
                        icon = Icons.Rounded.ScreenRotation,
                        label = "Rotasi",
                        isActive = isRotationOn,
                        onClick = {
                            haptics.letterTick()
                            isRotationOn = !isRotationOn
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- 2. SLIDERS (BRIGHTNESS & VOLUME) ---
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Brightness Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BrightnessHigh,
                        contentDescription = "Brightness",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Slider(
                        value = brightnessValue,
                        onValueChange = {
                            brightnessValue = it
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4CC2FF),
                            activeTrackColor = Color(0xFF0078D7),
                            inactiveTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Volume Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VolumeUp,
                        contentDescription = "Volume",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Slider(
                        value = volumeValue,
                        onValueChange = {
                            volumeValue = it
                            audioManager?.setStreamVolume(
                                AudioManager.STREAM_MUSIC,
                                (it * maxVolume).toInt(),
                                0
                            )
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4CC2FF),
                            activeTrackColor = Color(0xFF0078D7),
                            inactiveTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- 3. BOTTOM BAR (BATTERY & SETTINGS) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.BatteryFull,
                        contentDescription = "Battery",
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Baterai Normal",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            haptics.letterTick()
                            onOpenLauncherSettings()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFFE2E8F0),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Windows 11 Action Center Pill Toggle Button
 */
@Composable
private fun ActionCenterTile(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color(0xFF0078D7) else Color(0x22FFFFFF))
            .border(
                width = 1.dp,
                color = if (isActive) Color(0xFF4CC2FF) else Color(0x15FFFFFF),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else Color(0xFFCBD5E1),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (isActive) Color.White else Color(0xFF94A3B8),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
