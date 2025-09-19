package com.smartassistant.ai;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager for downloading AI models from the internet
 */
public class ModelDownloadManager {
    private static final String TAG = "ModelDownloadManager";
    
    // Free AI models URLs (using public TensorFlow Lite models)
    private static final String GAME_DETECTION_MODEL_URL = 
        "https://tfhub.dev/tensorflow/lite-model/ssd_mobilenet_v1/1/metadata/2?lite-format=tflite";
    private static final String CARD_DETECTION_MODEL_URL = 
        "https://tfhub.dev/tensorflow/lite-model/efficientnet/lite0/classification/2?lite-format=tflite";
    
    public static final String GAME_DETECTION_MODEL = "game_detection_model.tflite";
    public static final String CARD_DETECTION_MODEL = "card_detection_model.tflite";
    
    private Context context;
    private DownloadManager downloadManager;
    private Handler mainHandler;
    
    private Map<Long, String> downloadIds;
    private DownloadProgressListener progressListener;
    private BroadcastReceiver downloadCompleteReceiver;
    
    public interface DownloadProgressListener {
        void onDownloadStarted(String modelName);
        void onDownloadProgress(String modelName, int progress, long downloadedBytes, long totalBytes);
        void onDownloadCompleted(String modelName, String filePath);
        void onDownloadFailed(String modelName, String error);
    }
    
    public ModelDownloadManager(Context context) {
        this.context = context;
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.downloadIds = new HashMap<>();
        
        setupDownloadReceiver();
    }
    
    private void setupDownloadReceiver() {
        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    handleDownloadComplete(downloadId);
                }
            }
        };
        
        context.registerReceiver(downloadCompleteReceiver, 
            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    
    public void setProgressListener(DownloadProgressListener listener) {
        this.progressListener = listener;
    }
    
    public void downloadAllModels() {
        downloadModel(GAME_DETECTION_MODEL, GAME_DETECTION_MODEL_URL);
        downloadModel(CARD_DETECTION_MODEL, CARD_DETECTION_MODEL_URL);
    }
    
    public void downloadModel(String modelName, String modelUrl) {
        try {
            // Check if model already exists
            File modelFile = new File(getModelPath(modelName));
            if (modelFile.exists()) {
                Log.d(TAG, "Model " + modelName + " already exists");
                if (progressListener != null) {
                    progressListener.onDownloadCompleted(modelName, modelFile.getAbsolutePath());
                }
                return;
            }
            
            // Create download request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(modelUrl));
            request.setTitle("تحميل نموذج الذكاء الاصطناعي");
            request.setDescription("جاري تحميل " + getModelDisplayName(modelName));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            
            // Set destination
            File modelsDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ai_models");
            if (!modelsDir.exists()) {
                modelsDir.mkdirs();
            }
            
            request.setDestinationInExternalFilesDir(context, "ai_models", modelName);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setAllowedOverRoaming(false);
            
            // Start download
            long downloadId = downloadManager.enqueue(request);
            downloadIds.put(downloadId, modelName);
            
            if (progressListener != null) {
                progressListener.onDownloadStarted(modelName);
            }
            
            // Start progress monitoring
            startProgressMonitoring(downloadId, modelName);
            
            Log.d(TAG, "Started downloading " + modelName + " with ID: " + downloadId);
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting download for " + modelName, e);
            if (progressListener != null) {
                progressListener.onDownloadFailed(modelName, e.getMessage());
            }
        }
    }
    
    private void startProgressMonitoring(long downloadId, String modelName) {
        new Thread(() -> {
            boolean downloading = true;
            while (downloading) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                
                Cursor cursor = downloadManager.query(query);
                if (cursor != null && cursor.moveToFirst()) {
                    int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                    
                    if (status == DownloadManager.STATUS_SUCCESSFUL || 
                        status == DownloadManager.STATUS_FAILED) {
                        downloading = false;
                    } else if (status == DownloadManager.STATUS_RUNNING) {
                        long downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        long total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        
                        if (total > 0) {
                            int progress = (int) ((downloaded * 100) / total);
                            
                            if (progressListener != null) {
                                mainHandler.post(() -> 
                                    progressListener.onDownloadProgress(modelName, progress, downloaded, total)
                                );
                            }
                        }
                    }
                }
                
                if (cursor != null) {
                    cursor.close();
                }
                
                try {
                    Thread.sleep(500); // Update every 500ms
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
    
    private void handleDownloadComplete(long downloadId) {
        String modelName = downloadIds.get(downloadId);
        if (modelName == null) return;
        
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        
        Cursor cursor = downloadManager.query(query);
        if (cursor != null && cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                String filePath = getModelPath(modelName);
                Log.d(TAG, "Download completed for " + modelName + " at: " + filePath);
                
                if (progressListener != null) {
                    progressListener.onDownloadCompleted(modelName, filePath);
                }
            } else {
                String reason = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));
                Log.e(TAG, "Download failed for " + modelName + ": " + reason);
                
                if (progressListener != null) {
                    progressListener.onDownloadFailed(modelName, "فشل التحميل: " + reason);
                }
            }
        }
        
        if (cursor != null) {
            cursor.close();
        }
        
        downloadIds.remove(downloadId);
    }
    
    public String getModelPath(String modelName) {
        File modelsDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "ai_models");
        return new File(modelsDir, modelName).getAbsolutePath();
    }
    
    public boolean isModelDownloaded(String modelName) {
        File modelFile = new File(getModelPath(modelName));
        return modelFile.exists() && modelFile.length() > 0;
    }
    
    private String getModelDisplayName(String modelName) {
        switch (modelName) {
            case GAME_DETECTION_MODEL:
                return "نموذج اكتشاف الألعاب";
            case CARD_DETECTION_MODEL:
                return "نموذج اكتشاف البطاقات";
            default:
                return modelName;
        }
    }
    
    public void cleanup() {
        if (downloadCompleteReceiver != null) {
            try {
                context.unregisterReceiver(downloadCompleteReceiver);
            } catch (Exception e) {
                Log.w(TAG, "Error unregistering receiver", e);
            }
        }
    }
}