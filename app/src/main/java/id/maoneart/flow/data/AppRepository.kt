package id.maoneart.flow.data

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import id.maoneart.flow.model.AppInfo
import id.maoneart.flow.model.DefaultCategories
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
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val collator = Collator.getInstance(Locale("id", "ID")).apply {
        strength = Collator.PRIMARY
    }

    companion object {
        @Volatile
        private var cachedApps: List<AppInfo>? = null
        private val iconCache = ConcurrentHashMap<String, Drawable>(256)

        fun getCachedApps(): List<AppInfo>? = cachedApps

        fun clearPackageIcon(packageName: String) {
            iconCache.remove(packageName)
        }
    }

    val installedAppsFlow: Flow<List<AppInfo>> = callbackFlow {
        // 1. Instant 0ms emission from hot memory cache
        cachedApps?.let { hotList ->
            trySend(hotList)
        }

        fun updateApps() {
            scope.launch(Dispatchers.IO) {
                try {
                    val apps = fetchInstalledApps()
                    cachedApps = apps
                    trySend(apps)
                } catch (_: Throwable) {}
            }
        }

        // Fresh background fetch
        updateApps()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.data?.schemeSpecificPart?.let { pkg ->
                    clearPackageIcon(pkg)
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

        try {
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (_: Throwable) {
            try {
                context.registerReceiver(receiver, filter)
            } catch (_: Throwable) {}
        }

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

        val result = ArrayList<AppInfo>(resolveInfos.size)

        for (info in resolveInfos) {
            val actInfo = info.activityInfo ?: continue
            val pkgName = actInfo.packageName ?: continue
            val actName = actInfo.name ?: continue

            val isSelf = (pkgName == selfPackage)
            val label = if (isSelf) {
                "MaoneArt Flow"
            } else {
                try {
                    info.loadLabel(packageManager).toString().trim().ifEmpty { pkgName }
                } catch (_: Throwable) {
                    pkgName
                }
            }

            val appInfo = actInfo.applicationInfo
            val categoryId = if (isSelf) "system" else resolveAppCategory(pkgName, label, appInfo)

            result.add(
                AppInfo(
                    packageName = pkgName,
                    activityName = actName,
                    label = label,
                    firstLetter = AppInfo.computeFirstLetter(label),
                    categoryId = categoryId,
                    categoryTitle = DefaultCategories.getTitle(categoryId),
                    categoryIcon = DefaultCategories.getIcon(categoryId)
                )
            )
        }

        // De-duplicate strictly by app.id (prevents Dual Apps / Cloned Apps from causing LazyColumn duplicate key crash)
        val distinctResult = result.distinctBy { it.id }

        // Sort alphabetically using Indonesian locale collator
        val sortedResult = distinctResult.sortedWith { a, b ->
            val firstCompare = a.firstLetter.compareTo(b.firstLetter)
            if (firstCompare != 0) {
                if (a.firstLetter == '#') 1
                else if (b.firstLetter == '#') -1
                else firstCompare
            } else {
                collator.compare(a.label, b.label)
            }
        }

        cachedApps = sortedResult

        // Background non-blocking pre-warming of icon cache
        scope.launch(Dispatchers.IO) {
            for (app in sortedResult) {
                if (!iconCache.containsKey(app.packageName)) {
                    try {
                        val icon = packageManager.getApplicationIcon(app.packageName)
                        iconCache[app.packageName] = icon
                    } catch (_: Throwable) {}
                }
            }
        }

        return sortedResult
    }

    private fun resolveAppCategory(pkgName: String, label: String, appInfo: ApplicationInfo?): String {
        val pkg = pkgName.lowercase()
        val lbl = label.lowercase()

        // 1. Game check
        if (appInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appInfo.category == ApplicationInfo.CATEGORY_GAME) return "games"
        }
        if (pkg.contains("game") || lbl.contains("game") || pkg.contains("mlbb") ||
            pkg.contains("pubg") || pkg.contains("freefire") || pkg.contains("roblox") ||
            pkg.contains("genshin") || pkg.contains("arcade") || pkg.contains("simulator")) {
            return "games"
        }

        // 2. Social / Chat
        if (appInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appInfo.category == ApplicationInfo.CATEGORY_SOCIAL) return "social"
        }
        if (pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("instagram") ||
            pkg.contains("facebook") || pkg.contains("twitter") || pkg.contains("tiktok") ||
            pkg.contains("discord") || pkg.contains("chat") || pkg.contains("message") ||
            pkg.contains("pesan") || pkg.contains("line") || lbl.contains("chat") ||
            lbl.contains("sosial") || lbl.contains("pesan")) {
            return "social"
        }

        // 3. Media & Audio & Video & Foto
        if (appInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appInfo.category == ApplicationInfo.CATEGORY_AUDIO ||
                appInfo.category == ApplicationInfo.CATEGORY_VIDEO ||
                appInfo.category == ApplicationInfo.CATEGORY_IMAGE) {
                return "media"
            }
        }
        if (pkg.contains("music") || pkg.contains("youtube") || pkg.contains("spotify") ||
            pkg.contains("video") || pkg.contains("media") || pkg.contains("player") ||
            pkg.contains("camera") || pkg.contains("kamera") || pkg.contains("gallery") ||
            pkg.contains("galeri") || pkg.contains("foto") || pkg.contains("photo") ||
            pkg.contains("vlc") || pkg.contains("netflix") || lbl.contains("musik") ||
            lbl.contains("video") || lbl.contains("kamera") || lbl.contains("galeri")) {
            return "media"
        }

        // 4. System & Phone
        if (pkg.contains("settings") || pkg.contains("setelan") || pkg.contains("dialer") ||
            pkg.contains("phone") || pkg.contains("telepon") || pkg.contains("contact") ||
            pkg.contains("kontak") || pkg.contains("vending") || pkg.contains("playstore") ||
            lbl.contains("setelan") || lbl.contains("pengaturan") || lbl.contains("telepon") ||
            lbl.contains("kontak")) {
            return "system"
        }

        // 5. Productivity & Tools default
        return "productivity"
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
        // 1. Try launching with explicit ComponentName (accurate for apps with multiple launcher activities)
        if (app.activityName.isNotBlank()) {
            try {
                val component = ComponentName(app.packageName, app.activityName)
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    this.component = component
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }
                context.startActivity(intent)
                return true
            } catch (_: Throwable) {}
        }

        // 2. Fallback to package launcher intent
        return try {
            val intent = packageManager.getLaunchIntentForPackage(app.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            if (intent != null) {
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
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {}
    }

    fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Throwable) {}
    }
}
