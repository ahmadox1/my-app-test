package ai.screentalk.ml.prompt

object PromptBuilder {
    private const val SYSTEM_PROMPT = "You are ScreenTalk, an on-device assistant that helps describe the current phone screen." +
        " Answer concisely and mention when information is not visible."

    fun build(userQuestion: String, screenContext: String): String {
        val languageNote = if (userQuestion.containsArabic()) {
            "Respond in Arabic unless the user switches language."
        } else {
            "Respond in the user's language."
        }
        return buildString {
            appendLine(SYSTEM_PROMPT)
            appendLine(languageNote)
            appendLine()
            appendLine("SCREEN CONTEXT:")
            appendLine(screenContext.ifBlank { "No visible text captured." })
            appendLine()
            appendLine("USER QUESTION:")
            appendLine(userQuestion)
            appendLine()
            appendLine("Answer:")
        }
    }

    private fun String.containsArabic(): Boolean = any { it.code in 0x0600..0x06FF }
}
