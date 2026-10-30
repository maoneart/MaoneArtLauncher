package id.maoneart.flow.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.CalendarToday
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.maoneart.flow.R
import id.maoneart.flow.ui.theme.FlowCyan
import id.maoneart.flow.ui.theme.FlowElectricBlue
import id.maoneart.flow.ui.theme.FlowRed
import id.maoneart.flow.ui.theme.FlowTextPrimary
import id.maoneart.flow.ui.theme.FlowTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DigitalClockHeader(
    is24Hour: Boolean = true,
    showBanner: Boolean = true,
    widgetTheme: String = "classic",
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var timeString by remember { mutableStateOf("") }
    var dayOfWeekString by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }
    var fullDateString by remember { mutableStateOf("") }
    var batteryLevel by remember { mutableIntStateOf(100) }
    var batteryTemp by remember { mutableIntStateOf(28) }
    var isCharging by remember { mutableStateOf(false) }

    // Instant real-time battery & charging listener (0ms reaction when charger is plugged or unplugged)
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                intent?.let { batteryStatus ->
                    val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        batteryLevel = (level * 100) / scale
                    }
                    val rawTemp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                    if (rawTemp > 0) {
                        batteryTemp = rawTemp / 10
                    }
                    val status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                 status == BatteryManager.BATTERY_STATUS_FULL
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        try {
            val initial = ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            initial?.let { receiver.onReceive(context, it) }
        } catch (_: Throwable) {
            try {
                val initial = context.registerReceiver(receiver, filter)
                initial?.let { receiver.onReceive(context, it) }
            } catch (_: Throwable) {}
        }

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Throwable) {}
        }
    }

    LaunchedEffect(is24Hour) {
        val localeId = Locale("id", "ID")
        val timeFormat = SimpleDateFormat(if (is24Hour) "HH:mm" else "hh:mm a", localeId)
        val dayFormat = SimpleDateFormat("EEEE", localeId)
        val shortDateFormat = SimpleDateFormat("MMM dd, yyyy", localeId)
        val fullDateFormat = SimpleDateFormat("EEEE, d MMMM", localeId)

        while (true) {
            val now = Date()
            timeString = timeFormat.format(now)
            dayOfWeekString = dayFormat.format(now).uppercase()
            dateString = shortDateFormat.format(now)
            fullDateString = fullDateFormat.format(now)

            // Synchronize delay exactly to the start of the next minute (sub-second accuracy with Android status bar)
            val nowMs = System.currentTimeMillis()
            val delayUntilNextMinute = (60000L - (nowMs % 60000L) + 50L).coerceIn(1000L, 60000L)
            delay(delayUntilNextMinute)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = if (showBanner) 8.dp else 6.dp)
    ) {
        // --- 1. EXCLUSIVE "MAONEART FLOW" GRAFFITI LOGO (Can be hidden via Settings) ---
        if (showBanner) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onOpenSettings?.invoke()
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.maoneart_graffiti_logo),
                    contentDescription = "MaoneArt Flow",
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .height(106.dp),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center
                )
            }
        }

        if (widgetTheme == "niagara_pills") {
            // --- 2A. NIAGARA TRIO PILLS THEME (Exact match with Bug screenshot) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pill 1: TODAY (Neon Green)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Throwable) {}
                        }
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 5.dp, height = 26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TODAY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.4.sp
                        )
                        Text(
                            text = "🕓 $timeString",
                            fontSize = 13.sp,
                            color = FlowTextSecondary
                        )
                    }
                }

                // Pill 2: DAY & DATE (Amber / Yellow)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_APP_CALENDAR)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Throwable) {}
                        }
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 5.dp, height = 26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD600))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = dayOfWeekString,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.4.sp
                        )
                        Text(
                            text = "📅 $dateString",
                            fontSize = 13.sp,
                            color = FlowTextSecondary
                        )
                    }
                }

                // Pill 3: WEATHER & BATTERY (Neon Cyan)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Throwable) {}
                        }
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 5.dp, height = 26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00B0FF))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "WEATHER & POWER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.4.sp
                        )
                        Text(
                            text = if (isCharging) "⚡ $batteryLevel% (Mengisi) • Cerah ${batteryTemp}°C" else "⚡ $batteryLevel% • Cerah ${batteryTemp}°C",
                            fontSize = 13.sp,
                            color = FlowTextSecondary
                        )
                    }
                }
            }
        } else {
            // --- 2B. MAONEART CLASSIC MINIMALIST DIGITAL CLOCK ---
            Text(
                text = timeString,
                fontSize = 54.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2.5).sp,
                color = FlowTextPrimary,
                lineHeight = 54.sp,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        try {
                            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Throwable) {
                            try {
                                val fallback = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_APP_CALENDAR)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(fallback)
                            } catch (_: Throwable) {}
                        }
                    }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ELEGANT DATE & BATTERY STATUS ROW
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Calendar / Date Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_APP_CALENDAR)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Throwable) {}
                        }
                        .padding(end = 12.dp, top = 2.dp, bottom = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = FlowCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = fullDateString,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = FlowTextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Frosted Glass Battery Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x18FFFFFF))
                        .clickable {
                            try {
                                val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Throwable) {
                                try {
                                    val fallback = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(fallback)
                                } catch (_: Throwable) {}
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = if (isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryFull,
                        contentDescription = null,
                        tint = when {
                            isCharging -> FlowCyan
                            batteryLevel <= 20 -> FlowRed
                            else -> FlowCyan
                        },
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isCharging) "⚡ $batteryLevel%" else "$batteryLevel%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FlowTextPrimary
                    )
                }
            }
        }
    }
}
