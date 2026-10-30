package id.maoneart.flow.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FlowHaptics(private val context: Context) {

    private val hapticScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Throwable) {
        null
    }

    private val hasVibrator: Boolean = try {
        vibrator?.hasVibrator() == true
    } catch (_: Throwable) {
        false
    }

    private val sonificationAttributes: AudioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .build()

    @Volatile
    private var lastTickTime: Long = 0L

    /**
     * Crisp mechanical tactile tick for Alphabet Wave Scrolling (Niagara style).
     * Non-blocking background vibrator dispatch + 38ms rate limiting completely eliminates
     * UI thread Binder IPC stalls and eliminates lag.
     */
    fun tick(view: View? = null) {
        val now = SystemClock.uptimeMillis()
        if (now - lastTickTime < 38L) return
        lastTickTime = now

        // 1. Instant view-level touch feedback on UI thread (0ms)
        view?.let { v ->
            try {
                v.isHapticFeedbackEnabled = true
                v.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or 1 // FLAG_IGNORE_VIEW_SETTING
                )
            } catch (_: Throwable) {}
        }

        // 2. Hardware vibrator executed on background thread: ZERO UI THREAD JANK
        if (hasVibrator && vibrator != null) {
            hapticScope.launch {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(30L, 170), sonificationAttributes)
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(30L, sonificationAttributes)
                    }
                } catch (_: Throwable) {}
            }
        }
    }

    /**
     * Firm tactile click for button presses & chip selections.
     */
    fun click(view: View? = null) {
        view?.let { v ->
            try {
                v.isHapticFeedbackEnabled = true
                v.performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or 1
                )
            } catch (_: Throwable) {}
        }

        if (hasVibrator && vibrator != null) {
            hapticScope.launch {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(40L, 210), sonificationAttributes)
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(40L, sonificationAttributes)
                    }
                } catch (_: Throwable) {}
            }
        }
    }

    /**
     * Heavy tactile pulse for long-press actions.
     */
    fun heavyClick(view: View? = null) {
        view?.let { v ->
            try {
                v.isHapticFeedbackEnabled = true
                v.performHapticFeedback(
                    HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or 1
                )
            } catch (_: Throwable) {}
        }

        if (hasVibrator && vibrator != null) {
            hapticScope.launch {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(55L, 255), sonificationAttributes)
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(55L, sonificationAttributes)
                    }
                } catch (_: Throwable) {}
            }
        }
    }
}

@Composable
fun rememberFlowHaptics(): FlowHaptics {
    val context = LocalContext.current
    return remember(context) { FlowHaptics(context.applicationContext) }
}
