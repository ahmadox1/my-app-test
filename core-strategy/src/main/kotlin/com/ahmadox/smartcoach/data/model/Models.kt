package com.ahmadox.smartcoach.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val myElixir: Int = 0,
    val oppElixir: Int = 0,
    val myHand: List<Card> = emptyList(),
    val oppUnits: List<Unit> = emptyList(),
    val events: List<GameEvent> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Card(
    val name: String,
    val cost: Int,
    val type: CardType,
    val level: Int = 1
)

@Serializable
data class Unit(
    val name: String,
    val position: Position,
    val health: Int,
    val isAlly: Boolean
)

@Serializable
data class Position(
    val x: Float,
    val y: Float
)

@Serializable
data class GameEvent(
    val type: EventType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class StrategyRecommendation(
    val action: StrategyAction,
    val confidence: Confidence,
    val reasoning: String,
    val suggestedCards: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ModelConfig(
    val modelUrl: String,
    val modelSha256: String,
    val modelSizeMb: Int,
    val modelQuality: ModelQuality
)

enum class CardType {
    TROOP, SPELL, BUILDING
}

enum class EventType {
    CARD_PLAYED, UNIT_DESTROYED, ELIXIR_CHANGE, BATTLE_START, BATTLE_END
}

enum class StrategyAction {
    ATTACK, DEFENSE, WAIT
}

enum class Confidence {
    HIGH, MEDIUM, LOW
}

enum class ModelQuality {
    HIGH, MEDIUM, LOW
}