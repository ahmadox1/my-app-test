package com.smartassistant.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.smartassistant.overlay.BubbleOverlayService

/**
 * Service for analyzing games and providing tips - simplified for offline builds
 */
class GameAnalysisService : Service() {
    
    companion object {
        private const val TAG = "GameAnalysisService"
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private var analysisRunnable: Runnable? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Game Analysis Service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting game analysis")
        startAnalysis()
        return START_STICKY
    }
    
    private fun startAnalysis() {
        analysisRunnable = object : Runnable {
            override fun run() {
                performAnalysis()
                handler.postDelayed(this, 5000) // Analyze every 5 seconds
            }
        }
        handler.post(analysisRunnable!!)
    }
    
    private fun performAnalysis() {
        Log.d(TAG, "Performing game analysis...")
        
        // Simulate game analysis
        val tips = listOf(
            "استخدم بطاقة السهم للدفاع ضد المينيون",
            "احتفظ بالإكسير للهجمة القادمة",
            "ضع المدفع في المنتصف للدفاع",
            "استخدم الفايربول ضد مجموعة الأعداء",
            "اختر التوقيت المناسب للهجوم"
        )
        
        val randomTip = tips.random()
        val tipTypes = listOf(
            BubbleOverlayService.TIP_TYPE_ATTACK,
            BubbleOverlayService.TIP_TYPE_DEFENSE,
            BubbleOverlayService.TIP_TYPE_ELIXIR,
            BubbleOverlayService.TIP_TYPE_GENERAL
        )
        val randomType = tipTypes.random()
        
        // Send tip to bubble service
        val bubbleIntent = Intent(this, BubbleOverlayService::class.java).apply {
            action = BubbleOverlayService.ACTION_SHOW_TIP
            putExtra(BubbleOverlayService.EXTRA_TIP_TEXT, randomTip)
            putExtra(BubbleOverlayService.EXTRA_TIP_TYPE, randomType)
        }
        startService(bubbleIntent)
        
        Log.d(TAG, "Analysis result: $randomTip")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        analysisRunnable?.let { handler.removeCallbacks(it) }
        Log.d(TAG, "Game Analysis Service destroyed")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}