package com.smartassistant.games;

import android.graphics.Bitmap;
import android.util.Log;

import com.smartassistant.ai.OpenCVManager;
import com.smartassistant.ai.TensorFlowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Specialized analyzer for Clash Royale game
 * Provides strategic analysis and suggestions based on game state
 */
public class ClashRoyaleAnalyzer {
    private static final String TAG = "ClashRoyaleAnalyzer";
    
    private TensorFlowManager tensorFlowManager;
    private OpenCVManager openCVManager;
    private Bitmap currentScreenshot;
    
    // Game state tracking
    private BattleState lastBattleState;
    private long lastAnalysisTime;
    private static final long ANALYSIS_INTERVAL = 2000; // 2 seconds
    
    public ClashRoyaleAnalyzer(TensorFlowManager tensorFlowManager, OpenCVManager openCVManager) {
        this.tensorFlowManager = tensorFlowManager;
        this.openCVManager = openCVManager;
    }
    
    public void updateScreenshot(Bitmap bitmap) {
        this.currentScreenshot = bitmap;
    }
    
    public boolean isClashRoyaleActive() {
        if (currentScreenshot == null || openCVManager == null) {
            return false;
        }
        
        return openCVManager.isClashRoyaleScreen(currentScreenshot);
    }
    
    public BattleState analyzeBattleState() {
        if (currentScreenshot == null) {
            return null;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAnalysisTime < ANALYSIS_INTERVAL) {
            return lastBattleState; // Return cached state to avoid over-analysis
        }
        
        try {
            BattleState battleState = new BattleState();
            
            // Analyze image for game elements
            OpenCVManager.ImageAnalysisResult imageAnalysis = 
                openCVManager.analyzeImage(currentScreenshot);
            
            if (imageAnalysis != null) {
                // Extract battle information
                extractBattleInfo(imageAnalysis, battleState);
                
                // Analyze cards using TensorFlow
                analyzeCards(battleState);
                
                // Analyze battlefield situation
                analyzeBattlefield(imageAnalysis, battleState);
                
                lastBattleState = battleState;
                lastAnalysisTime = currentTime;
                
                Log.d(TAG, "Battle state analyzed: Elixir=" + battleState.playerElixir + 
                          ", Time=" + battleState.timeRemaining);
            }
            
            return battleState;
            
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing battle state", e);
            return null;
        }
    }
    
    private void extractBattleInfo(OpenCVManager.ImageAnalysisResult analysis, BattleState battleState) {
        // Extract timer
        for (OpenCVManager.TextArea textArea : analysis.textAreas) {
            switch (textArea.type) {
                case "timer":
                    battleState.timeRemaining = parseTimeString(textArea.text);
                    break;
                case "player_crowns":
                    battleState.playerCrowns = Integer.parseInt(textArea.text);
                    break;
                case "enemy_crowns":
                    battleState.enemyCrowns = Integer.parseInt(textArea.text);
                    break;
                case "elixir_count":
                    battleState.playerElixir = Integer.parseInt(textArea.text);
                    break;
            }
        }
        
        // Extract towers
        for (OpenCVManager.GameElement element : analysis.gameElements) {
            if (element.type.equals("tower")) {
                if (element.owner.equals("player")) {
                    battleState.playerTowers.add(element);
                } else {
                    battleState.enemyTowers.add(element);
                }
            }
        }
        
        // Extract troops
        for (OpenCVManager.GameElement element : analysis.gameElements) {
            if (element.type.equals("troop")) {
                if (element.owner.equals("player")) {
                    battleState.playerTroops.add(element);
                } else {
                    battleState.enemyTroops.add(element);
                }
            }
        }
    }
    
    private void analyzeCards(BattleState battleState) {
        if (tensorFlowManager == null) return;
        
        // Detect cards in hand using TensorFlow
        TensorFlowManager.CardDetectionResult cardResult = 
            tensorFlowManager.detectCards(currentScreenshot);
        
        if (cardResult != null) {
            for (TensorFlowManager.DetectedCard card : cardResult.detectedCards) {
                // Determine if it's player or enemy card based on position
                if (card.y > 0.7) { // Bottom area = player cards
                    battleState.playerCards.add(card.cardType);
                } else {
                    battleState.detectedEnemyCards.add(card.cardType);
                }
            }
        }
    }
    
    private void analyzeBattlefield(OpenCVManager.ImageAnalysisResult analysis, BattleState battleState) {
        // Analyze color regions to determine battlefield control
        for (OpenCVManager.ColorRegion region : analysis.colorRegions) {
            switch (region.name) {
                case "player_territory":
                    battleState.playerTerritoryControl = calculateTerritoryControl(region);
                    break;
                case "enemy_territory":
                    battleState.enemyTerritoryControl = calculateTerritoryControl(region);
                    break;
            }
        }
        
        // Determine battle phase
        if (battleState.timeRemaining > 120) {
            battleState.battlePhase = BattlePhase.EARLY;
        } else if (battleState.timeRemaining > 60) {
            battleState.battlePhase = BattlePhase.MID;
        } else {
            battleState.battlePhase = BattlePhase.LATE;
        }
    }
    
    private float calculateTerritoryControl(OpenCVManager.ColorRegion region) {
        // Simplified territory control calculation
        // In reality, this would analyze troop density, building presence, etc.
        return 0.5f + (float)(Math.random() * 0.5); // Random for demo
    }
    
    private int parseTimeString(String timeStr) {
        try {
            String[] parts = timeStr.split(":");
            if (parts.length == 2) {
                return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing time string: " + timeStr, e);
        }
        return 180; // Default 3 minutes
    }
    
    /**
     * Generate strategic suggestions based on current battle state
     */
    public String generateStrategicSuggestion(BattleState battleState) {
        if (battleState == null) {
            return "غير قادر على تحليل الوضع الحالي";
        }
        
        StringBuilder suggestion = new StringBuilder();
        
        // Elixir-based suggestions
        if (battleState.playerElixir >= 8) {
            suggestion.append("🔥 إكسير ممتاز! وقت مثالي لهجمة قوية\n");
        } else if (battleState.playerElixir <= 3) {
            suggestion.append("⚠️ إكسير منخفض - ركز على الدفاع\n");
        }
        
        // Time-based suggestions
        switch (battleState.battlePhase) {
            case EARLY:
                suggestion.append("🏁 بداية المعركة - اكتشف أوراق الخصم\n");
                break;
            case MID:
                suggestion.append("⚔️ منتصف المعركة - كن استراتيجياً\n");
                break;
            case LATE:
                suggestion.append("⏰ الوقت ينفد! كن جريئاً في هجماتك\n");
                break;
        }
        
        // Crown-based suggestions
        if (battleState.playerCrowns > battleState.enemyCrowns) {
            suggestion.append("👑 أنت متقدم - حافظ على الميزة\n");
        } else if (battleState.playerCrowns < battleState.enemyCrowns) {
            suggestion.append("🎯 أنت متأخر - تحتاج لهجمات أقوى\n");
        }
        
        // Tower-based suggestions
        if (battleState.enemyTowers.size() < 3) {
            suggestion.append("🏰 برج العدو مدمر - اضغط للحصول على التاج\n");
        }
        
        // Card-based suggestions
        if (battleState.detectedEnemyCards.contains("Giant") && 
            battleState.playerCards.contains("Inferno Tower")) {
            suggestion.append("🗼 العدو لديه Giant - استخدم Inferno Tower\n");
        }
        
        if (battleState.detectedEnemyCards.contains("Minion Horde") && 
            battleState.playerCards.contains("Arrows")) {
            suggestion.append("🏹 العدو لديه Minion Horde - احتفظ بـ Arrows\n");
        }
        
        // Territory control suggestions
        if (battleState.enemyTerritoryControl > 0.7) {
            suggestion.append("🛡️ العدو يسيطر على أراضيه - هاجم من الجانبين\n");
        }
        
        // Default suggestion if nothing specific
        if (suggestion.length() == 0) {
            String[] genericTips = {
                "💡 راقب دورة بطاقات الخصم",
                "⚡ لا تهدر الإكسير بلا هدف",
                "🔄 استخدم تكتيك الهجوم المضاد",
                "🎯 ركز على هدف واحد في كل مرة",
                "⚖️ حافظ على توازن الهجوم والدفاع"
            };
            
            int randomIndex = (int)(Math.random() * genericTips.length);
            suggestion.append(genericTips[randomIndex]);
        }
        
        return suggestion.toString().trim();
    }
    
    // Battle state data class
    public static class BattleState {
        public int timeRemaining = 180; // 3 minutes default
        public int playerElixir = 5;
        public int playerCrowns = 0;
        public int enemyCrowns = 0;
        
        public List<String> playerCards = new ArrayList<>();
        public List<String> detectedEnemyCards = new ArrayList<>();
        
        public List<OpenCVManager.GameElement> playerTowers = new ArrayList<>();
        public List<OpenCVManager.GameElement> enemyTowers = new ArrayList<>();
        public List<OpenCVManager.GameElement> playerTroops = new ArrayList<>();
        public List<OpenCVManager.GameElement> enemyTroops = new ArrayList<>();
        
        public float playerTerritoryControl = 0.5f;
        public float enemyTerritoryControl = 0.5f;
        
        public BattlePhase battlePhase = BattlePhase.EARLY;
    }
    
    public enum BattlePhase {
        EARLY,  // First minute
        MID,    // Second minute
        LATE    // Final minute + overtime
    }
}