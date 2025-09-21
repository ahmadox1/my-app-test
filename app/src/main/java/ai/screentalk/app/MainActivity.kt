package ai.screentalk.app

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import ai.screentalk.app.permissions.PermissionUtils
import ai.screentalk.app.ui.ScreenTalkApp
import ai.screentalk.overlay.ChatHeadService
import ai.screentalk.screen.ScreenCaptureService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var captureBinder: ScreenCaptureService.ScreenCaptureBinder? = null
    private var captureJob: Job? = null
    private val capturing = MutableStateFlow(false)
    private val overlayGranted = MutableStateFlow(false)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayGranted.value = PermissionUtils.hasOverlayPermission(this)
        if (!PermissionUtils.hasNotificationPermission(this)) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val mediaProjectionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                captureBinder?.startProjection(result.resultCode, result.data!!)
            }
        }

        setContent {
            val capturingState by capturing.collectAsState()
            val overlayState by overlayGranted.collectAsState()
            androidx.compose.material3.MaterialTheme {
                ScreenTalkApp(
                capturing = capturingState,
                hasOverlay = overlayState,
                onRequestOverlay = {
                    startActivity(PermissionUtils.overlaySettingsIntent(this))
                },
                onStartBubble = { startChatHead() },
                onStartCapture = {
                    val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    val intent = manager.createScreenCaptureIntent()
                    ContextCompat.startForegroundService(
                        this,
                        Intent(this, ScreenCaptureService::class.java)
                    )
                    mediaProjectionLauncher.launch(intent)
                },
                onStopCapture = {
                    captureBinder?.stopProjection()
                }
            )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, ScreenCaptureService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onResume() {
        super.onResume()
        overlayGranted.value = PermissionUtils.hasOverlayPermission(this)
    }

    override fun onStop() {
        super.onStop()
        unbindSafely()
    }

    private fun unbindSafely() {
        try {
            unbindService(connection)
        } catch (_: IllegalArgumentException) {
        }
        captureJob?.cancel()
        captureJob = null
        captureBinder = null
    }

    private fun startChatHead() {
        if (!PermissionUtils.hasOverlayPermission(this)) {
            startActivity(PermissionUtils.overlaySettingsIntent(this))
            return
        }
        ContextCompat.startForegroundService(this, Intent(this, ChatHeadService::class.java))
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? ScreenCaptureService.ScreenCaptureBinder ?: return
            captureBinder = binder
            captureJob?.cancel()
            captureJob = lifecycleScope.launch {
                binder.isCapturing().collect { active ->
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
