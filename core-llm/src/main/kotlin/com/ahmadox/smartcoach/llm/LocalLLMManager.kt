package com.ahmadox.smartcoach.llm

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local LLM manager using llama.cpp for on-device inference.
 * 
 * This is a simplified implementation that handles model download and basic text generation.
 * In production, this would integrate with actual llama.cpp native bindings.
 */
@Singleton
class LocalLLMManager @Inject constructor(
    private val context: Context
) {
    private val httpClient = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    
    private var isModelLoaded = false
    private val modelDirectory = File(context.filesDir, "models")
    
    /**
     * Download and verify the AI model
     */
    suspend fun downloadModel(config: ModelConfig, onProgress: (Float) -> Unit): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                modelDirectory.mkdirs()
                val modelFile = File(modelDirectory, "model.gguf")
                
                if (modelFile.exists() && verifyModelChecksum(modelFile, config.modelSha256)) {
                    isModelLoaded = true
                    return@withContext true
                }
                
                // Download model (simplified - in production would handle chunked download)
                val request = Request.Builder()
                    .url(config.modelUrl)
                    .build()
                
                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) return@withContext false
                
                response.body?.let { body ->
                    val totalSize = body.contentLength()
                    var downloadedSize = 0L
                    
                    body.byteStream().use { input ->
                        modelFile.outputStream().use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                downloadedSize += bytesRead
                                
                                val progress = downloadedSize.toFloat() / totalSize.toFloat()
                                onProgress(progress)
                            }
                        }
                    }
                }
                
                // Verify downloaded model
                if (verifyModelChecksum(modelFile, config.modelSha256)) {
                    isModelLoaded = true
                    return@withContext true
                }
                
                false
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Generate strategy explanation using the local LLM
     */
    suspend fun generateStrategyExplanation(
        gameState: com.ahmadox.smartcoach.data.model.GameState,
        recommendation: com.ahmadox.smartcoach.data.model.StrategyRecommendation
    ): String {
        if (!isModelLoaded) {
            return "النموذج غير محمل. يرجى تحميل النموذج أولاً."
        }
        
        return withContext(Dispatchers.Default) {
            // Simplified text generation - in production would use llama.cpp
            generateMockExplanation(gameState, recommendation)
        }
    }
    
    /**
     * Load model configuration from remote or local fallback
     */
    suspend fun loadModelConfig(): ModelConfig {
        return try {
            // Try to load from remote config
            loadRemoteConfig() ?: getDefaultConfig()
        } catch (e: Exception) {
            getDefaultConfig()
        }
    }
    
    private suspend fun loadRemoteConfig(): ModelConfig? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://raw.githubusercontent.com/ahmadox1/my-app-test/main/remote_config.json")
                    .build()
                
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let { jsonString ->
                        json.decodeFromString<RemoteConfig>(jsonString).toModelConfig()
                    }
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun getDefaultConfig(): ModelConfig {
        return ModelConfig(
            modelUrl = "https://huggingface.co/microsoft/DialoGPT-small/resolve/main/pytorch_model.bin",
            modelSha256 = "dummy_sha256_hash",
            modelSizeMb = 117,
            modelQuality = com.ahmadox.smartcoach.data.model.ModelQuality.LOW
        )
    }
    
    private fun verifyModelChecksum(file: File, expectedSha256: String): Boolean {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
            actualHash == expectedSha256
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate mock explanation for testing purposes
     */
    private fun generateMockExplanation(
        gameState: com.ahmadox.smartcoach.data.model.GameState,
        recommendation: com.ahmadox.smartcoach.data.model.StrategyRecommendation
    ): String {
        val actionText = when (recommendation.action) {
            com.ahmadox.smartcoach.data.model.StrategyAction.ATTACK -> "الهجوم"
            com.ahmadox.smartcoach.data.model.StrategyAction.DEFENSE -> "الدفاع"
            com.ahmadox.smartcoach.data.model.StrategyAction.WAIT -> "الانتظار"
        }
        
        return buildString {
            appendLine("تحليل الوضع الحالي:")
            appendLine("• إكسيرك: ${gameState.myElixir}")
            appendLine("• إكسير الخصم: ${gameState.oppElixir}")
            appendLine("• عدد بطاقاتك: ${gameState.myHand.size}")
            appendLine("• وحدات الخصم: ${gameState.oppUnits.size}")
            appendLine()
            appendLine("الاقتراح: $actionText")
            appendLine("السبب: ${recommendation.reasoning}")
            appendLine()
            appendLine("نصائح إضافية:")
            when (recommendation.action) {
                com.ahmadox.smartcoach.data.model.StrategyAction.ATTACK -> {
                    appendLine("• استخدم البطاقات الهجومية بتسلسل صحيح")
                    appendLine("• راقب رد فعل الخصم واستعد للدفاع")
                }
                com.ahmadox.smartcoach.data.model.StrategyAction.DEFENSE -> {
                    appendLine("• ضع المباني الدفاعية في المواقع الصحيحة")
                    appendLine("• استخدم البطاقات منخفضة التكلفة أولاً")
                }
                com.ahmadox.smartcoach.data.model.StrategyAction.WAIT -> {
                    appendLine("• اجمع الإكسير واحتفظ بالبطاقات القوية")
                    appendLine("• راقب تحركات الخصم وكن مستعداً للرد")
                }
            }
        }
    }
    
    fun isModelReady(): Boolean = isModelLoaded
}

@Serializable
private data class RemoteConfig(
    val model_url: String,
    val model_sha256: String,
    val model_size_mb: Int,
    val model_quality: String = "medium"
) {
    fun toModelConfig(): ModelConfig {
        return ModelConfig(
            modelUrl = model_url,
            modelSha256 = model_sha256,
            modelSizeMb = model_size_mb,
            modelQuality = when (model_quality.lowercase()) {
                "high" -> com.ahmadox.smartcoach.data.model.ModelQuality.HIGH
                "low" -> com.ahmadox.smartcoach.data.model.ModelQuality.LOW
                else -> com.ahmadox.smartcoach.data.model.ModelQuality.MEDIUM
            }
        )
    }
}