package ai.screentalk.ml.llm

import android.content.Context
import ai.screentalk.common.AppResult
import ai.screentalk.common.Files
import ai.screentalk.common.Logger
import ai.screentalk.ml.LocalModelEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class LlamaCppEngine(private val context: Context) : LocalModelEngine {
    private val initialized = AtomicBoolean(false)
    private var modelPath: String? = null

    init {
        runCatching { System.loadLibrary("llama_bridge") }
            .onFailure { Logger.e("Failed to load llama bridge", it) }
    }

    override fun isReady(): Boolean = initialized.get()

    override suspend fun ensureModelAvailable(onProgress: (Int) -> Unit) {
        val modelsDir = Files.modelsDir(context)
        val defaultModel = resolveDefaultModel(modelsDir)
        if (defaultModel != null && defaultModel.exists()) {
            val success = nativeInit(defaultModel.absolutePath)
            if (success) {
                modelPath = defaultModel.absolutePath
                initialized.set(true)
                onProgress(100)
            } else {
                initialized.set(false)
                onProgress(0)
            }
        } else {
            initialized.set(false)
            onProgress(0)
        }
    }

    override suspend fun generateStream(
        prompt: String,
        params: LocalModelEngine.GenParams,
        onToken: (String) -> Unit
    ): AppResult<String> = withContext(Dispatchers.IO) {
        if (!isReady()) {
            return@withContext AppResult.Error(IllegalStateException("Model not initialized"))
        }
        val tokens = nativeGenerate(prompt, params.temperature, params.topP, params.maxTokens)
            ?: return@withContext AppResult.Error(IllegalStateException("Generation failed"))
        tokens.forEach(onToken)
        AppResult.Success(tokens.joinToString(separator = ""))
    }

    fun release() {
        nativeRelease()
        initialized.set(false)
        modelPath = null
    }

    private fun resolveDefaultModel(modelsDir: File): File? {
        val preferred = File(modelsDir, "small/model.gguf")
        if (preferred.exists()) return preferred
        return modelsDir.walkTopDown().firstOrNull { it.isFile && it.extension == "gguf" }
    }

    private external fun nativeInit(modelPath: String): Boolean
    private external fun nativeRelease()
    private external fun nativeGenerate(prompt: String, temperature: Float, topP: Float, maxTokens: Int): Array<String>?
}
