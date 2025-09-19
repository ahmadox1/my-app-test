package com.smartassistant

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.smartassistant.ai.ModelDownloadManager
import com.smartassistant.overlay.BubbleOverlayService
import com.smartassistant.overlay.NotificationTipManager
import com.smartassistant.services.GameAnalysisService
import com.smartassistant.services.ScreenCaptureService

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_SCREEN_CAPTURE = 1000
        private const val REQUEST_OVERLAY_PERMISSION = 1001
    }

    private lateinit var btnStartService: Button
    private lateinit var btnStopService: Button
    private lateinit var btnSettings: Button
    private lateinit var btnDownloadModels: Button
    private lateinit var statusText: TextView
    private lateinit var detectedGame: TextView
    private lateinit var suggestionText: TextView
    private lateinit var downloadProgress: TextView
    private lateinit var suggestionsCard: CardView

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var isServiceRunning = false
    private lateinit var modelDownloadManager: ModelDownloadManager
    private lateinit var notificationTipManager: NotificationTipManager

    // Activity result launcher for screen capture permission
    private val screenCaptureResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                startScreenCaptureService(result.resultCode, data)
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "يجب منح صلاحية التقاط الشاشة",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()

        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        // Initialize AI model download manager
        initializeModelDownloader()

        // Initialize notification tip manager
        notificationTipManager = NotificationTipManager(this)
    }

    private fun initViews() {
        btnStartService = findViewById(R.id.btn_start_service)
        btnStopService = findViewById(R.id.btn_stop_service)
        btnSettings = findViewById(R.id.btn_settings)
        btnDownloadModels = findViewById(R.id.btn_download_models)

        statusText = findViewById(R.id.status_text)
        detectedGame = findViewById(R.id.detected_game)
        suggestionText = findViewById(R.id.suggestion_text)
        suggestionsCard = findViewById(R.id.suggestions_card)
        downloadProgress = findViewById(R.id.download_progress)
    }

    private fun setupClickListeners() {
        btnStartService.setOnClickListener {
            if (checkPermissions()) {
                requestScreenCapture()
            }
        }

        btnStopService.setOnClickListener {
            stopServices()
        }

        btnSettings.setOnClickListener {
            openAccessibilitySettings()
        }

        btnDownloadModels.setOnClickListener {
            downloadAIModels()
        }
    }

    private fun initializeModelDownloader() {
        modelDownloadManager = ModelDownloadManager(this)
        modelDownloadManager.setProgressListener(object : ModelDownloadManager.DownloadProgressListener {
            override fun onDownloadStarted(modelName: String) {
                runOnUiThread {
                    downloadProgress.text = "بدء تحميل ${getModelDisplayName(modelName)}..."
                    btnDownloadModels.isEnabled = false
                }
            }

            override fun onDownloadProgress(
                modelName: String,
                progress: Int,
                downloadedBytes: Long,
                totalBytes: Long
            ) {
                runOnUiThread {
                    val progressText = String.format(
                        "تحميل %s: %d%% (%s / %s)",
                        getModelDisplayName(modelName),
                        progress,
                        formatBytes(downloadedBytes),
                        formatBytes(totalBytes)
                    )
                    downloadProgress.text = progressText
                }
            }

            override fun onDownloadCompleted(modelName: String, filePath: String) {
                runOnUiThread {
                    downloadProgress.text = "تم تحميل ${getModelDisplayName(modelName)} بنجاح!"
                    checkAllModelsDownloaded()
                }
            }

            override fun onDownloadFailed(modelName: String, error: String) {
                runOnUiThread {
                    downloadProgress.text = "فشل تحميل ${getModelDisplayName(modelName)}: $error"
                    btnDownloadModels.isEnabled = true
                }
            }
        })
    }

    private fun downloadAIModels() {
        if (!checkPermissions()) {
            return
        }

        downloadProgress.text = "جاري التحقق من النماذج الموجودة..."
        modelDownloadManager.downloadAllModels()
    }

    private fun checkAllModelsDownloaded() {
        val allDownloaded = modelDownloadManager.isModelDownloaded(ModelDownloadManager.GAME_DETECTION_MODEL) &&
                modelDownloadManager.isModelDownloaded(ModelDownloadManager.CARD_DETECTION_MODEL)

        if (allDownloaded) {
            downloadProgress.text = "✅ جميع النماذج جاهزة! يمكنك الآن بدء الخدمة"
            btnDownloadModels.isEnabled = true
            btnStartService.isEnabled = true
        }
    }

    private fun getModelDisplayName(modelName: String): String {
        return when (modelName) {
            ModelDownloadManager.GAME_DETECTION_MODEL -> "نموذج اكتشاف الألعاب"
            ModelDownloadManager.CARD_DETECTION_MODEL -> "نموذج اكتشاف البطاقات"
            else -> modelName
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }

    private fun checkPermissions(): Boolean {
        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return false
        }
        return true
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
    }

    private fun requestScreenCapture() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureResultLauncher.launch(captureIntent)
    }

    private fun startScreenCaptureService(resultCode: Int, data: Intent?) {
        val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
            putExtra("resultCode", resultCode)
            putExtra("data", data)
        }

        ContextCompat.startForegroundService(this, serviceIntent)

        // Also start the game analysis service
        val analysisIntent = Intent(this, GameAnalysisService::class.java)
        startService(analysisIntent)

        // Start bubble overlay service
        val bubbleIntent = Intent(this, BubbleOverlayService::class.java)
        startService(bubbleIntent)

        updateServiceStatus(true)
    }

    private fun stopServices() {
        stopService(Intent(this, ScreenCaptureService::class.java))
        stopService(Intent(this, GameAnalysisService::class.java))
        stopService(Intent(this, BubbleOverlayService::class.java))

        // Clear any remaining notifications
        notificationTipManager.clearAllTips()

        updateServiceStatus(false)
    }

    private fun updateServiceStatus(running: Boolean) {
        isServiceRunning = running

        if (running) {
            statusText.text = "الخدمة تعمل - جاهز للتحليل"
            btnStartService.isEnabled = false
            btnStopService.isEnabled = true
            suggestionsCard.visibility = View.VISIBLE
        } else {
            statusText.text = "الخدمة متوقفة"
            btnStartService.isEnabled = true
            btnStopService.isEnabled = false
            suggestionsCard.visibility = View.GONE
            detectedGame.text = getString(R.string.game_not_detected)
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)

        Toast.makeText(
            this,
            "فعّل خدمة مساعد الألعاب الذكي من قائمة خدمات الوصول",
            Toast.LENGTH_LONG
        ).show()
    }

    fun updateDetectedGame(gameName: String) {
        runOnUiThread {
            detectedGame.text = "تم اكتشاف: $gameName"
        }
    }

    fun updateSuggestion(suggestion: String) {
        runOnUiThread {
            suggestionText.text = suggestion
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(
                    this,
                    "تم منح صلاحية العرض فوق التطبيقات",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "يجب منح صلاحية العرض فوق التطبيقات",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if services are still running
        // This is a simplified check - in production, you'd use proper service communication
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources if needed
    }
}