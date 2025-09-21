package ai.screentalk.common.downloads

import android.content.Context
import ai.screentalk.common.AppResult
import ai.screentalk.common.Files
import ai.screentalk.common.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.io.DEFAULT_BUFFER_SIZE

private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class ModelDescriptor(
    val id: String,
    val name: String,
    val size: String,
    val url: String,
    val sha256: String
)

@Serializable
data class SttModelDescriptor(
    val id: String,
    val name: String,
    val language: String,
    val url: String,
    val sha256: String
)

@Serializable
data class TessDataDescriptor(
    val id: String,
    val language: String,
    val url: String,
    val sha256: String
)

object Downloads {
    suspend fun downloadLlmModel(
        context: Context,
        descriptor: ModelDescriptor,
        onProgress: (Int) -> Unit
    ): AppResult<File> = withContext(Dispatchers.IO) {
        val targetDir = File(Files.modelsDir(context), descriptor.id)
        targetDir.mkdirs()
        val targetFile = File(targetDir, File(descriptor.url).name)
        downloadFile(descriptor.url, targetFile, descriptor.sha256, onProgress)
    }

    suspend fun downloadSttModel(
        context: Context,
        descriptor: SttModelDescriptor,
        onProgress: (Int) -> Unit
    ): AppResult<File> = withContext(Dispatchers.IO) {
        val targetDir = File(Files.sttDir(context), descriptor.id)
        targetDir.mkdirs()
        val targetFile = File(targetDir, File(descriptor.url).name)
        downloadFile(descriptor.url, targetFile, descriptor.sha256, onProgress)
    }

    suspend fun downloadTessdata(
        context: Context,
        descriptor: TessDataDescriptor,
        onProgress: (Int) -> Unit
    ): AppResult<File> = withContext(Dispatchers.IO) {
        val targetDir = Files.tessDir(context)
        val targetFile = File(targetDir, "${descriptor.id}.traineddata")
        downloadFile(descriptor.url, targetFile, descriptor.sha256, onProgress)
    }

    fun loadLlmCatalog(context: Context): List<ModelDescriptor> =
        context.assets.open("models.json").use { stream ->
            val jsonString = stream.readBytes().decodeToString()
            json.decodeFromString(ListSerializer(ModelDescriptor.serializer()), jsonString)
        }

    fun loadSttCatalog(context: Context): List<SttModelDescriptor> =
        context.assets.open("stt_models.json").use { stream ->
            val jsonString = stream.readBytes().decodeToString()
            json.decodeFromString(ListSerializer(SttModelDescriptor.serializer()), jsonString)
        }

    fun loadTessCatalog(context: Context): List<TessDataDescriptor> =
        context.assets.open("tessdata.json").use { stream ->
            val jsonString = stream.readBytes().decodeToString()
            json.decodeFromString(ListSerializer(TessDataDescriptor.serializer()), jsonString)
        }

    private fun downloadFile(
        url: String,
        destination: File,
        expectedSha256: String,
        onProgress: (Int) -> Unit
    ): AppResult<File> {
        return try {
            destination.parentFile?.mkdirs()
            val tempFile = File(destination.parentFile, "${destination.name}.download")
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.connect()
                val totalBytes = connection.contentLengthLong.takeIf { it > 0 } ?: -1
                val digest = MessageDigest.getInstance("SHA-256")
                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var downloaded = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            digest.update(buffer, 0, read)
                            downloaded += read
                            if (totalBytes > 0) {
                                val percent = (downloaded * 100 / totalBytes).toInt().coerceIn(0, 100)
                                onProgress(percent)
                            }
                        }
                        output.flush()
                    }
                }
                val actualSha = digest.digest().toHexString()
                if (!actualSha.equals(expectedSha256, ignoreCase = true)) {
                    tempFile.delete()
                    Logger.e("Checksum mismatch for $url: expected=$expectedSha256 actual=$actualSha")
                    return AppResult.Error(IllegalStateException("Checksum mismatch"))
                }
            } finally {
                connection.disconnect()
            }
            if (destination.exists()) {
                destination.delete()
            }
            tempFile.renameTo(destination)
            onProgress(100)
            AppResult.Success(destination)
        } catch (t: Throwable) {
            Logger.e("Download failed for $url", t)
            AppResult.Error(t)
        }
    }

    private fun ByteArray.toHexString(): String = joinToString(separator = "") { byte ->
        "%02x".format(byte)
    }
}
