package com.ahmadox.smartcoach.strategy

import com.ahmadox.smartcoach.data.model.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the RuleBasedStrategyEngine.
 * 
 * These tests validate that the strategy rules work as expected and
 * provide clear reasoning for their recommendations.
 */
class RuleBasedStrategyEngineTest {
    
    private lateinit var strategyEngine: RuleBasedStrategyEngine
    
    @Before
    fun setup() {
        strategyEngine = RuleBasedStrategyEngine()
    }
    
    @Test
    fun `should recommend defense when opponent has high elixir and many units`() {
        // Given: Opponent has high elixir (8) and multiple units (3)
        val gameState = GameState(
            myElixir = 5,
            oppElixir = 8,
            myHand = createBalancedHand(),
            oppUnits = createMultipleUnits(3),
            events = emptyList()
        )
        
        // When: Analyzing the game state
        val recommendation = strategyEngine.analyzeGameState(gameState)
        
        // Then: Should recommend defense
        assertEquals(StrategyAction.DEFENSE, recommendation.action)
        assertEquals(Confidence.MEDIUM, recommendation.confidence)
        assertTrue(recommendation.reasoning.contains("الخصم لديه إكسير عالي"))
        assertFalse(recommendation.suggestedCards.isEmpty())
    }
    
    @Test
    fun `should recommend attack when we have elixir advantage and good cards`() {
        // Given: We have elixir advantage (6) and good hand quality
        val gameState = GameState(
            myElixir = 8,
            oppElixir = 4,
            myHand = createGoodAttackHand(),
            oppUnits = createSingleUnit(),
            events = emptyList()
        )
        
        // When: Analyzing the game state
        val recommendation = strategyEngine.analyzeGameState(gameState)
        
        // Then: Should recommend attack
        assertEquals(StrategyAction.ATTACK, recommendation.action)
        assertTrue(
            recommendation.confidence == Confidence.HIGH ||
            recommendation.confidence == Confidence.MEDIUM
        )
        assertTrue(recommendation.reasoning.contains("ميزة في الإكسير"))
        assertFalse(recommendation.suggestedCards.isEmpty())
    }
    
    @Test
    fun `should recommend wait when situation is balanced`() {
        // Given: Balanced situation - similar elixir, few units
        val gameState = GameState(
            myElixir = 5,
            oppElixir = 5,
            myHand = createAverageHand(),
            oppUnits = createSingleUnit(),
            events = emptyList()
        )
        
        // When: Analyzing the game state
        val recommendation = strategyEngine.analyzeGameState(gameState)
        
        // Then: Should recommend wait
        assertEquals(StrategyAction.WAIT, recommendation.action)
        assertEquals(Confidence.MEDIUM, recommendation.confidence)
        assertTrue(recommendation.reasoning.contains("متوازن"))
    }
    
    @Test
    fun `should recommend defense with high confidence when opponent is very threatening`() {
        // Given: Very threatening opponent (9 elixir, 4 units)
        val gameState = GameState(
            myElixir = 4,
            oppElixir = 9,
            myHand = createDefensiveHand(),
            oppUnits = createMultipleUnits(4),
            events = emptyList()
        )
        
        // When: Analyzing the game state
        val recommendation = strategyEngine.analyzeGameState(gameState)
        
        // Then: Should recommend defense with high confidence
        assertEquals(StrategyAction.DEFENSE, recommendation.action)
        assertEquals(Confidence.HIGH, recommendation.confidence)
    }
    
    @Test
    fun `should recommend attack with high confidence when we have major advantage`() {
        // Given: Major advantage (9 elixir vs 2, great hand)
        val gameState = GameState(
            myElixir = 9,
            oppElixir = 2,
            myHand = createExcellentAttackHand(),
            oppUnits = emptyList(),
            events = emptyList()
        )
        
        // When: Analyzing the game state
        val recommendation = strategyEngine.analyzeGameState(gameState)
        
        // Then: Should recommend attack with high confidence
        assertEquals(StrategyAction.ATTACK, recommendation.action)
        assertEquals(Confidence.HIGH, recommendation.confidence)
    }
    
    @Test
    fun `should provide defensive cards when recommending defense`() {
        // Given: Defense scenario
        val gameState = GameState(
            myElixir = 4,
            oppElixir = 8,
            myHand = listOf(
                Card("Cannon", 3, CardType.BUILDING),
                Card("Skeletons", 1, CardType.TROOP),
                Card("Fireball", 4, CardType.SPELL),
                Card("Giant", 5, CardType.TROOP)
            ),
            oppUnits = createMultipleUnits(3),
            events = emptyList()
        )
        
        // When: Analyzing the game state
        val recommendation = strategyEngine.analyzeGameState(gameState)
        
        // Then: Should suggest defensive cards
        assertEquals(StrategyAction.DEFENSE, recommendation.action)
        assertTrue(recommendation.suggestedCards.contains("Cannon"))
        assertTrue(recommendation.suggestedCards.contains("Skeletons"))
    }
    
    @Test
    fun `should provide offensive cards when recommending attack`() {
        // Given: Attack scenario
        val gameState = GameState(
            myElixir = 8,
            oppElixir = 3,
            myHand = listOf(
                Card("Giant", 5, CardType.TROOP),
                Card("Wizard", 5, CardType.TROOP),
                Card("Arrows", 3, CardType.SPELL),
                Card("Skeletons", 1, CardType.TROOP)
            ),
            oppUnits = emptyList(),
            events = emptyList()
        )
        
        // When: Analyzing the game state
        val recommendation = strategyEngine.analyzeGameState(gameState)
        
        // Then: Should suggest offensive cards
        assertEquals(StrategyAction.ATTACK, recommendation.action)
        assertTrue(recommendation.suggestedCards.contains("Giant"))
        assertTrue(recommendation.suggestedCards.contains("Wizard"))
    }
    
    // Helper methods to create test data
    
    private fun createBalancedHand(): List<Card> {
        return listOf(
            Card("Knight", 3, CardType.TROOP),
            Card("Archers", 3, CardType.TROOP),
            Card("Fireball", 4, CardType.SPELL),
            Card("Cannon", 3, CardType.BUILDING)
        )
    }
    
    private fun createGoodAttackHand(): List<Card> {
        return listOf(
            Card("Giant", 5, CardType.TROOP),
            Card("Musketeer", 4, CardType.TROOP),
            Card("Fireball", 4, CardType.SPELL),
            Card("Zap", 2, CardType.SPELL)
        )
    }
    
    private fun createExcellentAttackHand(): List<Card> {
        return listOf(
            Card("Giant", 5, CardType.TROOP),
            Card("Wizard", 5, CardType.TROOP),
            Card("Barbarians", 5, CardType.TROOP),
            Card("Lightning", 6, CardType.SPELL)
        )
    }
    
    private fun createDefensiveHand(): List<Card> {
        return listOf(
            Card("Cannon", 3, CardType.BUILDING),
            Card("Tesla", 4, CardType.BUILDING),
            Card("Skeletons", 1, CardType.TROOP),
            Card("Arrows", 3, CardType.SPELL)
        )
    }
    
    private fun createAverageHand(): List<Card> {
        return listOf(
            Card("Knight", 3, CardType.TROOP),
            Card("Fireball", 4, CardType.SPELL),
            Card("Giant", 5, CardType.TROOP)
        )
    }
    
    private fun createSingleUnit(): List<Unit> {
        return listOf(
            Unit("Enemy Knight", Position(0.5f, 0.3f), 100, false)
        )
    }
    
    private fun createMultipleUnits(count: Int): List<Unit> {
        return (1..count).map { index ->
            Unit(
                "Enemy Unit $index", 
                Position(0.3f + index * 0.1f, 0.4f), 
                80 + index * 10, 
                false
            )
        }
    }
}