package ai.screentalk.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class ScreenTalkApp : Application() {
    private val scope = CoroutineScope(SupervisorJob())

    override fun onTerminate() {
        super.onTerminate()
        scope.cancel()
    }
}
