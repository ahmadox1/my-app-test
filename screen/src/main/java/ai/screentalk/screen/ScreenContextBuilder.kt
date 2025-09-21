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
        state.update {
            it.copy(
                appPackage = packageName.orEmpty(),
                activity = activityName.orEmpty(),
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun updateOcr(text: String) {
        state.update {
            it.copy(ocrText = text, timestamp = System.currentTimeMillis())
        }
    }

    fun updateAccessibility(text: String, focused: String) {
        state.update {
            it.copy(
                accessibilityText = text,
                focusedNode = focused,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun lastContext(): String {
        val snapshot = state.value
        val builder = StringBuilder()
        if (snapshot.appPackage.isNotEmpty()) {
            builder.append("App: ${snapshot.appPackage}")
            if (snapshot.activity.isNotEmpty()) {
                builder.append(" / ${snapshot.activity}")
            }
            builder.append('•')
        }
        if (snapshot.ocrText.isNotBlank()) {
            builder.append(" OCR: \"")
            builder.append(snapshot.ocrText.take(200))
            builder.append("\"")
        }
        if (snapshot.accessibilityText.isNotBlank()) {
            builder.append(" • UI: ")
            builder.append(snapshot.accessibilityText.take(200))
        }
        if (snapshot.focusedNode.isNotBlank()) {
            builder.append(" • Focused: ")
            builder.append(snapshot.focusedNode)
        }
        val context = builder.toString()
        return if (context.length > 600) context.take(600) else context
    }
}
