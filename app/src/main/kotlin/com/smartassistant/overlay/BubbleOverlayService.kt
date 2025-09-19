package com.smartassistant.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager

/**
 * Service for displaying gaming tips as floating bubbles over other apps
 */
class BubbleOverlayService : Service() {
    
    companion object {
        private const val TAG = "BubbleOverlayService"
        
        const val ACTION_SHOW_TIP = "SHOW_TIP"
        const val EXTRA_TIP_TEXT = "tip_text"
        const val EXTRA_TIP_TYPE = "tip_type"
        
        // Tip types
        const val TIP_TYPE_ATTACK = "ATTACK"
        const val TIP_TYPE_DEFENSE = "DEFENSE"
        const val TIP_TYPE_ELIXIR = "ELIXIR"
        const val TIP_TYPE_TIME = "TIME"
        const val TIP_TYPE_GENERAL = "GENERAL"
    }
    
    private lateinit var windowManager: WindowManager
    private var bubbleView: android.view.View? = null
    private var isExpanded = false
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createBubbleView()
        Log.d(TAG, "Bubble Overlay Service created")
    }
    
    private fun createBubbleView() {
        // Create a simple bubble view programmatically
        val bubbleLayout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            background = android.graphics.drawable.ColorDrawable(0x80000000.toInt())
            setPadding(16, 8, 16, 8)
        }
        
        val textView = android.widget.TextView(this).apply {
            text = "🎮"
            textSize = 24f
            setTextColor(0xFFFFFFFF.toInt())
        }
        
        bubbleLayout.addView(textView)
        bubbleView = bubbleLayout
        
        // Set up window parameters
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }
        
        try {
            windowManager.addView(bubbleView, params)
            Log.d(TAG, "Bubble view added to window")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding bubble view", e)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_TIP -> {
                val tipText = intent.getStringExtra(EXTRA_TIP_TEXT)
                val tipType = intent.getStringExtra(EXTRA_TIP_TYPE)
                showTip(tipText, tipType)
            }
        }
        return START_STICKY
    }
    
    private fun showTip(tipText: String?, tipType: String?) {
        Log.d(TAG, "Showing tip: $tipText (type: $tipType)")
        
        // Update bubble view with tip
        bubbleView?.let { view ->
            val textView = (view as android.widget.LinearLayout).getChildAt(0) as android.widget.TextView
            textView.text = when (tipType) {
                TIP_TYPE_ATTACK -> "⚔️"
                TIP_TYPE_DEFENSE -> "🛡️"
                TIP_TYPE_ELIXIR -> "⚡"
                TIP_TYPE_TIME -> "⏰"
                else -> "🎮"
            }
            
            // Simple animation - just log for now
            Log.d(TAG, "Tip displayed: $tipText")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        bubbleView?.let { view ->
            try {
                windowManager.removeView(view)
                Log.d(TAG, "Bubble view removed from window")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing bubble view", e)
            }
        }
        
        Log.d(TAG, "Bubble Overlay Service destroyed")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}