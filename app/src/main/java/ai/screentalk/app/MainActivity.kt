package ai.screentalk.app

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import ai.screentalk.app.permissions.PermissionUtils
import ai.screentalk.app.ui.ScreenTalkApp
import ai.screentalk.overlay.ChatHeadService
import ai.screentalk.screen.ScreenCaptureService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val overlayGranted = MutableStateFlow(false)
    private val capturing = MutableStateFlow(false)
    private var captureBinder: ScreenCaptureService.ScreenCaptureBinder? = null
    private var captureJob: Job? = null

    private val mediaProjectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                captureBinder?.startProjection(result.resultCode, result.data!!)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayGranted.value = PermissionUtils.hasOverlayPermission(this)

        setContent {
            val hasOverlay by overlayGranted.collectAsStateWithLifecycle()
            val isCapturing by capturing.collectAsStateWithLifecycle()
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ScreenTalkApp(
                        hasOverlayPermission = hasOverlay,
                        isCapturing = isCapturing,
                        onRequestOverlay = { startActivity(PermissionUtils.overlaySettingsIntent(this)) },
                        onStartBubble = { startChatHead() },
                        onStartCapture = { requestScreenCapture() },
                        onStopCapture = { stopScreenCapture() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            overlayGranted.value = PermissionUtils.hasOverlayPermission(this@MainActivity)
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, ScreenCaptureService::class.java)
        bindService(intent, captureConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindCapture()
    }

    private fun startChatHead() {
        lifecycleScope.launch {
            if (!PermissionUtils.hasOverlayPermission(this@MainActivity)) {
                startActivity(PermissionUtils.overlaySettingsIntent(this@MainActivity))
                return@launch
            }
            val intent = Intent(this@MainActivity, ChatHeadService::class.java)
            ContextCompat.startForegroundService(this@MainActivity, intent)
        }
    }

    private fun requestScreenCapture() {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as android.media.projection.MediaProjectionManager
        val intent = manager.createScreenCaptureIntent()
        ContextCompat.startForegroundService(this, Intent(this, ScreenCaptureService::class.java))
        mediaProjectionLauncher.launch(intent)
    }

    private fun stopScreenCapture() {
        captureBinder?.stopProjection()
    }

    private fun unbindCapture() {
        try {
            unbindService(captureConnection)
        } catch (_: IllegalArgumentException) {
        }
        captureJob?.cancel()
        captureJob = null
        captureBinder = null
    }

    private val captureConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? ScreenCaptureService.ScreenCaptureBinder ?: return
            captureBinder = binder
            captureJob?.cancel()
            captureJob = lifecycleScope.launch {
                binder.capturing().collect { active ->
                    capturing.value = active
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            captureJob?.cancel()
            captureJob = null
            captureBinder = null
            capturing.value = false
        }
    }
}
