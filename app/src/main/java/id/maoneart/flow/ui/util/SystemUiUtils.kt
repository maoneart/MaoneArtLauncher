package id.maoneart.flow.ui.util

import android.annotation.SuppressLint
import android.content.Context
import java.lang.reflect.Method

/**
 * Utility for expanding system status bar and quick settings panels programmatically
 * for gesture-based invocation.
 */
object SystemUiUtils {

    /**
     * Expand the notification shade (Status bar notifications panel)
     */
    @SuppressLint("WrongConstant")
    fun expandNotifications(context: Context): Boolean {
        return try {
            val statusBarService = context.getSystemService("statusbar") ?: return false
            val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
            val method: Method = try {
                statusBarManagerClass.getMethod("expandNotificationsPanel")
            } catch (_: NoSuchMethodException) {
                statusBarManagerClass.getMethod("expand")
            }
            method.isAccessible = true
            method.invoke(statusBarService)
            true
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Expand the Quick Settings panel (Control Center / System UI tiles)
     */
    @SuppressLint("WrongConstant")
    fun expandQuickSettings(context: Context): Boolean {
        return try {
            val statusBarService = context.getSystemService("statusbar") ?: return false
            val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
            val method: Method = try {
                statusBarManagerClass.getMethod("expandSettingsPanel")
            } catch (_: NoSuchMethodException) {
                try {
                    statusBarManagerClass.getMethod("expandNotificationsPanel")
                } catch (_: NoSuchMethodException) {
                    statusBarManagerClass.getMethod("expand")
                }
            }
            method.isAccessible = true
            method.invoke(statusBarService)
            true
        } catch (_: Throwable) {
            expandNotifications(context)
        }
    }
}
