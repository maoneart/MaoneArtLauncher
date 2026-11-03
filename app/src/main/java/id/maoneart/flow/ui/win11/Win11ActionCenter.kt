package id.maoneart.flow.ui.win11

import android.app.Activity
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.BatteryChargingFull
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
 * Features real Android device status and toggles:
 * - Wi-Fi (live status & Quick Settings panel)
 * - Bluetooth (live adapter status & toggle/settings)
 * - Senter / Flashlight Torch (real CameraManager LED toggle)
 * - Mode Pesawat / Airplane Mode (live global status & panel)
 * - Jangan Ganggu / DND (live interruption filter & toggle)
 * - Rotasi Layar / Auto-rotation (live accelerometer rotation & toggle)
 * - Live Window Brightness slider
 * - Live Media Volume slider synced with AudioManager
 * - Live Battery percentage and charging indicator
 */
@Composable
fun Win11ActionCenter(
    isLandscape: Boolean,
    onOpenLauncherSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberFlowHaptics()
    val context = LocalContext.current
    val activity = context as? Activity

    // --- 1. LIVE WI-FI STATUS ---
    val wifiManager = remember { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager }
    var isWifiOn by remember { mutableStateOf(wifiManager?.isWifiEnabled == true) }

    // --- 2. LIVE BLUETOOTH STATUS ---
    val bluetoothAdapter = remember {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
            ?: BluetoothAdapter.getDefaultAdapter()
    }
    var isBluetoothOn by remember { mutableStateOf(bluetoothAdapter?.isEnabled == true) }

    // --- 3. LIVE AIRPLANE MODE STATUS ---
    var isAirplaneOn by remember {
        mutableStateOf(
            try {
                Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
            } catch (_: Throwable) { false }
        )
    }

    // --- 4. LIVE FLASHLIGHT / SENTER (CAMERA MANAGER) ---
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager }
    val cameraId = remember {
        try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager?.cameraIdList?.firstOrNull()
        } catch (_: Throwable) { null }
    }
    var isFlashlightOn by remember { mutableStateOf(false) }

    // Torch state listener
    DisposableEffect(cameraManager, cameraId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraManager != null) {
            val torchCallback = object : CameraManager.TorchCallback() {
                override fun onTorchModeChanged(camId: String, enabled: Boolean) {
                    if (cameraId == null || camId == cameraId) {
                        isFlashlightOn = enabled
                    }
                }
            }
            try {
                cameraManager.registerTorchCallback(torchCallback, null)
            } catch (_: Throwable) {}
            onDispose {
                try { cameraManager.unregisterTorchCallback(torchCallback) } catch (_: Throwable) {}
            }
        } else {
            onDispose {}
        }
    }

    // --- 5. LIVE AUTO-ROTATION STATUS ---
    var isRotationOn by remember {
        mutableStateOf(
            try {
                Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
            } catch (_: Throwable) { true }
        )
    }

    // --- 6. LIVE DND / JANGAN GANGGU STATUS ---
    val notificationManager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager }
    var isDndOn by remember {
        mutableStateOf(
            notificationManager?.currentInterruptionFilter?.let {
                it != NotificationManager.INTERRUPTION_FILTER_ALL
            } ?: false
        )
    }

    // --- 7. LIVE AUDIO VOLUME ---
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember { (audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15).coerceAtLeast(1) }
    var volumeValue by remember {
        val currentVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 8
        mutableFloatStateOf((currentVol.toFloat() / maxVolume).coerceIn(0f, 1f))
    }

    // --- 8. LIVE SCREEN BRIGHTNESS ---
    var brightnessValue by remember {
        val currentWinBrightness = activity?.window?.attributes?.screenBrightness ?: -1f
        val initial = if (currentWinBrightness >= 0f) {
            currentWinBrightness
        } else {
            try {
                Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128) / 255f
            } catch (_: Throwable) {
                0.6f
            }
        }
        mutableFloatStateOf(initial.coerceIn(0.05f, 1.0f))
    }

    // --- 9. LIVE BATTERY MONITOR ---
    var batteryPct by remember { mutableIntStateOf(100) }
    var isCharging by remember { mutableStateOf(false) }

    // BroadcastReceivers for Wi-Fi, Bluetooth, Airplane, Volume, Battery
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                when (intent?.action) {
                    WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                        val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                        isWifiOn = state == WifiManager.WIFI_STATE_ENABLED
                    }
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                        isBluetoothOn = state == BluetoothAdapter.STATE_ON
                    }
                    Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                        isAirplaneOn = intent.getBooleanExtra("state", false)
                    }
                    "android.media.VOLUME_CHANGED_ACTION" -> {
                        val streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
                        if (streamType == AudioManager.STREAM_MUSIC) {
                            val newVol = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1)
                            if (newVol >= 0) {
                                volumeValue = (newVol.toFloat() / maxVolume).coerceIn(0f, 1f)
                            }
                        }
                    }
                    Intent.ACTION_BATTERY_CHANGED -> {
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
        }

        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction("android.media.VOLUME_CHANGED_ACTION")
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        context.registerReceiver(receiver, filter)

        onDispose {
            try { context.unregisterReceiver(receiver) } catch (_: Throwable) {}
        }
    }

    val cardModifier = if (isLandscape) {
        Modifier
            .width(360.dp)
            .height(350.dp)
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
                // ROW 1: Wi-Fi, Bluetooth, Flashlight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Wi-Fi Tile
                    ActionCenterTile(
                        icon = Icons.Rounded.Wifi,
                        label = "Wi-Fi",
                        isActive = isWifiOn,
                        onClick = {
                            haptics.letterTick()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                try {
                                    context.startActivity(Intent(Settings.Panel.ACTION_WIFI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (_: Throwable) {
                                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                            } else {
                                try {
                                    @Suppress("DEPRECATION")
                                    wifiManager?.setWifiEnabled(!isWifiOn)
                                } catch (_: Throwable) {
                                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Bluetooth Tile
                    ActionCenterTile(
                        icon = Icons.Rounded.Bluetooth,
                        label = "Bluetooth",
                        isActive = isBluetoothOn,
                        onClick = {
                            haptics.letterTick()
                            if (bluetoothAdapter != null) {
                                if (isBluetoothOn) {
                                    try {
                                        @Suppress("DEPRECATION")
                                        bluetoothAdapter.disable()
                                    } catch (_: Throwable) {
                                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                    }
                                } else {
                                    try {
                                        @Suppress("DEPRECATION")
                                        bluetoothAdapter.enable()
                                    } catch (_: Throwable) {
                                        try {
                                            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(enableBtIntent)
                                        } catch (_: Throwable) {
                                            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                        }
                                    }
                                }
                            } else {
                                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Flashlight / Senter Tile (Real Hardware LED toggle)
                    ActionCenterTile(
                        icon = Icons.Rounded.FlashlightOn,
                        label = "Senter",
                        isActive = isFlashlightOn,
                        onClick = {
                            haptics.letterTick()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraManager != null && cameraId != null) {
                                try {
                                    val nextState = !isFlashlightOn
                                    cameraManager.setTorchMode(cameraId, nextState)
                                    isFlashlightOn = nextState
                                } catch (_: Throwable) {}
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // ROW 2: Mode Pesawat, Jangan Ganggu, Rotasi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Airplane Mode Tile
                    ActionCenterTile(
                        icon = Icons.Rounded.AirplanemodeActive,
                        label = "Pesawat",
                        isActive = isAirplaneOn,
                        onClick = {
                            haptics.letterTick()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                try {
                                    context.startActivity(Intent(Settings.Panel.ACTION_AIRPLANE_MODE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (_: Throwable) {
                                    context.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                            } else {
                                context.startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Do Not Disturb / Jangan Ganggu Tile
                    ActionCenterTile(
                        icon = Icons.Rounded.DoNotDisturb,
                        label = "Jgn Ganggu",
                        isActive = isDndOn,
                        onClick = {
                            haptics.letterTick()
                            try {
                                if (notificationManager?.isNotificationPolicyAccessGranted == true) {
                                    val newFilter = if (isDndOn) NotificationManager.INTERRUPTION_FILTER_ALL else NotificationManager.INTERRUPTION_FILTER_PRIORITY
                                    notificationManager.setInterruptionFilter(newFilter)
                                    isDndOn = newFilter != NotificationManager.INTERRUPTION_FILTER_ALL
                                } else {
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                            } catch (_: Throwable) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (_: Throwable) {}
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Auto-rotation / Rotasi Tile
                    ActionCenterTile(
                        icon = Icons.Rounded.ScreenRotation,
                        label = "Rotasi",
                        isActive = isRotationOn,
                        onClick = {
                            haptics.letterTick()
                            try {
                                if (Settings.System.canWrite(context)) {
                                    val next = if (isRotationOn) 0 else 1
                                    Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, next)
                                    isRotationOn = next == 1
                                } else {
                                    context.startActivity(Intent(Settings.ACTION_AUTO_ROTATE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                }
                            } catch (_: Throwable) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (_: Throwable) {}
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // --- 2. LIVE SLIDERS (BRIGHTNESS & VOLUME) ---
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Brightness Slider (Live window screenBrightness)
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
                        onValueChange = { newBrightness ->
                            brightnessValue = newBrightness
                            val lp = activity?.window?.attributes
                            if (lp != null) {
                                lp.screenBrightness = newBrightness.coerceIn(0.02f, 1.0f)
                                activity.window.attributes = lp
                            }
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4CC2FF),
                            activeTrackColor = Color(0xFF0078D7),
                            inactiveTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Volume Slider (Live STREAM_MUSIC)
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
                        onValueChange = { newVolRatio ->
                            volumeValue = newVolRatio
                            val targetVol = (newVolRatio * maxVolume).toInt().coerceIn(0, maxVolume)
                            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
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

            // --- 3. BOTTOM BAR (BATTERY & LAUNCHER SETTINGS) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCharging) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryChargingFull,
                            contentDescription = "Battery Charging",
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.BatteryFull,
                            contentDescription = "Battery",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCharging) "Mengisi Daya ($batteryPct%)" else "Baterai $batteryPct%",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

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
            .padding(horizontal = 4.dp, vertical = 6.dp),
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
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
