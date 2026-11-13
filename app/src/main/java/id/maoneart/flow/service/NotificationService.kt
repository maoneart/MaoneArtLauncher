package id.maoneart.flow.service

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MediaTrackInfo(
    val isPlaying: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val packageName: String = "",
    val appName: String = "",
    val artwork: Bitmap? = null,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val hasActiveSession: Boolean = false
)

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

        private val _currentMediaTrack = MutableStateFlow(MediaTrackInfo())
        val currentMediaTrack: StateFlow<MediaTrackInfo> = _currentMediaTrack.asStateFlow()

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

        fun togglePlayPause(): Boolean {
            return try {
                val controller = currentInstance?.getActiveController() ?: return false
                val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
                if (isPlaying) {
                    controller.transportControls?.pause()
                } else {
                    controller.transportControls?.play()
                }
                true
            } catch (_: Throwable) {
                false
            }
        }

        fun skipToNext(): Boolean {
            return try {
                val controller = currentInstance?.getActiveController() ?: return false
                controller.transportControls?.skipToNext()
                true
            } catch (_: Throwable) {
                false
            }
        }

        fun skipToPrevious(): Boolean {
            return try {
                val controller = currentInstance?.getActiveController() ?: return false
                controller.transportControls?.skipToPrevious()
                true
            } catch (_: Throwable) {
                false
            }
        }

        fun openPlayerApp(context: Context): Boolean {
            return try {
                val controller = currentInstance?.getActiveController()
                val sessionActivity = controller?.sessionActivity
                if (sessionActivity != null) {
                    try {
                        sessionActivity.send()
                        return true
                    } catch (_: Throwable) {}
                }
                val pkg = controller?.packageName
                if (!pkg.isNullOrEmpty()) {
                    val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        return true
                    }
                }
                false
            } catch (_: Throwable) {
                false
            }
        }
    }

    private var mediaSessionManager: MediaSessionManager? = null
    private var activeMediaController: MediaController? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val mediaCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateMediaTrack()
        }
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateMediaTrack()
        }
        override fun onSessionDestroyed() {
            activeMediaController = null
            findAndBindActiveMediaSession()
        }
    }

    private val sessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { _ ->
        findAndBindActiveMediaSession()
    }

    fun getActiveController(): MediaController? {
        if (activeMediaController == null) {
            findAndBindActiveMediaSession()
        }
        return activeMediaController
    }

    fun findAndBindActiveMediaSession() {
        try {
            if (!isServiceConnected) {
                _currentMediaTrack.value = MediaTrackInfo()
                return
            }
            val mgr = mediaSessionManager ?: (getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager)
            mediaSessionManager = mgr
            if (mgr == null) {
                _currentMediaTrack.value = MediaTrackInfo()
                return
            }

            val component = ComponentName(this, NotificationService::class.java)
            val controllers = try {
                mgr.getActiveSessions(component)
            } catch (_: Throwable) {
                null
            }

            val targetController = controllers?.firstOrNull { controller ->
                val state = controller.playbackState?.state
                state == PlaybackState.STATE_PLAYING
            } ?: controllers?.firstOrNull { controller ->
                val state = controller.playbackState?.state
                state == PlaybackState.STATE_PAUSED || state == PlaybackState.STATE_BUFFERING
            } ?: controllers?.firstOrNull()

            if (targetController != null) {
                if (targetController != activeMediaController) {
                    try {
                        activeMediaController?.unregisterCallback(mediaCallback)
                    } catch (_: Throwable) {}
                    activeMediaController = targetController
                    try {
                        activeMediaController?.registerCallback(mediaCallback, mainHandler)
                    } catch (_: Throwable) {}
                }
                updateMediaTrack()
                return
            }

            // Fallback: check active notifications for EXTRA_MEDIA_SESSION token
            val active = try { activeNotifications } catch (_: Throwable) { null }
            if (active != null) {
                for (sbn in active) {
                    val extras = sbn.notification?.extras ?: continue
                    val token = extras.getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION)
                    if (token != null) {
                        try {
                            val controller = MediaController(this, token)
                            if (controller != activeMediaController) {
                                try {
                                    activeMediaController?.unregisterCallback(mediaCallback)
                                } catch (_: Throwable) {}
                                activeMediaController = controller
                                try {
                                    activeMediaController?.registerCallback(mediaCallback, mainHandler)
                                } catch (_: Throwable) {}
                            }
                            updateMediaTrack()
                            return
                        } catch (_: Throwable) {}
                    }
                }
            }

            if (activeMediaController != null) {
                try {
                    activeMediaController?.unregisterCallback(mediaCallback)
                } catch (_: Throwable) {}
                activeMediaController = null
            }
            _currentMediaTrack.value = MediaTrackInfo()
        } catch (_: Throwable) {
            _currentMediaTrack.value = MediaTrackInfo()
        }
    }

    private fun updateMediaTrack() {
        try {
            val controller = activeMediaController
            if (controller == null) {
                _currentMediaTrack.value = MediaTrackInfo()
                return
            }

            val state = controller.playbackState
            val playbackStateVal = state?.state ?: PlaybackState.STATE_NONE
            val isPlaying = playbackStateVal == PlaybackState.STATE_PLAYING
            val isPaused = playbackStateVal == PlaybackState.STATE_PAUSED
            val isBuffering = playbackStateVal == PlaybackState.STATE_BUFFERING

            if (playbackStateVal == PlaybackState.STATE_STOPPED || 
                playbackStateVal == PlaybackState.STATE_NONE || 
                playbackStateVal == PlaybackState.STATE_ERROR) {
                _currentMediaTrack.value = MediaTrackInfo()
                return
            }

            val metadata = controller.metadata
            val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: metadata?.getText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)?.toString()
                ?: ""

            val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?: metadata?.getText(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)?.toString()
                ?: ""

            val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""

            val artwork = try {
                metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                    ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            } catch (_: Throwable) {
                null
            }

            val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
            val position = state?.position ?: 0L

            val pkg = controller.packageName ?: ""
            val appName = try {
                val pm = packageManager
                val appInfo = pm.getApplicationInfo(pkg, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Throwable) {
                pkg
            }

            if (title.isNotEmpty() || isPlaying || isPaused || isBuffering) {
                _currentMediaTrack.value = MediaTrackInfo(
                    isPlaying = isPlaying,
                    title = title.ifEmpty { "Memutar Audio" },
                    artist = artist.ifEmpty { appName },
                    album = album,
                    packageName = pkg,
                    appName = appName,
                    artwork = artwork,
                    durationMs = duration.coerceAtLeast(0L),
                    positionMs = position.coerceAtLeast(0L),
                    hasActiveSession = true
                )
            } else {
                _currentMediaTrack.value = MediaTrackInfo()
            }
        } catch (_: Throwable) {
            _currentMediaTrack.value = MediaTrackInfo()
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        currentInstance = this
        isServiceConnected = true
        updateActiveNotifications()

        try {
            val mgr = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            mediaSessionManager = mgr
            val component = ComponentName(this, NotificationService::class.java)
            mgr?.addOnActiveSessionsChangedListener(sessionsChangedListener, component, mainHandler)
            findAndBindActiveMediaSession()
        } catch (_: Throwable) {}
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsChangedListener)
            activeMediaController?.unregisterCallback(mediaCallback)
        } catch (_: Throwable) {}
        activeMediaController = null
        mediaSessionManager = null
        currentInstance = null
        isServiceConnected = false
        _notificationCounts.value = emptyMap()
        _activeNotificationList.value = emptyList()
        _currentMediaTrack.value = MediaTrackInfo()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        updateActiveNotifications()
        findAndBindActiveMediaSession()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        updateActiveNotifications()
        findAndBindActiveMediaSession()
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
                    if (act == null) return@forEach
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
