package ai.screentalk.ml.stt

import android.content.Context
import ai.screentalk.common.AppResult
import ai.screentalk.common.FileUtils
import ai.screentalk.common.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.vosk.Model

class VoskStt(private val context: Context) {

    @Volatile
    private var model: Model? = null

    suspend fun ensureModel(language: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (model != null) return@runCatching
            val modelsDir = FileUtils.getModelsDir(context)
            val voskDir = modelsDir.resolve("vosk/${language}")
            if (!voskDir.exists()) {
                throw IllegalStateException("Vosk model missing for ${'$'}language")
            }
            model = Model(voskDir.absolutePath)
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable ->
                Logger.e("Failed to load Vosk model", throwable)
                AppResult.Error(throwable)
            }
        )
    }

    suspend fun listen(language: String, durationMs: Long = 5_000L): AppResult<String> =
        withContext(Dispatchers.IO) {
            if (model == null) {
                val ensure = ensureModel(language)
                if (ensure is AppResult.Error) {
                    return@withContext AppResult.Error(ensure.throwable)
                }
            }
            // Placeholder: actual microphone capture requires AudioRecord integration.
            // We return an empty transcript while the full pipeline is implemented.
            delay(durationMs)
            AppResult.Success("")
        }
}
