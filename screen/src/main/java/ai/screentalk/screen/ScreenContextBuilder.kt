package ai.screentalk.screen

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object ScreenContextBuilder {
    data class ScreenContext(
        val appPackage: String = "",
        val activity: String = "",
        val ocrText: String = "",
        val accessibilityText: String = "",
        val focusedNode: String = "",
        val timestamp: Long = 0L
    )

    private val state = MutableStateFlow(ScreenContext())

    fun state(): StateFlow<ScreenContext> = state

    fun updateApp(packageName: String?, activityName: String?) {
        if (packageName.isNullOrEmpty() && activityName.isNullOrEmpty()) return
        state.update {
            it.copy(
                appPackage = packageName.orEmpty(),
                activity = activityName.orEmpty(),
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun updateOcr(text: String) {
        if (text.isBlank()) return
        state.update {
            it.copy(
                ocrText = text.take(MAX_TEXT_CHARS),
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun updateAccessibility(text: String, focused: String?, packageName: String?) {
        if (text.isBlank() && focused.isNullOrBlank()) return
        state.update {
            it.copy(
                appPackage = packageName ?: it.appPackage,
                accessibilityText = text.take(MAX_TEXT_CHARS),
                focusedNode = focused.orEmpty().take(MAX_TEXT_CHARS),
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun lastContext(): String {
        val snapshot = state.value
        val builder = StringBuilder()
        if (snapshot.appPackage.isNotBlank()) {
            builder.append("App: ${snapshot.appPackage}")
            if (snapshot.activity.isNotBlank()) {
                builder.append("/${snapshot.activity}")
            }
            builder.append(" • ")
        }
        if (snapshot.ocrText.isNotBlank()) {
            builder.append("OCR: \"")
            builder.append(snapshot.ocrText.take(200))
            builder.append("\"")
            builder.append(" • ")
        }
        if (snapshot.accessibilityText.isNotBlank()) {
            builder.append("UI: ")
            builder.append(snapshot.accessibilityText.take(200))
            builder.append(" • ")
        }
        if (snapshot.focusedNode.isNotBlank()) {
            builder.append("Focus: ")
            builder.append(snapshot.focusedNode.take(80))
        }
        if (builder.isEmpty()) {
            builder.append("No screen context available")
        }
        return builder.toString()
    }

    fun reset() {
        state.value = ScreenContext()
    }

    private const val MAX_TEXT_CHARS = 600
}
