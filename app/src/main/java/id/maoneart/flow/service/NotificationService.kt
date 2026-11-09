package id.maoneart.flow.service

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveNotification(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val postTime: Long,
    val contentIntent: PendingIntent? = null,
    val replyAction: Notification.Action? = null,
    val markReadAction: Notification.Action? = null
) {
    val canReply: Boolean get() = replyAction != null && !replyAction.remoteInputs.isNullOrEmpty()
}

class NotificationService : NotificationListenerService() {

    companion object {
        private val _notificationCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
        val notificationCounts: StateFlow<Map<String, Int>> = _notificationCounts.asStateFlow()

        private val _activeNotificationList = MutableStateFlow<List<ActiveNotification>>(emptyList())
        val activeNotificationList: StateFlow<List<ActiveNotification>> = _activeNotificationList.asStateFlow()

        var currentInstance: NotificationService? = null
            private set

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

        /**
         * Sends an inline Direct Reply (e.g. WhatsApp, Telegram, SMS, Instagram)
         * using Android's official RemoteInput + PendingIntent mechanism (Niagara style).
         */
        fun sendDirectReply(
            context: Context,
            notification: ActiveNotification,
            replyText: String
        ): Boolean {
            val trimmed = replyText.trim()
            if (trimmed.isEmpty()) return false
            val action = notification.replyAction ?: return false
            val remoteInputs = action.remoteInputs ?: return false
            if (remoteInputs.isEmpty()) return false
            val actionIntent = action.actionIntent ?: return false

            return try {
                val localIntent = Intent().addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                val resultsBundle = Bundle()
                for (remoteInput in remoteInputs) {
                    resultsBundle.putCharSequence(remoteInput.resultKey, trimmed)
                }
                RemoteInput.addResultsToIntent(remoteInputs, localIntent, resultsBundle)
                actionIntent.send(context, 0, localIntent)

                // Optionally trigger Mark as Read action if available so badge clears cleanly
                try {
                    notification.markReadAction?.actionIntent?.send()
                } catch (_: Throwable) {}

                true
            } catch (_: Throwable) {
                false
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        currentInstance = this
        isServiceConnected = true
        updateActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        currentInstance = null
        isServiceConnected = false
        _notificationCounts.value = emptyMap()
        _activeNotificationList.value = emptyList()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        updateActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateActiveNotifications()
    }

    fun dismissNotification(key: String) {
        try {
            cancelNotification(key)
            updateActiveNotifications()
        } catch (_: Throwable) {}
    }

    fun clearAllUserNotifications() {
        try {
            cancelAllNotifications()
            updateActiveNotifications()
        } catch (_: Throwable) {}
    }

    private fun updateActiveNotifications() {
        try {
            val active = activeNotifications
            if (active == null || active.isEmpty()) {
                _notificationCounts.value = emptyMap()
                _activeNotificationList.value = emptyList()
                return
            }

            val counts = mutableMapOf<String, Int>()
            val notifs = mutableListOf<ActiveNotification>()

            for (sbn in active) {
                if (sbn.isOngoing) continue

                val pkg = sbn.packageName ?: continue
                val notif = sbn.notification ?: continue

                // Skip empty group summary headers when individual chat notifications exist
                val isGroupSummary = (notif.flags and Notification.FLAG_GROUP_SUMMARY) != 0

                val extras = notif.extras
                val convTitle = extras?.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()?.trim()
                val rawTitle = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
                val title = if (!convTitle.isNullOrEmpty() && !rawTitle.startsWith(convTitle)) {
                    "$convTitle • $rawTitle"
                } else {
                    rawTitle
                }

                val text = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()
                    ?: extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
                    ?: ""

                // Extract Direct Reply Action (RemoteInput) & Mark-as-Read Action
                var replyAct: Notification.Action? = null
                var readAct: Notification.Action? = null
                notif.actions?.forEach { act ->
                    val inputs = act.remoteInputs
                    if (inputs != null && inputs.isNotEmpty() && act.actionIntent != null && replyAct == null) {
                        replyAct = act
                    } else if (inputs.isNullOrEmpty() && act.actionIntent != null) {
                        val label = act.title?.toString()?.lowercase() ?: ""
                        if (label.contains("read") || label.contains("baca") || label.contains("tandai")) {
                            readAct = act
                        }
                    }
                }

                if (!isGroupSummary || replyAct != null) {
                    val number = notif.number
                    val increment = if (number > 0) number else 1
                    counts[pkg] = (counts[pkg] ?: 0) + increment
                }

                if ((title.isNotEmpty() || text.isNotEmpty()) && (!isGroupSummary || replyAct != null)) {
                    notifs.add(
                        ActiveNotification(
                            key = sbn.key,
                            packageName = pkg,
                            title = title,
                            text = text,
                            postTime = sbn.postTime,
                            contentIntent = notif.contentIntent,
                            replyAction = replyAct,
                            markReadAction = readAct
                        )
                    )
                }
            }

            // Fallback count if an app only posted a group summary
            if (counts.isEmpty()) {
                for (sbn in active) {
                    if (sbn.isOngoing) continue
                    val pkg = sbn.packageName ?: continue
                    counts[pkg] = (counts[pkg] ?: 0) + 1
                }
            }

            _notificationCounts.value = counts
            // Prioritize notifications that support Direct Reply and most recent postTime
            _activeNotificationList.value = notifs
                .sortedWith(compareByDescending<ActiveNotification> { it.canReply }.thenByDescending { it.postTime })
                .take(30)
        } catch (_: Throwable) {
            _notificationCounts.value = emptyMap()
            _activeNotificationList.value = emptyList()
        }
    }
}
