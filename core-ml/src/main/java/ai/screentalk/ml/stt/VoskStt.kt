package ai.screentalk.ml.stt

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import ai.screentalk.common.AppResult
import ai.screentalk.common.Files
import ai.screentalk.common.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.util.concurrent.atomic.AtomicReference

class VoskStt(private val context: Context) {
    private val currentModel = AtomicReference<Model?>()
    private var currentLanguage: String? = null

    suspend fun listen(language: String): AppResult<String> = withContext(Dispatchers.IO) {
        var recognizer: Recognizer? = null
        var audioRecord: AudioRecord? = null
        try {
            val model = loadModel(language) ?: return@withContext AppResult.Error(IllegalStateException("Model missing for $language"))
            recognizer = Recognizer(model, SAMPLE_RATE.toFloat())
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext AppResult.Error(IllegalStateException("Unable to init microphone"))
            }
            audioRecord.startRecording()
            val buffer = ByteArray(4096)
            val startTime = SystemClock.elapsedRealtime()
            var silenceStart = startTime
            var lastResult = ""
            while (SystemClock.elapsedRealtime() - startTime < MAX_RECORDING_MS) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read <= 0) continue
                val hasResult = recognizer.acceptWaveForm(buffer, read)
                val partial = recognizer.partialResult
                if (hasSpoken(partial)) {
                    silenceStart = SystemClock.elapsedRealtime()
                }
                if (hasResult) {
                    lastResult = recognizer.result
                }
                if (SystemClock.elapsedRealtime() - silenceStart > SILENCE_TIMEOUT_MS) {
                    break
                }
            }
            val finalJson = if (lastResult.isNotEmpty()) lastResult else recognizer.finalResult
            val transcript = JSONObject(finalJson).optString("text").trim()
            if (transcript.isNotEmpty()) {
                AppResult.Success(transcript)
            } else {
                AppResult.Error(IllegalStateException("No speech detected"))
            }
        } catch (t: Throwable) {
            Logger.e("Vosk STT failure", t)
            AppResult.Error(t)
        } finally {
            recognizer?.close()
            audioRecord?.let {
                try {
                    if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        it.stop()
                    }
                } catch (_: IllegalStateException) {
                }
                it.release()
            }
        }
    }

    private fun hasSpoken(partialResult: String): Boolean {
        if (partialResult.isBlank()) return false
        val text = runCatching { JSONObject(partialResult).optString("partial") }.getOrDefault("")
        return text.isNotBlank()
    }

    private fun loadModel(language: String): Model? {
        if (currentLanguage == language) {
            currentModel.get()?.let { return it }
        }
        val modelDir = File(Files.sttDir(context), language)
        if (!modelDir.exists()) {
            Logger.e("Vosk model directory missing: ${modelDir.absolutePath}")
            return null
        }
        return try {
            currentModel.getAndSet(Model(modelDir.absolutePath))?.close()
            currentLanguage = language
            currentModel.get()
        } catch (t: Throwable) {
            Logger.e("Failed to load Vosk model", t)
            currentModel.set(null)
            null
        }
    }

    fun shutdown() {
        currentModel.getAndSet(null)?.close()
    }

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val MAX_RECORDING_MS = 10_000L
        private const val SILENCE_TIMEOUT_MS = 1_500L
    }
}
