package ai.screentalk.screen

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ai.screentalk.common.Logger
import ai.screentalk.screen.ocr.MLKitOcrEngine
import ai.screentalk.screen.ocr.OcrEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class ScreenCaptureService : Service() {

    inner class ScreenCaptureBinder : Binder() {
        fun startProjection(resultCode: Int, data: Intent) = this@ScreenCaptureService.startProjection(resultCode, data)
        fun stopProjection() = this@ScreenCaptureService.stopProjection()
        fun isCapturing() = capturing
    }

    private val serviceScope = CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.Default)
    private val binder = ScreenCaptureBinder()
    private val ocrEngine: OcrEngine by lazy { MLKitOcrEngine(this) }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var captureJob: Job? = null

    private val capturing = kotlinx.coroutines.flow.MutableStateFlow(false)

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Waiting for capture"))
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProjection()
        serviceScope.cancel()
    }

    private fun startProjection(resultCode: Int, data: Intent) {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = manager.getMediaProjection(resultCode, data)
        setupVirtualDisplay()
        capturing.value = true
        captureJob?.cancel()
        captureJob = serviceScope.launch {
            while (isActive) {
                imageReader?.acquireLatestImage()?.use { image ->
                    val bitmap = image.toBitmap()
                    if (bitmap != null) {
                        try {
                            runCatching {
                                ocrEngine.extractText(bitmap)
                            }.onSuccess { text ->
                                ScreenContextBuilder.updateOcr(text)
                            }.onFailure { error ->
                                Logger.e("OCR failed", error)
                            }
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }
                delay(CAPTURE_INTERVAL_MS)
            }
        }
    }

    private fun stopProjection() {
        capturing.value = false
        captureJob?.cancel()
        captureJob = null
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
        mediaProjection?.stop()
        mediaProjection = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun setupVirtualDisplay() {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "screen_talk_capture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            imageReader?.surface,
            null,
            null
        )
    }

    private fun buildNotification(content: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ScreenTalk")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()

    private fun Image.toBitmap(): Bitmap? {
        if (format != PixelFormat.RGBA_8888) return null
        return try {
            val planes = planes
            if (planes.isEmpty()) return null
            val buffer = planes[0].buffer
            buffer.rewind()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)
            bitmap
        } catch (t: Throwable) {
            Logger.e("Failed to convert image", t)
            null
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Screen capture",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_ID = 42
        private const val CHANNEL_ID = "screen_capture"
        private const val CAPTURE_INTERVAL_MS = 1_500L
    }

}

private fun Image.use(block: (Image) -> Unit) {
    try {
        block(this)
    } finally {
        close()
    }
}
