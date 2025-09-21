package com.smartassistant.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * Manages gaming tip notifications as an alternative to overlay bubbles
 */
class NotificationTipManager(private val context: Context) {
    
    companion object {
        private const val TAG = "NotificationTipManager"
        private const val CHANNEL_ID = "GAMING_TIPS_CHANNEL"
        private const val CHANNEL_NAME = "نصائح الألعاب"
        private const val CHANNEL_DESC = "نصائح وإرشادات ذكية أثناء اللعب"
        private const val NOTIFICATION_ID_BASE = 2000
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var notificationCounter = 0
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // High importance for gaming tips
            ).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(false) // Don't vibrate during gaming
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }
    
    fun showGameTip(tipText: String?, tipType: String?) {
        if (tipText.isNullOrEmpty()) return
        
        // Create dismiss intent
        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java)
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationCounter,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(getTipIcon(tipType))
            .setContentTitle(getTipTitle(tipType))
            .setContentText(tipText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(tipText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setTimeoutAfter(8000) // Auto-dismiss after 8 seconds
            .setDeleteIntent(dismissPendingIntent)
            .setOngoing(false)
            .setShowWhen(false)
            .setColor(getTipColor(tipType))
        
        // Create unique notification ID
        val notificationId = NOTIFICATION_ID_BASE + (notificationCounter % 10)
        notificationCounter++
        
        // Show notification
        try {
            notificationManager.notify(notificationId, builder.build())
            Log.d(TAG, "Game tip notification shown: $tipText")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing tip notification", e)
        }
    }
    
    private fun getTipIcon(tipType: String?): Int {
        // Use system icons based on tip type
        return when (tipType ?: BubbleOverlayService.TIP_TYPE_GENERAL) {
            BubbleOverlayService.TIP_TYPE_ATTACK -> android.R.drawable.ic_media_play
            BubbleOverlayService.TIP_TYPE_DEFENSE -> android.R.drawable.ic_menu_close_clear_cancel
            BubbleOverlayService.TIP_TYPE_ELIXIR -> android.R.drawable.ic_dialog_alert
            BubbleOverlayService.TIP_TYPE_TIME -> android.R.drawable.ic_lock_idle_alarm
            else -> android.R.drawable.ic_dialog_info
        }
    }
    
    private fun getTipTitle(tipType: String?): String {
        return when (tipType ?: BubbleOverlayService.TIP_TYPE_GENERAL) {
            BubbleOverlayService.TIP_TYPE_ATTACK -> "🔥 نصيحة هجومية"
            BubbleOverlayService.TIP_TYPE_DEFENSE -> "🛡️ نصيحة دفاعية"
            BubbleOverlayService.TIP_TYPE_ELIXIR -> "⚡ حالة الإكسير"
            BubbleOverlayService.TIP_TYPE_TIME -> "⏰ تنبيه وقت"
            else -> "🎮 نصيحة ذكية"
        }
    }
    
    private fun getTipColor(tipType: String?): Int {
        return when (tipType ?: BubbleOverlayService.TIP_TYPE_GENERAL) {
            BubbleOverlayService.TIP_TYPE_ATTACK -> 0xFFFF4444.toInt() // Red for attack
            BubbleOverlayService.TIP_TYPE_DEFENSE -> 0xFF4444FF.toInt() // Blue for defense
            BubbleOverlayService.TIP_TYPE_ELIXIR -> 0xFFFF8800.toInt() // Orange for elixir
            BubbleOverlayService.TIP_TYPE_TIME -> 0xFFFFAA00.toInt() // Yellow for time
            else -> 0xFF44AA44.toInt() // Green for general
        }
    }
    
    fun clearAllTips() {
        try {
            // Cancel all tip notifications
            repeat(10) { i ->
                notificationManager.cancel(NOTIFICATION_ID_BASE + i)
            }
            Log.d(TAG, "All tip notifications cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing tip notifications", e)
        }
    }
    
    /**
     * Receiver for handling notification dismissal
     */
    class NotificationDismissReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Tip notification dismissed by user")
        }
    }
}