package ai.screentalk.ml.llm

import ai.screentalk.common.AppResult
import ai.screentalk.ml.LocalModelEngine
import kotlinx.coroutines.delay

class EchoEngine : LocalModelEngine {
    override fun isReady(): Boolean = true

    override suspend fun ensureModelAvailable(onProgress: (Int) -> Unit) {
        onProgress(100)
    }

    override suspend fun generateStream(
        prompt: String,
        params: LocalModelEngine.GenParams,
        onToken: (String) -> Unit
    ): AppResult<String> {
        val response = "Echo: ${prompt.take(200)}"
        response.chunked(16).forEach {
            delay(10)
            onToken(it)
        }
        return AppResult.Success(response)
    }
}
