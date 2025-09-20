package com.ahmadox.smartcoach.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint

/**
 * Accessibility service that monitors foreground applications and reads screen elements.
 * 
 * This service specifically targets supported games and triggers screen analysis
 * when relevant events occur.
 */
@AndroidEntryPoint
class SmartCoachAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "SmartCoachAccessibility"
        
        // Supported game packages
        private val SUPPORTED_GAMES = setOf(
            "com.supercell.clashroyale",
            "com.supercell.clashofclans",
            // Add more games as needed
        )
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { handleAccessibilityEvent(it) }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Smart Coach Accessibility Service connected")
        
        // Notify main app that service is connected
        sendBroadcast(Intent("com.ahmadox.smartcoach.ACCESSIBILITY_CONNECTED"))
    }
    
    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Smart Coach Accessibility Service disconnected")
        
        // Notify main app that service is disconnected
        sendBroadcast(Intent("com.ahmadox.smartcoach.ACCESSIBILITY_DISCONNECTED"))
        
        return super.onUnbind(intent)
    }
    
    private fun handleAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        
        // Only process events from supported games
        if (!SUPPORTED_GAMES.contains(packageName)) return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(event, packageName)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleContentChanged(event, packageName)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                handleViewClicked(event, packageName)
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                handleTextChanged(event, packageName)
            }
        }
    }
    
    private fun handleWindowStateChanged(event: AccessibilityEvent, packageName: String) {
        Log.d(TAG, "Window state changed in $packageName")
        
        when (packageName) {
            "com.supercell.clashroyale" -> {
                // Notify that Clash Royale is active
                notifyGameStateChange("clash_royale", "window_changed")
            }
            "com.supercell.clashofclans" -> {
                // Notify that Clash of Clans is active
                notifyGameStateChange("clash_of_clans", "window_changed")
            }
        }
    }
    
    private fun handleContentChanged(event: AccessibilityEvent, packageName: String) {
        // Look for specific content changes that might indicate game state changes
        val text = event.text?.joinToString(" ") ?: ""
        
        when {
            text.contains("Battle", ignoreCase = true) -> {
                notifyGameEvent("battle_detected", text)
            }
            text.contains("Victory", ignoreCase = true) || 
            text.contains("Defeat", ignoreCase = true) -> {
                notifyGameEvent("battle_ended", text)
            }
            // Look for elixir or card-related text
            text.matches(Regex(".*\\d{1,2}/\\d{1,2}.*")) -> {
                notifyGameEvent("elixir_changed", text)
            }
        }
        
        Log.v(TAG, "Content changed: $text")
    }
    
    private fun handleViewClicked(event: AccessibilityEvent, packageName: String) {
        // Detect card plays or important button clicks
        val clickedText = event.text?.joinToString(" ") ?: ""
        
        if (clickedText.isNotEmpty()) {
            notifyGameEvent("card_played", clickedText)
            Log.d(TAG, "View clicked: $clickedText")
        }
    }
    
    private fun handleTextChanged(event: AccessibilityEvent, packageName: String) {
        // Monitor text changes for game state updates
        val newText = event.text?.joinToString(" ") ?: ""
        
        // Look for patterns that indicate important game information
        when {
            newText.matches(Regex("\\d+")) -> {
                // Possible elixir or health value
                notifyGameEvent("numeric_value_changed", newText)
            }
        }
    }
    
    private fun notifyGameStateChange(gameType: String, eventType: String) {
        val intent = Intent("com.ahmadox.smartcoach.GAME_STATE_CHANGED").apply {
            putExtra("game_type", gameType)
            putExtra("event_type", eventType)
            putExtra("timestamp", System.currentTimeMillis())
        }
        sendBroadcast(intent)
        
        // Also trigger screen capture if needed
        startScreenCapture()
    }
    
    private fun notifyGameEvent(eventType: String, eventData: String) {
        val intent = Intent("com.ahmadox.smartcoach.GAME_EVENT").apply {
            putExtra("event_type", eventType)
            putExtra("event_data", eventData)
            putExtra("timestamp", System.currentTimeMillis())
        }
        sendBroadcast(intent)
        
        // Trigger analysis if this is an important event
        if (isImportantEvent(eventType)) {
            startScreenCapture()
        }
    }
    
    private fun isImportantEvent(eventType: String): Boolean {
        return when (eventType) {
            "battle_detected", "card_played", "elixir_changed" -> true
            else -> false
        }
    }
    
    private fun startScreenCapture() {
        // Request screen capture from the main service
        val intent = Intent("com.ahmadox.smartcoach.REQUEST_SCREEN_CAPTURE")
        sendBroadcast(intent)
    }
}