package com.ahmadox.smartcoach.strategy

import com.ahmadox.smartcoach.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Rule-based strategy engine that analyzes game state and provides recommendations.
 * 
 * This engine uses clear, explainable rules to suggest strategic actions.
 * The rules are designed to be easily understood and modified.
 */
@Singleton
class RuleBasedStrategyEngine @Inject constructor() {
    
    /**
     * Analyzes the current game state and provides a strategic recommendation.
     * 
     * Rules applied:
     * 1. If opponent has high elixir (8+) and many units, recommend DEFENSE
     * 2. If we have elixir advantage (3+) and good cards, recommend ATTACK  
     * 3. If elixir is low (4-) or cards are not optimal, recommend WAIT
     * 4. Consider opponent unit count and positioning
     */
    fun analyzeGameState(gameState: GameState): StrategyRecommendation {
        val elixirAdvantage = gameState.myElixir - gameState.oppElixir
        val oppUnitCount = gameState.oppUnits.size
        val myHandQuality = calculateHandQuality(gameState.myHand)
        
        return when {
            // Rule 1: Defense when opponent is threatening
            shouldDefend(gameState, oppUnitCount) -> {
                StrategyRecommendation(
                    action = StrategyAction.DEFENSE,
                    confidence = calculateDefenseConfidence(gameState, oppUnitCount),
                    reasoning = "الخصم لديه إكسير عالي (${gameState.oppElixir}) ووحدات متعددة ($oppUnitCount). يُنصح بالدفاع لصد الهجوم القادم.",
                    suggestedCards = getDefensiveCards(gameState.myHand)
                )
            }
            
            // Rule 2: Attack when we have advantage
            shouldAttack(gameState, elixirAdvantage, myHandQuality) -> {
                StrategyRecommendation(
                    action = StrategyAction.ATTACK,
                    confidence = calculateAttackConfidence(elixirAdvantage, myHandQuality),
                    reasoning = "لديك ميزة في الإكسير (+$elixirAdvantage) وبطاقات جيدة. الوقت مناسب للهجوم.",
                    suggestedCards = getOffensiveCards(gameState.myHand)
                )
            }
            
            // Rule 3: Wait for better opportunity
            else -> {
                StrategyRecommendation(
                    action = StrategyAction.WAIT,
                    confidence = Confidence.MEDIUM,
                    reasoning = "الوضع متوازن. انتظر فرصة أفضل لجمع الإكسير أو الحصول على بطاقات أقوى.",
                    suggestedCards = emptyList()
                )
            }
        }
    }
    
    /**
     * Rule: Defend when opponent has high elixir (7+) and multiple units (2+)
     */
    private fun shouldDefend(gameState: GameState, oppUnitCount: Int): Boolean {
        return gameState.oppElixir >= 7 && oppUnitCount >= 2
    }
    
    /**
     * Rule: Attack when we have elixir advantage (3+) and decent hand quality (0.6+)
     */
    private fun shouldAttack(gameState: GameState, elixirAdvantage: Int, handQuality: Double): Boolean {
        return elixirAdvantage >= 3 && handQuality >= 0.6 && gameState.myElixir >= 6
    }
    
    /**
     * Calculate hand quality based on card types and costs
     * Returns value between 0.0 and 1.0
     */
    private fun calculateHandQuality(hand: List<Card>): Double {
        if (hand.isEmpty()) return 0.0
        
        val balanceScore = calculateHandBalance(hand)
        val costEfficiencyScore = calculateCostEfficiency(hand)
        
        return (balanceScore + costEfficiencyScore) / 2.0
    }
    
    /**
     * Check if hand has balanced mix of card types
     */
    private fun calculateHandBalance(hand: List<Card>): Double {
        val troops = hand.count { it.type == CardType.TROOP }
        val spells = hand.count { it.type == CardType.SPELL }
        val buildings = hand.count { it.type == CardType.BUILDING }
        
        // Prefer balanced mix
        return when {
            troops >= 2 && spells >= 1 -> 0.8
            troops >= 1 && spells >= 1 -> 0.6
            troops >= 2 -> 0.5
            else -> 0.3
        }
    }
    
    /**
     * Calculate cost efficiency (prefer variety of costs)
     */
    private fun calculateCostEfficiency(hand: List<Card>): Double {
        if (hand.isEmpty()) return 0.0
        
        val avgCost = hand.map { it.cost }.average()
        return when {
            avgCost in 3.0..5.0 -> 0.8  // Optimal average cost
            avgCost in 2.0..6.0 -> 0.6  // Good cost range
            else -> 0.4  // Too expensive or too cheap
        }
    }
    
    /**
     * Calculate confidence for defense recommendation
     */
    private fun calculateDefenseConfidence(gameState: GameState, oppUnitCount: Int): Confidence {
        return when {
            gameState.oppElixir >= 9 && oppUnitCount >= 3 -> Confidence.HIGH
            gameState.oppElixir >= 7 && oppUnitCount >= 2 -> Confidence.MEDIUM
            else -> Confidence.LOW
        }
    }
    
    /**
     * Calculate confidence for attack recommendation
     */
    private fun calculateAttackConfidence(elixirAdvantage: Int, handQuality: Double): Confidence {
        return when {
            elixirAdvantage >= 5 && handQuality >= 0.8 -> Confidence.HIGH
            elixirAdvantage >= 3 && handQuality >= 0.6 -> Confidence.MEDIUM
            else -> Confidence.LOW
        }
    }
    
    /**
     * Get defensive cards from hand
     */
    private fun getDefensiveCards(hand: List<Card>): List<String> {
        return hand.filter { 
            it.type == CardType.BUILDING || it.cost <= 4 
        }.map { it.name }
    }
    
    /**
     * Get offensive cards from hand
     */
    private fun getOffensiveCards(hand: List<Card>): List<String> {
        return hand.filter { 
            it.type == CardType.TROOP && it.cost >= 3 
        }.map { it.name }
    }
}