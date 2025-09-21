package ai.screentalk.ml.prompt

object PromptBuilder {
    private const val SYSTEM_PROMPT = "You are an on-device assistant. Answer questions about the CURRENT PHONE SCREEN only. If information is not visible, say you do not know."

    fun build(userQuestion: String, screenContext: String): String {
        return buildString {
            appendLine(SYSTEM_PROMPT)
            appendLine()
            appendLine("SCREEN CONTEXT:")
            appendLine(screenContext.ifBlank { "(context unavailable)" })
            appendLine()
            appendLine("USER QUESTION:")
            appendLine(userQuestion)
        }
    }
}
