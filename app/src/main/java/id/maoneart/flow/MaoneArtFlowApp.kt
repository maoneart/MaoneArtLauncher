package id.maoneart.flow

import android.app.Application
import id.maoneart.flow.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MaoneArtFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Hot pre-warm apps cache in background on process creation
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppRepository(this@MaoneArtFlowApp).fetchInstalledApps()
            } catch (_: Throwable) {}
        }
    }
}
