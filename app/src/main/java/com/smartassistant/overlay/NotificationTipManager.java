package com.smartassistant.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

/**
 * Manages gaming tip notifications as an alternative to overlay bubbles
 */
public class NotificationTipManager {
    private static final String TAG = "NotificationTipManager";
    
    private static final String CHANNEL_ID = "GAMING_TIPS_CHANNEL";
    private static final String CHANNEL_NAME = "نصائح الألعاب";
    private static final String CHANNEL_DESC = "نصائح وإرشادات ذكية أثناء اللعب";
    
    private static final int NOTIFICATION_ID_BASE = 2000;
    
    private Context context;
    private NotificationManager notificationManager;
    private int notificationCounter = 0;
    
    public NotificationTipManager(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // High importance for gaming tips
            );
            
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.enableVibration(false); // Don't vibrate during gaming
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created");
        }
    }
    
    public void showGameTip(String tipText, String tipType) {
        if (tipText == null || tipText.isEmpty()) return;
        
        // Create dismiss intent
        Intent dismissIntent = new Intent(context, NotificationDismissReceiver.class);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
            context, 
            notificationCounter, 
            dismissIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(getTipIcon(tipType))
            .setContentTitle(getTipTitle(tipType))
            .setContentText(tipText)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(tipText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setTimeoutAfter(8000) // Auto-dismiss after 8 seconds
            .setDeleteIntent(dismissPendingIntent)
            .setOngoing(false)
            .setShowWhen(false);
        
        // Add color based on tip type
        builder.setColor(getTipColor(tipType));
        
        // Create unique notification ID
        int notificationId = NOTIFICATION_ID_BASE + (notificationCounter % 10);
        notificationCounter++;
        
        // Show notification
        try {
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Game tip notification shown: " + tipText);
        } catch (Exception e) {
            Log.e(TAG, "Error showing tip notification", e);
        }
    }
    
    private int getTipIcon(String tipType) {
        // Use system icons based on tip type
        switch (tipType != null ? tipType : BubbleOverlayService.TIP_TYPE_GENERAL) {
            case BubbleOverlayService.TIP_TYPE_ATTACK:
                return android.R.drawable.ic_media_play;
            case BubbleOverlayService.TIP_TYPE_DEFENSE:
                return android.R.drawable.ic_menu_close_clear_cancel;
            case BubbleOverlayService.TIP_TYPE_ELIXIR:
                return android.R.drawable.ic_dialog_alert;
            case BubbleOverlayService.TIP_TYPE_TIME:
                return android.R.drawable.ic_lock_idle_alarm;
            default:
                return android.R.drawable.ic_dialog_info;
        }
    }
    
    private String getTipTitle(String tipType) {
        switch (tipType != null ? tipType : BubbleOverlayService.TIP_TYPE_GENERAL) {
            case BubbleOverlayService.TIP_TYPE_ATTACK:
                return "🔥 نصيحة هجومية";
            case BubbleOverlayService.TIP_TYPE_DEFENSE:
                return "🛡️ نصيحة دفاعية";
            case BubbleOverlayService.TIP_TYPE_ELIXIR:
                return "⚡ حالة الإكسير";
            case BubbleOverlayService.TIP_TYPE_TIME:
                return "⏰ تنبيه وقت";
            default:
                return "🎮 نصيحة ذكية";
        }
    }
    
    private int getTipColor(String tipType) {
        switch (tipType != null ? tipType : BubbleOverlayService.TIP_TYPE_GENERAL) {
            case BubbleOverlayService.TIP_TYPE_ATTACK:
                return 0xFFFF4444; // Red for attack
            case BubbleOverlayService.TIP_TYPE_DEFENSE:
                return 0xFF4444FF; // Blue for defense
            case BubbleOverlayService.TIP_TYPE_ELIXIR:
                return 0xFFFF8800; // Orange for elixir
            case BubbleOverlayService.TIP_TYPE_TIME:
                return 0xFFFFAA00; // Yellow for time
            default:
                return 0xFF44AA44; // Green for general
        }
    }
    
    public void clearAllTips() {
        try {
            // Cancel all tip notifications
            for (int i = 0; i < 10; i++) {
                notificationManager.cancel(NOTIFICATION_ID_BASE + i);
            }
            Log.d(TAG, "All tip notifications cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing tip notifications", e);
        }
    }
    
    /**
     * Receiver for handling notification dismissal
     */
    public static class NotificationDismissReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Tip notification dismissed by user");
        }
    }
}