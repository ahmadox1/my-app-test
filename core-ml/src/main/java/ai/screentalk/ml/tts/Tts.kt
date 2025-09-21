package ai.screentalk.ml.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class Tts(context: Context) : TextToSpeech.OnInitListener {
    private val initialized = AtomicBoolean(false)
    private val tts = TextToSpeech(context.applicationContext, this)
    private var pendingUtterance: String? = null
    private var preferredLocale: Locale = Locale.US

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            initialized.set(true)
            tts.language = preferredLocale
            pendingUtterance?.let { speak(it) }
            pendingUtterance = null
        }
    }

    fun setLocale(locale: Locale) {
        preferredLocale = locale
        if (initialized.get()) {
            tts.language = locale
        }
    }

    fun speak(text: String) {
        if (!initialized.get()) {
            pendingUtterance = text
            return
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun shutdown() {
        initialized.set(false)
        tts.stop()
        tts.shutdown()
    }

    companion object {
        private const val UTTERANCE_ID = "screen_talk_reply"
    }
}
