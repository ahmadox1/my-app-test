package com.ahmadox.smartcoach.service

import android.app.*
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
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.ahmadox.smartcoach.R
import com.ahmadox.smartcoach.vision.GameVisionAnalyzer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import javax.inject.Inject

/**
 * Service that handles screen capture using MediaProjection API.
 * 
 * This service captures the screen when requested and processes the images
 * for game analysis.
 */
@AndroidEntryPoint
class ScreenCaptureService : Service() {
    
    companion object {
        private const val TAG = "ScreenCaptureService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "screen_capture_channel"
        
        const val ACTION_START_CAPTURE = "start_capture"
        const val ACTION_STOP_CAPTURE = "stop_capture"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
    }
    
    @Inject
    lateinit var visionAnalyzer: GameVisionAnalyzer
    
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    
    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 0
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        
        // Get screen metrics
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        }
        
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        screenDensity = displayMetrics.densityDpi
        
        Log.d(TAG, "Screen: ${screenWidth}x$screenHeight @ $screenDensity dpi")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CAPTURE -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
                
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    startScreenCapture(resultCode, resultData)
                } else {
                    Log.e(TAG, "Invalid screen capture permission")
                    stopSelf()
                }
            }
            ACTION_STOP_CAPTURE -> {
                stopScreenCapture()
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopScreenCapture()
        serviceScope.cancel()
    }
    
    private fun startScreenCapture(resultCode: Int, resultData: Intent) {
        try {
            // Create notification channel
            createNotificationChannel()
            
            // Start foreground service
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
            
            // Initialize MediaProjection
            mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, resultData)
            
            // Set up image reader
            imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)
            imageReader?.setOnImageAvailableListener(imageAvailableListener, null)
            
            // Create virtual display
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "SmartCoachCapture",
                screenWidth,
                screenHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                null
            )
            
            Log.d(TAG, "Screen capture started successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start screen capture", e)
            stopSelf()
        }
    }
    
    private fun stopScreenCapture() {
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            
            imageReader?.close()
            imageReader = null
            
            mediaProjection?.stop()
            mediaProjection = null
            
            stopForeground(true)
            Log.d(TAG, "Screen capture stopped")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping screen capture", e)
        }
    }
    
    private val imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        try {
            val image = reader.acquireLatestImage()
            image?.let { processImage(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
        }
    }
    
    private fun processImage(image: Image) {
        serviceScope.launch(Dispatchers.Default) {
            try {
                val bitmap = imageToBitmap(image)
                image.close()
                
                if (bitmap != null) {
                    // Analyze the screen capture
                    val visionResult = visionAnalyzer.analyzeGameScreen(bitmap)
                    
                    // Send results to main app
                    val intent = Intent("com.ahmadox.smartcoach.SCREEN_ANALYZED").apply {
                        putExtra("analysis_success", visionResult.success)
                        putExtra("my_elixir", visionResult.myElixir)
                        putExtra("opp_elixir", visionResult.oppElixir)
                        putExtra("raw_text", visionResult.rawText)
                        putExtra("confidence", visionResult.confidence)
                        putExtra("timestamp", System.currentTimeMillis())
                    }
                    sendBroadcast(intent)
                    
                    Log.d(TAG, "Screen analyzed - Elixir: ${visionResult.myElixir}/${visionResult.oppElixir}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing screen capture", e)
            }
        }
    }
    
    private fun imageToBitmap(image: Image): Bitmap? {
        try {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * screenWidth
            
            val bitmap = Bitmap.createBitmap(
                screenWidth + rowPadding / pixelStride,
                screenHeight,
                Bitmap.Config.ARGB_8888
            )
            
            bitmap.copyPixelsFromBuffer(buffer)
            
            return if (rowPadding == 0) {
                bitmap
            } else {
                // Crop bitmap if there's row padding
                Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error converting image to bitmap", e)
            return null
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Capture",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Smart Coach screen capture service"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val stopIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ACTION_STOP_CAPTURE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Smart Coach")
            .setContentText("جاري تحليل الشاشة...")
            .setSmallIcon(R.drawable.ic_launcher)
            .addAction(
                R.drawable.ic_launcher,
                "إيقاف",
                stopPendingIntent
            )
            .setOngoing(true)
            .setShowWhen(false)
            .build()
    }
}