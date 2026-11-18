package id.maoneart.flow.data

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import id.maoneart.flow.model.FlowShortcutItem

/**
 * Manages querying and launching Android App Shortcuts via LauncherApps API.
 */
object AppShortcutManager {

    /**
     * Checks if the app currently has permission to query and launch shortcuts.
     * (Returns true if app is declared as HOME launcher or set as default launcher).
     */
    fun hasShortcutPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return false
        return try {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            launcherApps?.hasShortcutHostPermission() == true
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Queries all dynamic, manifest, and pinned shortcuts for a specific application package.
     */
    fun getShortcutsForApp(context: Context, packageName: String): List<FlowShortcutItem> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return emptyList()
        return try {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return emptyList()
            if (!launcherApps.hasShortcutHostPermission()) return emptyList()

            val query = LauncherApps.ShortcutQuery().apply {
                setPackage(packageName)
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                )
            }
            val shortcuts: List<ShortcutInfo> = launcherApps.getShortcuts(query, Process.myUserHandle()) ?: emptyList()
            val density = context.resources.displayMetrics.densityDpi

            shortcuts.map { shortcut ->
                val iconDrawable: Drawable? = try {
                    launcherApps.getShortcutIconDrawable(shortcut, density)
                } catch (_: Throwable) {
                    null
                }
                val label = shortcut.shortLabel?.toString()
                    ?: shortcut.longLabel?.toString()
                    ?: shortcut.id

                FlowShortcutItem(
                    id = shortcut.id,
                    packageName = shortcut.`package`,
                    shortLabel = label,
                    longLabel = shortcut.longLabel?.toString(),
                    icon = iconDrawable,
                    isDynamic = shortcut.isDynamic,
                    isManifest = shortcut.isDeclaredInManifest
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    /**
     * Launch a specific shortcut using LauncherApps.
     */
    fun launchShortcut(context: Context, packageName: String, shortcutId: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return false
        return try {
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return false
            launcherApps.startShortcut(packageName, shortcutId, null as? android.graphics.Rect, null as? android.os.Bundle, Process.myUserHandle())
            true
        } catch (_: Throwable) {
            false
        }
    }
}
