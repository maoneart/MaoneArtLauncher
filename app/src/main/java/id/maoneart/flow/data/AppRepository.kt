package id.maoneart.flow.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import id.maoneart.flow.model.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val iconCache = ConcurrentHashMap<String, Drawable>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val collator = Collator.getInstance(Locale("id", "ID")).apply {
        strength = Collator.PRIMARY
    }

    /**
     * Emits the complete list of installed applications and updates automatically
     * whenever packages are installed, updated, or removed by the system.
     */
    val installedAppsFlow: Flow<List<AppInfo>> = callbackFlow {
        fun updateApps() {
            scope.launch(Dispatchers.IO) {
                try {
                    val apps = fetchInstalledApps()
                    trySend(apps)
                } catch (_: Throwable) {}
            }
        }

        // Initial emission
        updateApps()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.data?.schemeSpecificPart?.let { pkg ->
                    iconCache.remove(pkg)
                }
                updateApps()
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }

        context.registerReceiver(receiver, filter)

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Throwable) {}
        }
    }.flowOn(Dispatchers.IO)

    fun fetchInstalledApps(): List<AppInfo> {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(launcherIntent, 0)
        val selfPackage = context.packageName

        val result = mutableListOf<AppInfo>()

        for (info in resolveInfos) {
            val pkgName = info.activityInfo.packageName
            val actName = info.activityInfo.name

            // Exclude launcher self from app drawer list to keep view uncluttered
            if (pkgName == selfPackage) continue

            val label = try {
                info.loadLabel(packageManager).toString().trim()
            } catch (_: Throwable) {
                pkgName
            }

            val installTime = try {
                packageManager.getPackageInfo(pkgName, 0).firstInstallTime
            } catch (_: Throwable) {
                0L
            }

            // Cache icon lazily in background
            if (!iconCache.containsKey(pkgName)) {
                try {
                    val icon = info.loadIcon(packageManager)
                    iconCache[pkgName] = icon
                } catch (_: Throwable) {}
            }

            result.add(
                AppInfo(
                    packageName = pkgName,
                    activityName = actName,
                    label = label,
                    firstLetter = AppInfo.computeFirstLetter(label),
                    installTime = installTime
                )
            )
        }

        // Sort alphabetically using Indonesian locale collator
        return result.sortedWith { a, b ->
            val firstCompare = a.firstLetter.compareTo(b.firstLetter)
            if (firstCompare != 0) {
                // Symbols/numbers (#) at the end or top
                if (a.firstLetter == '#') 1
                else if (b.firstLetter == '#') -1
                else firstCompare
            } else {
                collator.compare(a.label, b.label)
            }
        }
    }

    fun getAppIcon(packageName: String): Drawable? {
        iconCache[packageName]?.let { return it }
        return try {
            val icon = packageManager.getApplicationIcon(packageName)
            iconCache[packageName] = icon
            icon
        } catch (_: Throwable) {
            null
        }
    }

    fun launchApp(app: AppInfo): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    }

    fun openAppDetails(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {}
    }

    fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {}
    }
}
