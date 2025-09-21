package ai.screentalk.ml

import ai.screentalk.common.AppResult

interface LocalModelEngine {
    data class GenParams(
        val temperature: Float,
        val maxTokens: Int,
        val topP: Float
    )

    fun isReady(): Boolean

    suspend fun ensureModelAvailable(progress: (Int) -> Unit = {})

    suspend fun generateStream(
        prompt: String,
        params: GenParams,
        onToken: (String) -> Unit
    ): AppResult<String>
}
