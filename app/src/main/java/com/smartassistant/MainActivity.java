package com.smartassistant;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.smartassistant.services.GameAnalysisService;
import com.smartassistant.services.ScreenCaptureService;
import com.smartassistant.ai.ModelDownloadManager;
import com.smartassistant.overlay.BubbleOverlayService;
import com.smartassistant.overlay.NotificationTipManager;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SCREEN_CAPTURE = 1000;
    private static final int REQUEST_OVERLAY_PERMISSION = 1001;

    private Button btnStartService, btnStopService, btnSettings, btnDownloadModels;
    private TextView statusText, detectedGame, suggestionText, downloadProgress;
    private CardView suggestionsCard;

    private MediaProjectionManager mediaProjectionManager;
    private boolean isServiceRunning = false;
    private ModelDownloadManager modelDownloadManager;
    private NotificationTipManager notificationTipManager;

    // Activity result launcher for screen capture permission
    private ActivityResultLauncher<Intent> screenCaptureResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent data = result.getData();
                                startScreenCaptureService(result.getResultCode(), data);
                            } else {
                                Toast.makeText(MainActivity.this, 
                                    "يجب منح صلاحية التقاط الشاشة", 
                                    Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        
        mediaProjectionManager = (MediaProjectionManager) 
            getSystemService(MEDIA_PROJECTION_SERVICE);
            
        // Initialize AI model download manager
        initializeModelDownloader();
        
        // Initialize notification tip manager
        notificationTipManager = new NotificationTipManager(this);
    }

    private void initViews() {
        btnStartService = findViewById(R.id.btn_start_service);
        btnStopService = findViewById(R.id.btn_stop_service);
        btnSettings = findViewById(R.id.btn_settings);
        btnDownloadModels = findViewById(R.id.btn_download_models);
        
        statusText = findViewById(R.id.status_text);
        detectedGame = findViewById(R.id.detected_game);
        suggestionText = findViewById(R.id.suggestion_text);
        suggestionsCard = findViewById(R.id.suggestions_card);
        downloadProgress = findViewById(R.id.download_progress);
    }

    private void setupClickListeners() {
        btnStartService.setOnClickListener(v -> {
            if (checkPermissions()) {
                requestScreenCapture();
            }
        });

        btnStopService.setOnClickListener(v -> {
            stopServices();
        });

        btnSettings.setOnClickListener(v -> {
            openAccessibilitySettings();
        });
        
        btnDownloadModels.setOnClickListener(v -> {
            downloadAIModels();
        });
    }
    
    private void initializeModelDownloader() {
        modelDownloadManager = new ModelDownloadManager(this);
        modelDownloadManager.setProgressListener(new ModelDownloadManager.DownloadProgressListener() {
            @Override
            public void onDownloadStarted(String modelName) {
                runOnUiThread(() -> {
                    downloadProgress.setText("بدء تحميل " + getModelDisplayName(modelName) + "...");
                    btnDownloadModels.setEnabled(false);
                });
            }

            @Override
            public void onDownloadProgress(String modelName, int progress, long downloadedBytes, long totalBytes) {
                runOnUiThread(() -> {
                    String progressText = String.format("تحميل %s: %d%% (%s / %s)", 
                        getModelDisplayName(modelName), 
                        progress,
                        formatBytes(downloadedBytes),
                        formatBytes(totalBytes));
                    downloadProgress.setText(progressText);
                });
            }

            @Override
            public void onDownloadCompleted(String modelName, String filePath) {
                runOnUiThread(() -> {
                    downloadProgress.setText("تم تحميل " + getModelDisplayName(modelName) + " بنجاح!");
                    checkAllModelsDownloaded();
                });
            }

            @Override
            public void onDownloadFailed(String modelName, String error) {
                runOnUiThread(() -> {
                    downloadProgress.setText("فشل تحميل " + getModelDisplayName(modelName) + ": " + error);
                    btnDownloadModels.setEnabled(true);
                });
            }
        });
    }
    
    private void downloadAIModels() {
        if (!checkPermissions()) {
            return;
        }
        
        downloadProgress.setText("جاري التحقق من النماذج الموجودة...");
        modelDownloadManager.downloadAllModels();
    }
    
    private void checkAllModelsDownloaded() {
        boolean allDownloaded = modelDownloadManager.isModelDownloaded(ModelDownloadManager.GAME_DETECTION_MODEL) &&
                              modelDownloadManager.isModelDownloaded(ModelDownloadManager.CARD_DETECTION_MODEL);
        
        if (allDownloaded) {
            downloadProgress.setText("✅ جميع النماذج جاهزة! يمكنك الآن بدء الخدمة");
            btnDownloadModels.setEnabled(true);
            btnStartService.setEnabled(true);
        }
    }
    
    private String getModelDisplayName(String modelName) {
        switch (modelName) {
            case ModelDownloadManager.GAME_DETECTION_MODEL:
                return "نموذج اكتشاف الألعاب";
            case ModelDownloadManager.CARD_DETECTION_MODEL:
                return "نموذج اكتشاف البطاقات";
            default:
                return modelName;
        }
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private boolean checkPermissions() {
        // Check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
            return false;
        }
        return true;
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
    }

    private void requestScreenCapture() {
        Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
        screenCaptureResultLauncher.launch(captureIntent);
    }

    private void startScreenCaptureService(int resultCode, Intent data) {
        Intent serviceIntent = new Intent(this, ScreenCaptureService.class);
        serviceIntent.putExtra("resultCode", resultCode);
        serviceIntent.putExtra("data", data);
        
        ContextCompat.startForegroundService(this, serviceIntent);
        
        // Also start the game analysis service
        Intent analysisIntent = new Intent(this, GameAnalysisService.class);
        startService(analysisIntent);
        
        // Start bubble overlay service
        Intent bubbleIntent = new Intent(this, BubbleOverlayService.class);
        startService(bubbleIntent);
        
        updateServiceStatus(true);
    }

    private void stopServices() {
        Intent captureIntent = new Intent(this, ScreenCaptureService.class);
        stopService(captureIntent);
        
        Intent analysisIntent = new Intent(this, GameAnalysisService.class);
        stopService(analysisIntent);
        
        // Stop bubble overlay service
        Intent bubbleIntent = new Intent(this, BubbleOverlayService.class);
        stopService(bubbleIntent);
        
        // Clear any remaining notifications
        if (notificationTipManager != null) {
            notificationTipManager.clearAllTips();
        }
        
        updateServiceStatus(false);
    }

    private void updateServiceStatus(boolean running) {
        isServiceRunning = running;
        
        if (running) {
            statusText.setText("الخدمة تعمل - جاهز للتحليل");
            btnStartService.setEnabled(false);
            btnStopService.setEnabled(true);
            suggestionsCard.setVisibility(android.view.View.VISIBLE);
        } else {
            statusText.setText("الخدمة متوقفة");
            btnStartService.setEnabled(true);
            btnStopService.setEnabled(false);
            suggestionsCard.setVisibility(android.view.View.GONE);
            detectedGame.setText(getString(R.string.game_not_detected));
        }
    }

    private void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        
        Toast.makeText(this, 
            "فعّل خدمة مساعد الألعاب الذكي من قائمة خدمات الوصول", 
            Toast.LENGTH_LONG).show();
    }

    public void updateDetectedGame(String gameName) {
        runOnUiThread(() -> {
            detectedGame.setText("تم اكتشاف: " + gameName);
        });
    }

    public void updateSuggestion(String suggestion) {
        runOnUiThread(() -> {
            suggestionText.setText(suggestion);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "تم منح صلاحية العرض فوق التطبيقات", 
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "يجب منح صلاحية العرض فوق التطبيقات", 
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if services are still running
        // This is a simplified check - in production, you'd use proper service communication
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources if needed
    }
}