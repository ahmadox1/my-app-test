package com.smartassistant.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Service for capturing screen content - simplified for offline builds
 */
class ScreenCaptureService : Service() {
    
    companion object {
        private const val TAG = "ScreenCaptureService"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Screen Capture Service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", -1) ?: -1
        val data = intent?.getParcelableExtra<Intent>("data")
        
        Log.d(TAG, "Starting screen capture with result code: $resultCode")
        
        // In a real implementation, this would set up MediaProjection
        // For now, just simulate the service running
        simulateScreenCapture()
        
        return START_STICKY
    }
    
    private fun simulateScreenCapture() {
        Log.d(TAG, "Simulating screen capture...")
        
        // Start a background thread to simulate screen capture
        Thread {
            try {
                while (!Thread.currentThread().isInterrupted) {
                    Thread.sleep(1000)
                    Log.v(TAG, "Screen capture frame simulated")
                }
            } catch (e: InterruptedException) {
                Log.d(TAG, "Screen capture simulation stopped")
                Thread.currentThread().interrupt()
            }
        }.start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Screen Capture Service destroyed")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}