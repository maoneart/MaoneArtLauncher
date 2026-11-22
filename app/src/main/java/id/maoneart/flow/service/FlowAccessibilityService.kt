package id.maoneart.flow.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import java.lang.ref.WeakReference

/**
 * Lightweight Accessibility Service dedicated to providing system actions:
 * 1. Double-Tap to Lock Screen (GLOBAL_ACTION_LOCK_SCREEN) without disabling Biometrics / Fingerprint.
 * 2. Gesture-based Notification and Quick Settings expansion.
 */
class FlowAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = WeakReference(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Zero event processing needed; this service purely acts as an action provider
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (instance?.get() == this) {
            instance = null
        }
    }

    companion object {
        private var instance: WeakReference<FlowAccessibilityService>? = null

        val isRunning: Boolean
            get() = instance?.get() != null

        /**
         * Lock the screen using Android 9+ GLOBAL_ACTION_LOCK_SCREEN.
         * Crucially, this does NOT invalidate biometric/fingerprint authentication on next wake!
         */
        fun lockScreen(): Boolean {
            val service = instance?.get() ?: return false
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            } else {
                false
            }
        }

        fun openNotifications(): Boolean {
            val service = instance?.get() ?: return false
            return service.performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
        }

        fun openQuickSettings(): Boolean {
            val service = instance?.get() ?: return false
            return service.performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
        }

        /**
         * Checks if this Accessibility Service is currently enabled in Android system settings.
         */
        fun isServiceEnabled(context: Context): Boolean {
            if (isRunning) return true
            return try {
                val expected = "${context.packageName}/${FlowAccessibilityService::class.java.canonicalName}"
                val enabledServices = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ) ?: return false
                enabledServices.split(":").any { it.equals(expected, ignoreCase = true) }
            } catch (_: Throwable) {
                false
            }
        }

        /**
         * Launch Android Accessibility Settings so the user can easily toggle the service.
         */
        fun openAccessibilitySettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Throwable) {}
        }
    }
}
