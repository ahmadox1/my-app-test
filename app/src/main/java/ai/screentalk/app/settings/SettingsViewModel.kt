package ai.screentalk.app.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    fun toggleAccessibility(enabled: Boolean) {
        _state.update { it.copy(useAccessibility = enabled) }
    }

    fun setOcrEngine(engine: OcrEngine) {
        _state.update { it.copy(ocrEngine = engine) }
    }

    fun setCaptureInterval(intervalMs: Long) {
        _state.update { it.copy(captureIntervalMs = intervalMs) }
    }

    fun setTtsEnabled(enabled: Boolean) {
        _state.update { it.copy(ttsEnabled = enabled) }
    }

    fun setSaveScreens(enabled: Boolean) {
        _state.update { it.copy(saveScreenshots = enabled) }
    }
}

data class SettingsState(
    val ocrEngine: OcrEngine = OcrEngine.MLKit,
    val useAccessibility: Boolean = true,
    val captureIntervalMs: Long = 1_500L,
    val ttsEnabled: Boolean = true,
    val saveScreenshots: Boolean = false
)

enum class OcrEngine { MLKit, Tesseract }
