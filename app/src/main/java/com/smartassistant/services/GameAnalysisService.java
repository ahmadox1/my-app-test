package com.smartassistant.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.smartassistant.ai.OpenCVManager;
import com.smartassistant.ai.TensorFlowManager;
import com.smartassistant.games.ClashRoyaleAnalyzer;
import com.smartassistant.overlay.BubbleOverlayService;
import com.smartassistant.overlay.NotificationTipManager;

public class GameAnalysisService extends Service {
    private static final String TAG = "GameAnalysisService";
    
    private TensorFlowManager tensorFlowManager;
    private OpenCVManager openCVManager;
    private ClashRoyaleAnalyzer clashRoyaleAnalyzer;
    private NotificationTipManager notificationTipManager;
    
    private Handler mainHandler;
    private Handler backgroundHandler;
    
    private String currentGame = "";
    private boolean isAnalyzing = false;

    @Override
    public void onCreate() {
        super.onCreate();
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize AI components
        initializeAI();
        
        Log.d(TAG, "Game Analysis Service created");
    }

    private void initializeAI() {
        try {
            // Initialize TensorFlow Lite
            tensorFlowManager = new TensorFlowManager(this);
            
            // Initialize OpenCV
            openCVManager = new OpenCVManager(this);
            
            // Initialize game-specific analyzers
            clashRoyaleAnalyzer = new ClashRoyaleAnalyzer(tensorFlowManager, openCVManager);
            
            // Initialize notification tip manager
            notificationTipManager = new NotificationTipManager(this);
            
            Log.d(TAG, "AI components initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing AI components", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "ANALYZE_SCREEN".equals(intent.getAction())) {
            if (!isAnalyzing) {
                startAnalysis();
            }
        }
        
        return START_STICKY;
    }

    private void startAnalysis() {
        if (tensorFlowManager == null || openCVManager == null) {
            Log.e(TAG, "AI components not initialized");
            return;
        }
        
        isAnalyzing = true;
        
        // Start background analysis thread
        new Thread(() -> {
            try {
                analyzeCurrentScreen();
            } catch (Exception e) {
                Log.e(TAG, "Error in analysis thread", e);
            } finally {
                isAnalyzing = false;
            }
        }).start();
    }

    private void analyzeCurrentScreen() {
        // Simulate screen analysis
        // In a real implementation, this would process the actual bitmap from ScreenCaptureService
        
        // Detect current game
        String detectedGame = detectCurrentGame();
        
        if (!detectedGame.equals(currentGame)) {
            currentGame = detectedGame;
            notifyGameChanged(detectedGame);
        }
        
        // Analyze game-specific content
        if ("Clash Royale".equals(currentGame)) {
            analyzeClashRoyale();
        }
        
        // Wait before next analysis
        try {
            Thread.sleep(2000); // Analyze every 2 seconds
        } catch (InterruptedException e) {
            Log.w(TAG, "Analysis thread interrupted");
        }
    }

    private String detectCurrentGame() {
        // Simulate game detection
        // In reality, this would use computer vision to detect game UI elements
        
        if (clashRoyaleAnalyzer != null && clashRoyaleAnalyzer.isClashRoyaleActive()) {
            return "Clash Royale";
        }
        
        // Add detection for other games here
        
        return "Unknown";
    }

    private void analyzeClashRoyale() {
        if (clashRoyaleAnalyzer == null) return;
        
        try {
            // Analyze current battle state
            ClashRoyaleAnalyzer.BattleState battleState = clashRoyaleAnalyzer.analyzeBattleState();
            
            if (battleState != null) {
                // Generate strategic suggestions
                String suggestion = generateClashRoyaleSuggestion(battleState);
                notifySuggestion(suggestion);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing Clash Royale", e);
        }
    }

    private String generateClashRoyaleSuggestion(ClashRoyaleAnalyzer.BattleState battleState) {
        // Generate intelligent suggestions based on battle state
        StringBuilder suggestion = new StringBuilder();
        
        // Analyze elixir
        if (battleState.playerElixir >= 6) {
            if (battleState.enemyTowers.size() > battleState.playerTowers.size()) {
                suggestion.append("🔥 فرصة هجوم! استخدم تركيبة قوية مع وجود الإكسير الكافي\n");
            } else {
                suggestion.append("⚡ إكسير جيد - فكر في هجوم مضاد\n");
            }
        } else if (battleState.playerElixir <= 3) {
            suggestion.append("🛡️ إكسير منخفض - ركز على الدفاع واكتساب الإكسير\n");
        }
        
        // Analyze enemy cards
        if (battleState.detectedEnemyCards.size() >= 3) {
            suggestion.append("📊 تم رصد ").append(battleState.detectedEnemyCards.size())
                     .append(" بطاقات من الخصم - خطط للمضاد\n");
        }
        
        // Analyze time remaining
        if (battleState.timeRemaining <= 60) {
            suggestion.append("⏰ الوقت ينفد! كن جريئاً في الهجمات\n");
        }
        
        // Default suggestion if no specific advice
        if (suggestion.length() == 0) {
            suggestion.append("🎮 راقب تحركات الخصم وكن مستعداً للرد");
        }
        
        return suggestion.toString();
    }

    private void notifyGameChanged(String gameName) {
        // Notify MainActivity about game change
        Intent broadcastIntent = new Intent("GAME_DETECTED");
        broadcastIntent.putExtra("gameName", gameName);
        sendBroadcast(broadcastIntent);
        
        Log.d(TAG, "Game detected: " + gameName);
    }

    private void notifySuggestion(String suggestion) {
        // Send to bubble overlay service
        Intent bubbleIntent = new Intent(this, BubbleOverlayService.class);
        bubbleIntent.setAction(BubbleOverlayService.ACTION_SHOW_TIP);
        bubbleIntent.putExtra(BubbleOverlayService.EXTRA_TIP_TEXT, suggestion);
        bubbleIntent.putExtra(BubbleOverlayService.EXTRA_TIP_TYPE, determineTipType(suggestion));
        startService(bubbleIntent);
        
        // Also send as notification (fallback)
        if (notificationTipManager != null) {
            notificationTipManager.showGameTip(suggestion, determineTipType(suggestion));
        }
        
        // Notify MainActivity about new suggestion
        Intent broadcastIntent = new Intent("SUGGESTION_READY");
        broadcastIntent.putExtra("suggestion", suggestion);
        sendBroadcast(broadcastIntent);
        
        Log.d(TAG, "Suggestion: " + suggestion);
    }
    
    private String determineTipType(String suggestion) {
        if (suggestion.contains("🔥") || suggestion.contains("هجوم")) {
            return BubbleOverlayService.TIP_TYPE_ATTACK;
        } else if (suggestion.contains("🛡️") || suggestion.contains("دفاع")) {
            return BubbleOverlayService.TIP_TYPE_DEFENSE;
        } else if (suggestion.contains("⚡") || suggestion.contains("إكسير")) {
            return BubbleOverlayService.TIP_TYPE_ELIXIR;
        } else if (suggestion.contains("⏰") || suggestion.contains("وقت")) {
            return BubbleOverlayService.TIP_TYPE_TIME;
        } else {
            return BubbleOverlayService.TIP_TYPE_GENERAL;
        }
    }

    public void processBitmap(Bitmap bitmap) {
        // Process the bitmap from screen capture
        if (bitmap != null && !bitmap.isRecycled()) {
            // Update analyzers with new bitmap
            if (clashRoyaleAnalyzer != null) {
                clashRoyaleAnalyzer.updateScreenshot(bitmap);
            }
            
            // Trigger analysis
            if (!isAnalyzing) {
                startAnalysis();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Clean up AI components
        if (tensorFlowManager != null) {
            tensorFlowManager.cleanup();
        }
        
        if (openCVManager != null) {
            openCVManager.cleanup();
        }
        
        Log.d(TAG, "Game Analysis Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}