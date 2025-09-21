package ai.screentalk.ml.llm

import android.content.Context
import ai.screentalk.common.AppResult
import ai.screentalk.common.FileUtils
import ai.screentalk.common.Logger
import ai.screentalk.ml.LocalModelEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class LlamaCppEngine(private val context: Context) : LocalModelEngine {

    private val initialized = AtomicBoolean(false)
    private var modelPath: File? = null

    init {
        runCatching {
            System.loadLibrary("llama_bridge")
        }.onFailure {
            Logger.e("Unable to load llama bridge", it)
        }
    }

    override fun isReady(): Boolean = initialized.get()

    override suspend fun ensureModelAvailable(progress: (Int) -> Unit) {
        // In a full implementation we would download the model and report progress.
        // Here we simply check if a default model exists.
        val modelsDir = FileUtils.getModelsDir(context)
        val defaultModel = File(modelsDir, "default/model.gguf")
        if (defaultModel.exists()) {
            modelPath = defaultModel
            initialized.set(true)
        } else {
            initialized.set(false)
        }
    }

    override suspend fun generateStream(
        prompt: String,
        params: LocalModelEngine.GenParams,
        onToken: (String) -> Unit
    ): AppResult<String> = withContext(Dispatchers.Default) {
        val model = modelPath
        if (!initialized.get() || model == null) {
            return@withContext AppResult.Error(IllegalStateException("Model not initialised"))
        }

        return@withContext runCatching {
            val builder = StringBuilder()
            val tokens = nativeGenerate(
                prompt,
                params.temperature,
                params.topP,
                params.maxTokens
            )
            tokens.forEach { token ->
                builder.append(token)
                onToken(token)
            }
            builder.toString()
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { error ->
                Logger.e("native inference failed", error)
                AppResult.Error(error)
            }
        )
    }

    private external fun nativeGenerate(
        prompt: String,
        temperature: Float,
        topP: Float,
        maxTokens: Int
    ): Array<String>
}
