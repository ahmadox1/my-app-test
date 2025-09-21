package ai.screentalk.ml.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import ai.screentalk.common.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class Tts(context: Context) {
    private val tts = TextToSpeech(context) { status ->
        Logger.d("TTS init status: ${'$'}status")
    }

    fun setVoice(locale: Locale) {
        tts.language = locale
    }

    suspend fun speak(text: String) = withContext(Dispatchers.Main) {
        val completion = CompletableDeferred<Unit>()
        val utteranceId = System.currentTimeMillis().toString()
        tts.setOnUtteranceCompletedListener {
            completion.complete(Unit)
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        completion.await()
    }

    fun shutdown() {
        tts.shutdown()
    }
}
