package id.maoneart.flow.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationService : NotificationListenerService() {

    companion object {
        private val _notificationCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
        val notificationCounts: StateFlow<Map<String, Int>> = _notificationCounts.asStateFlow()

        var isServiceConnected: Boolean = false
            private set

        fun isNotificationAccessGranted(context: Context): Boolean {
            return try {
                val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
                enabledPackages.contains(context.packageName)
            } catch (_: Throwable) {
                false
            }
        }

        fun openNotificationAccessSettings(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Throwable) {
                try {
                    val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallbackIntent)
                } catch (_: Throwable) {}
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceConnected = true
        updateActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceConnected = false
        _notificationCounts.value = emptyMap()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        updateActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateActiveNotifications()
    }

    private fun updateActiveNotifications() {
        try {
            val active = activeNotifications
            if (active == null || active.isEmpty()) {
                _notificationCounts.value = emptyMap()
                return
            }

            val counts = mutableMapOf<String, Int>()

            for (sbn in active) {
                // Ignore ongoing notifications like media playback or foreground service if desired,
                // but keep all user messages like WhatsApp, Telegram, SMS, Gmail, etc.
                if (sbn.isOngoing) continue

                val pkg = sbn.packageName ?: continue
                val notif = sbn.notification ?: continue

                // Check notification badge count or increment
                val number = notif.number
                val increment = if (number > 0) number else 1
                counts[pkg] = (counts[pkg] ?: 0) + increment
            }

            _notificationCounts.value = counts
        } catch (_: Throwable) {
            _notificationCounts.value = emptyMap()
        }
    }
}
