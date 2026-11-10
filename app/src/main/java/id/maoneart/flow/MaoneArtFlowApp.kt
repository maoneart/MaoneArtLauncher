package id.maoneart.flow

import android.app.Application
import android.content.Intent
import id.maoneart.flow.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MaoneArtFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Install bulletproof Global Crash Handler (logs & safe graceful recovery)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val logDir = getExternalFilesDir(null) ?: filesDir
                val crashFile = File(logDir, "launcher_crash.log")
                PrintWriter(FileWriter(crashFile, true)).use { pw ->
                    pw.println("=== Crash at $timestamp on thread ${thread.name} ===")
                    throwable.printStackTrace(pw)
                    pw.println("===================================================\n")
                }
            } catch (_: Throwable) {}

            try {
                // Gracefully re-launch MainActivity without showing system crash dialog
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(10)
            } catch (_: Throwable) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        // 2. Hot pre-warm apps cache in background on process creation
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppRepository(this@MaoneArtFlowApp).fetchInstalledApps()
            } catch (_: Throwable) {}
        }
    }
}
