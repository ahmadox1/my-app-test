package ai.screentalk.overlay.ui

sealed interface ChatMessage {
    val id: Long

    data class User(override val id: Long, val text: String) : ChatMessage
    data class Assistant(override val id: Long, val text: String) : ChatMessage
}
