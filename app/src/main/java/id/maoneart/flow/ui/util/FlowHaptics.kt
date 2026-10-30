package id.maoneart.flow.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class FlowHaptics(private val context: Context) {

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

    /**
     * Crisp mechanical tactile tick for Alphabet Wave Scrolling (Niagara style).
     * Dual-Layer: Directly drives hardware vibration motor for guaranteed physical feel
     * across all Android devices, plus view-level haptic constants.
     */
    fun tick(view: View? = null) {
        try {
            if (hasVibrator && vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(32L, 170), sonificationAttributes)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(32L, sonificationAttributes)
                }
            }
            view?.let { v ->
                v.isHapticFeedbackEnabled = true
                v.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or 1 // FLAG_IGNORE_VIEW_SETTING
                )
            }
        } catch (_: Throwable) {}
    }

    /**
     * Firm tactile click for button presses & chip selections.
     */
    fun click(view: View? = null) {
        try {
            if (hasVibrator && vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(42L, 210), sonificationAttributes)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(42L, sonificationAttributes)
                }
            }
            view?.let { v ->
                v.isHapticFeedbackEnabled = true
                v.performHapticFeedback(
                    HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or 1
                )
            }
        } catch (_: Throwable) {}
    }

    /**
     * Heavy tactile pulse for long-press actions.
     */
    fun heavyClick(view: View? = null) {
        try {
            if (hasVibrator && vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(55L, 255), sonificationAttributes)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(55L, sonificationAttributes)
                }
            }
            view?.let { v ->
                v.isHapticFeedbackEnabled = true
                v.performHapticFeedback(
                    HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING or 1
                )
            }
        } catch (_: Throwable) {}
    }
}

@Composable
fun rememberFlowHaptics(): FlowHaptics {
    val context = LocalContext.current
    return remember(context) { FlowHaptics(context.applicationContext) }
}
