package com.smartassistant.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import com.smartassistant.R;

import java.nio.ByteBuffer;

public class ScreenCaptureService extends Service {
    private static final String TAG = "ScreenCaptureService";
    private static final String CHANNEL_ID = "SCREEN_CAPTURE_CHANNEL";
    private static final int NOTIFICATION_ID = 1;
    
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    
    private int screenWidth, screenHeight, screenDensity;
    private int resultCode;
    private Intent resultData;

    @Override
    public void onCreate() {
        super.onCreate();
        
        createNotificationChannel();
        mediaProjectionManager = (MediaProjectionManager) 
            getSystemService(MEDIA_PROJECTION_SERVICE);
        
        // Get screen metrics
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        screenDensity = metrics.densityDpi;
        
        backgroundHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            resultCode = intent.getIntExtra("resultCode", -1);
            resultData = intent.getParcelableExtra("data");
            
            if (resultCode != -1 && resultData != null) {
                startForeground(NOTIFICATION_ID, createNotification());
                startScreenCapture();
                return START_STICKY;
            }
        }
        
        return START_NOT_STICKY;
    }

    private void startScreenCapture() {
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
        
        if (mediaProjection == null) {
            Log.e(TAG, "MediaProjection is null");
            stopSelf();
            return;
        }
        
        // Create ImageReader for capturing screenshots
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, 
            PixelFormat.RGBA_8888, 2);
        
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                processScreenshot(reader);
            }
        }, backgroundHandler);
        
        // Create VirtualDisplay
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            screenWidth, screenHeight, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.getSurface(),
            null, null
        );
        
        Log.d(TAG, "Screen capture started");
    }

    private void processScreenshot(ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireLatestImage();
            if (image != null) {
                // Convert Image to Bitmap
                Bitmap bitmap = imageToBitmap(image);
                if (bitmap != null) {
                    // Send bitmap to game analysis service
                    sendBitmapToAnalysis(bitmap);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing screenshot", e);
        } finally {
            if (image != null) {
                image.close();
            }
        }
    }

    private Bitmap imageToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * screenWidth;

        Bitmap bitmap = Bitmap.createBitmap(
            screenWidth + rowPadding / pixelStride,
            screenHeight,
            Bitmap.Config.ARGB_8888
        );
        
        bitmap.copyPixelsFromBuffer(buffer);
        
        // Crop to remove padding if necessary
        if (rowPadding != 0) {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight);
        }
        
        return bitmap;
    }

    private void sendBitmapToAnalysis(Bitmap bitmap) {
        // Send the bitmap to GameAnalysisService for processing
        Intent intent = new Intent(this, GameAnalysisService.class);
        intent.setAction("ANALYZE_SCREEN");
        // In a real implementation, you'd save the bitmap to a file or pass it via a more efficient method
        // For now, we'll just trigger the analysis
        startService(intent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Screen Capture Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("خدمة التقاط الشاشة للمساعد الذكي");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("مساعد الألعاب الذكي")
                .setContentText("يتم تحليل الشاشة...")
                .setSmallIcon(R.drawable.ic_gaming_assistant)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
        
        if (imageReader != null) {
            imageReader.close();
        }
        
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
        
        Log.d(TAG, "Screen capture service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}