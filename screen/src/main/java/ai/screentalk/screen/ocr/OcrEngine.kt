package ai.screentalk.screen.ocr

import android.content.Context
import android.graphics.Bitmap
import ai.screentalk.common.FileUtils
import ai.screentalk.common.Logger
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface OcrEngine {
    suspend fun extractText(bitmap: Bitmap): String
}

class MLKitOcrEngine(context: Context) : OcrEngine {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extractText(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        return recognizer.process(image).await().text
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { error ->
            if (continuation.isActive) continuation.resumeWithException(error)
        }
        addOnCanceledListener {
            if (continuation.isActive) continuation.cancel()
        }
    }
}

class TessOcrEngine(private val context: Context, private val language: String = "ara") : OcrEngine {
    private val tessBaseAPI = TessBaseAPI()
    @Volatile
    private var initialised = false

    override suspend fun extractText(bitmap: Bitmap): String {
        ensureInit()
        return withContext(Dispatchers.IO) {
            tessBaseAPI.setImage(bitmap)
            tessBaseAPI.utF8Text.orEmpty()
        }
    }

    private fun ensureInit() {
        if (initialised) return
        synchronized(this) {
            if (initialised) return
            val tessDir = File(FileUtils.getModelsDir(context), "tesseract")
            val tessData = File(tessDir, "tessdata/$language.traineddata")
            if (!tessData.exists()) {
                Logger.w("Tesseract traineddata missing: ${tessData.absolutePath}")
                throw IllegalStateException("Missing tessdata for ${language}")
            }
            tessBaseAPI.init(tessDir.absolutePath, language)
            initialised = true
        }
    }

    fun shutdown() {
        tessBaseAPI.recycle()
    }
}
