package ai.screentalk.ml.llm

import ai.screentalk.common.AppResult
import ai.screentalk.ml.LocalModelEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class EchoEngine : LocalModelEngine {
    override fun isReady(): Boolean = true

    override suspend fun ensureModelAvailable(progress: (Int) -> Unit) {
        progress(100)
    }

    override suspend fun generateStream(
        prompt: String,
        params: LocalModelEngine.GenParams,
        onToken: (String) -> Unit
    ): AppResult<String> = withContext(Dispatchers.Default) {
        val reply = "Echo: ${'$'}prompt"
        val tokens = reply.split(" ")
        for (token in tokens) {
            onToken("${token} ")
            delay(40)
        }
        AppResult.Success(reply)
    }
}
