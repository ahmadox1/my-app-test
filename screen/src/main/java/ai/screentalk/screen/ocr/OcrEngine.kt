package ai.screentalk.screen.ocr

import android.content.Context
import android.graphics.Bitmap
import ai.screentalk.common.Files
import ai.screentalk.common.Logger
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface OcrEngine {
    suspend fun extractText(bitmap: Bitmap, languageHint: String? = null): String
}

class HybridOcrEngine(
    private val primary: OcrEngine,
    private val fallback: OcrEngine
) : OcrEngine {
    override suspend fun extractText(bitmap: Bitmap, languageHint: String?): String {
        val primaryResult = runCatching { primary.extractText(bitmap, languageHint) }
            .onFailure { Logger.e("Primary OCR failed", it) }
            .getOrDefault("")
            .trim()
        if (primaryResult.isNotBlank()) return primaryResult

        return runCatching { fallback.extractText(bitmap, languageHint) }
            .onFailure { Logger.e("Fallback OCR failed", it) }
            .getOrDefault("")
            .trim()
    }
}

class MLKitOcrEngine(context: Context) : OcrEngine {
    private val latinClient = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val chineseClient = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    private val japaneseClient = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
    private val koreanClient = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    override suspend fun extractText(bitmap: Bitmap, languageHint: String?): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        val client = when (languageHint?.lowercase()) {
            "zh", "zh-cn", "zh-tw" -> chineseClient
            "ja", "jp" -> japaneseClient
            "ko" -> koreanClient
            else -> latinClient
        }
        val result = client.process(image).await()
        return result.text
    }
}

class TessOcrEngine(private val context: Context) : OcrEngine {
    private val dataPath: String = context.filesDir.absolutePath

    override suspend fun extractText(bitmap: Bitmap, languageHint: String?): String = withContext(Dispatchers.IO) {
        val language = languageHint?.takeIf { it.isNotBlank() } ?: "eng"
        ensureDataDir()
        if (!hasTrainedData(language)) {
            Logger.d("Traineddata for $language missing; skipping Tess OCR")
            return@withContext ""
        }
        val tess = TessBaseAPI()
        try {
            if (!tess.init(dataPath, language)) {
                Logger.e("Failed to init Tesseract for language $language")
                return@withContext ""
            }
            tess.setImage(bitmap)
            tess.utF8Text.orEmpty()
        } catch (t: Throwable) {
            Logger.e("Tesseract OCR error", t)
            ""
        } finally {
            tess.recycle()
        }
    }

    private fun ensureDataDir() {
        Files.tessDir(context)
    }

    private fun hasTrainedData(language: String): Boolean {
        val trainedFile = File(Files.tessDir(context), "$language.traineddata")
        return trainedFile.exists()
    }
}

private suspend fun Task<Text>.await(): Text = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
    addOnCanceledListener { cont.cancel() }
}
