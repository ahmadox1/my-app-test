package ai.screentalk.screen

import android.app.Notification
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
import ai.screentalk.screen.ocr.HybridOcrEngine
import ai.screentalk.screen.ocr.MLKitOcrEngine
import ai.screentalk.screen.ocr.OcrEngine
import ai.screentalk.screen.ocr.TessOcrEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ScreenCaptureService : Service() {

    inner class ScreenCaptureBinder : Binder() {
        fun startProjection(resultCode: Int, data: Intent) = startProjectionInternal(resultCode, data)
        fun stopProjection() = stopProjectionInternal()
        fun capturing(): StateFlow<Boolean> = capturing
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Default)
    private val binder = ScreenCaptureBinder()

    private val capturing = MutableStateFlow(false)
    private val ocrEngine: OcrEngine by lazy {
        HybridOcrEngine(
            primary = MLKitOcrEngine(this),
            fallback = TessOcrEngine(this)
        )
    }

    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var captureJob: Job? = null
    private var lastOcrText: String = ""

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
        stopProjectionInternal()
        serviceJob.cancel()
    }

    private fun startProjectionInternal(resultCode: Int, data: Intent) {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = manager.getMediaProjection(resultCode, data)
        setupVirtualDisplay()
        capturing.value = true
        captureJob?.cancel()
        captureJob = serviceScope.launch {
            while (isActive) {
                imageReader?.acquireLatestImage()?.use { image ->
                    val bitmap = image.toBitmap() ?: return@use
                    val text = runCatching { ocrEngine.extractText(bitmap, null) }
                        .onFailure { Logger.e("OCR failed", it) }
                        .getOrDefault("")
                        .trim()
                    if (text.isNotBlank() && text != lastOcrText) {
                        lastOcrText = text
                        ScreenContextBuilder.updateOcr(text)
                    }
                    bitmap.recycle()
                }
                delay(CAPTURE_INTERVAL_MS)
            }
        }
    }

    private fun stopProjectionInternal() {
        capturing.value = false
        captureJob?.cancel()
        captureJob = null
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
        projection?.stop()
        projection = null
        lastOcrText = ""
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
        virtualDisplay = projection?.createVirtualDisplay(
            "screentalk_capture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            imageReader?.surface,
            null,
            null
        )
    }

    private fun buildNotification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ScreenTalk capture")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()

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

    private fun Image.toBitmap(): Bitmap? {
        return try {
            val plane = planes.firstOrNull() ?: return null
            val buffer = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * width
            val paddedBitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride,
                height,
                Bitmap.Config.ARGB_8888
            )
            paddedBitmap.copyPixelsFromBuffer(buffer)
            val cropped = Bitmap.createBitmap(paddedBitmap, 0, 0, width, height)
            paddedBitmap.recycle()
            cropped
        } catch (t: Throwable) {
            Logger.e("Failed to copy screen image", t)
            null
        }
    }

    private inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
        var throwable: Throwable? = null
        try {
            return block(this)
        } catch (t: Throwable) {
            throwable = t
            throw t
        } finally {
            try {
                this?.close()
            } catch (close: Throwable) {
                if (throwable != null) {
                    throwable.addSuppressed(close)
                } else {
                    throw close
                }
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "screen_capture"
        private const val NOTIFICATION_ID = 42
        private const val CAPTURE_INTERVAL_MS = 1_500L
    }
}
