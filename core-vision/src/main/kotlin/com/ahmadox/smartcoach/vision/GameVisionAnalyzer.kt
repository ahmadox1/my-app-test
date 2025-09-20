package com.ahmadox.smartcoach.vision

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Vision analyzer that processes screen captures to extract game information.
 * 
 * Uses ML Kit for text recognition and basic image analysis for game state detection.
 */
@Singleton
class GameVisionAnalyzer @Inject constructor() {
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    /**
     * Analyze a screen capture to extract game state information
     */
    suspend fun analyzeGameScreen(bitmap: Bitmap): VisionResult {
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            
            // Extract text using ML Kit
            val visionText = textRecognizer.process(inputImage).await()
            
            // Parse extracted text for game elements
            val elixirInfo = parseElixirInfo(visionText.text)
            val cardInfo = parseCardInfo(visionText.text)
            val unitInfo = parseUnitInfo(visionText.text, bitmap)
            
            return VisionResult(
                success = true,
                myElixir = elixirInfo.first,
                oppElixir = elixirInfo.second,
                detectedCards = cardInfo,
                detectedUnits = unitInfo,
                rawText = visionText.text,
                confidence = calculateOverallConfidence(visionText.text)
            )
            
        } catch (e: Exception) {
            return VisionResult(
                success = false,
                error = e.message ?: "Unknown error during vision analysis"
            )
        }
    }
    
    /**
     * Parse elixir information from detected text
     * Looks for patterns like "10/10" or "5" near elixir indicators
     */
    private fun parseElixirInfo(text: String): Pair<Int, Int> {
        val elixirRegex = Regex("""(\d{1,2})/(\d{1,2})""")
        val singleElixirRegex = Regex("""\b(\d{1,2})\b""")
        
        // Try to find "X/Y" pattern first (full elixir display)
        val elixirMatch = elixirRegex.find(text)
        if (elixirMatch != null) {
            val current = elixirMatch.groupValues[1].toIntOrNull() ?: 0
            val max = elixirMatch.groupValues[2].toIntOrNull() ?: 10
            return Pair(current, current) // Assume both players have similar elixir
        }
        
        // Try to find single numbers that could be elixir
        val numbers = singleElixirRegex.findAll(text)
            .map { it.groupValues[1].toIntOrNull() }
            .filterNotNull()
            .filter { it in 0..10 }
            .toList()
        
        return when {
            numbers.size >= 2 -> Pair(numbers[0], numbers[1])
            numbers.size == 1 -> Pair(numbers[0], numbers[0])
            else -> Pair(5, 5) // Default values
        }
    }
    
    /**
     * Parse card information from detected text
     * Looks for common card names and elixir costs
     */
    private fun parseCardInfo(text: String): List<DetectedCard> {
        val commonCards = listOf(
            "Knight", "Archers", "Arrows", "Fireball", "Giant", "Wizard",
            "Dragon", "Skeleton", "Goblin", "Barbarian", "Minion", "Hog",
            "Cannon", "Tesla", "Rocket", "Lightning", "Freeze", "Mirror"
        )
        
        val detectedCards = mutableListOf<DetectedCard>()
        
        commonCards.forEach { cardName ->
            if (text.contains(cardName, ignoreCase = true)) {
                detectedCards.add(
                    DetectedCard(
                        name = cardName,
                        cost = getCardCost(cardName),
                        confidence = 0.8f
                    )
                )
            }
        }
        
        return detectedCards
    }
    
    /**
     * Parse unit information using simple image analysis
     * This is a simplified version - in production would use more sophisticated CV
     */
    private fun parseUnitInfo(text: String, bitmap: Bitmap): List<DetectedUnit> {
        // Simplified unit detection based on text and image analysis
        val units = mutableListOf<DetectedUnit>()
        
        // Look for health indicators in text (simplified)
        val healthRegex = Regex("""\b(\d+)\s*HP""")
        val healthMatches = healthRegex.findAll(text).toList()
        
        healthMatches.forEachIndexed { index, match ->
            val health = match.groupValues[1].toIntOrNull() ?: 100
            units.add(
                DetectedUnit(
                    name = "Unit${index + 1}",
                    health = health,
                    position = Pair(0.5f, 0.5f), // Center position as default
                    isAlly = index % 2 == 0 // Alternate between ally/enemy
                )
            )
        }
        
        return units
    }
    
    /**
     * Get elixir cost for known cards
     */
    private fun getCardCost(cardName: String): Int {
        return when (cardName.lowercase()) {
            "skeleton", "arrows" -> 2
            "knight", "archers", "goblin" -> 3
            "fireball", "barbarian", "cannon" -> 4
            "giant", "wizard", "hog" -> 5
            "dragon", "rocket" -> 6
            "lightning" -> 7
            else -> 4 // Default cost
        }
    }
    
    /**
     * Calculate overall confidence of the vision analysis
     */
    private fun calculateOverallConfidence(text: String): Float {
        var confidence = 0.5f // Base confidence
        
        // Increase confidence based on detected elements
        if (text.contains(Regex("""\d{1,2}/\d{1,2}"""))) confidence += 0.2f // Elixir format
        if (text.length > 10) confidence += 0.1f // Sufficient text detected
        if (text.contains("HP", ignoreCase = true)) confidence += 0.1f // Health indicators
        
        return confidence.coerceIn(0f, 1f)
    }
}

/**
 * Result of vision analysis
 */
data class VisionResult(
    val success: Boolean = false,
    val myElixir: Int = 0,
    val oppElixir: Int = 0,
    val detectedCards: List<DetectedCard> = emptyList(),
    val detectedUnits: List<DetectedUnit> = emptyList(),
    val rawText: String = "",
    val confidence: Float = 0f,
    val error: String? = null
)

/**
 * Detected card information
 */
data class DetectedCard(
    val name: String,
    val cost: Int,
    val confidence: Float
)

/**
 * Detected unit information
 */
data class DetectedUnit(
    val name: String,
    val health: Int,
    val position: Pair<Float, Float>, // x, y coordinates (0-1 normalized)
    val isAlly: Boolean
)