package com.smartassistant.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class SmartAssistantAccessibilityService extends AccessibilityService {
    private static final String TAG = "SmartAssistantAccessibility";
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        
        String packageName = event.getPackageName() != null ? 
                           event.getPackageName().toString() : "";
        
        // Check if it's a supported game
        if (packageName.equals("com.supercell.clashroyale")) {
            handleClashRoyaleEvent(event);
        } else if (packageName.equals("com.supercell.clashofclans")) {
            handleClashOfClansEvent(event);
        }
    }
    
    private void handleClashRoyaleEvent(AccessibilityEvent event) {
        // Notify the analysis service that Clash Royale is active
        Intent intent = new Intent("GAME_ACTIVITY_DETECTED");
        intent.putExtra("game", "Clash Royale");
        intent.putExtra("event_type", event.getEventType());
        sendBroadcast(intent);
        
        Log.d(TAG, "Clash Royale activity detected");
    }
    
    private void handleClashOfClansEvent(AccessibilityEvent event) {
        // Handle Clash of Clans events
        Intent intent = new Intent("GAME_ACTIVITY_DETECTED");
        intent.putExtra("game", "Clash of Clans");
        intent.putExtra("event_type", event.getEventType());
        sendBroadcast(intent);
        
        Log.d(TAG, "Clash of Clans activity detected");
    }
    
    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted");
    }
    
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Smart Assistant Accessibility Service connected");
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Smart Assistant Accessibility Service unbound");
        return super.onUnbind(intent);
    }
}