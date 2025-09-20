package com.ahmadox.smartcoach.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StrategyRecommendation(
    val action: StrategyAction,
    val confidence: Confidence,
    val reasoning: String,
    val suggestedCards: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class StrategyAction {
    ATTACK, DEFENSE, WAIT
}

enum class Confidence {
    HIGH, MEDIUM, LOW
}

@Serializable
data class ModelConfig(
    val modelUrl: String,
    val modelSha256: String,
    val modelSizeMb: Int,
    val modelQuality: ModelQuality
)

enum class ModelQuality {
    HIGH, MEDIUM, LOW
}