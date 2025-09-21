package ai.screentalk.app.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SettingsState(
    val ttsEnabled: Boolean = true,
    val captureIntervalMs: Long = 1500L,
    val ocrEngine: OcrEngine = OcrEngine.MLKIT
)

enum class OcrEngine { MLKIT, TESSERACT }

class SettingsViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = mutableState

    fun toggleTts() {
        mutableState.value = mutableState.value.copy(ttsEnabled = !mutableState.value.ttsEnabled)
    }
}
