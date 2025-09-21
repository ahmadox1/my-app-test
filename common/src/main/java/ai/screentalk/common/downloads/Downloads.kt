package ai.screentalk.common.downloads

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import ai.screentalk.common.FileUtils
import ai.screentalk.common.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

private const val KEY_URL = "url"
private const val KEY_TARGET = "target"
private const val KEY_SHA = "sha"
private const val KEY_TITLE = "title"
private const val BUFFER_SIZE = 8 * 1024

data class DownloadSpec(
    val id: String,
    val url: String,
    val targetFile: File,
    val sha256: String,
    val title: String
)

object DownloadScheduler {
    fun enqueue(context: Context, spec: DownloadSpec) {
        val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
            .addTag(spec.id)
            .setInputData(
                workDataOf(
                    KEY_URL to spec.url,
                    KEY_TARGET to spec.targetFile.absolutePath,
                    KEY_SHA to spec.sha256,
                    KEY_TITLE to spec.title
                )
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            spec.id,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}

class ModelDownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            inputData.getString(KEY_TARGET).hashCode(),
            DownloadNotifications.create(applicationContext, inputData.getString(KEY_TITLE) ?: "Model download")
        )
    }

    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val targetPath = inputData.getString(KEY_TARGET) ?: return Result.failure()
        val sha = inputData.getString(KEY_SHA) ?: return Result.failure()

        val target = File(targetPath)
        FileUtils.ensureDir(target.parentFile)

        return try {
            withContext(Dispatchers.IO) {
                downloadToFile(url, target)
                val checksum = FileUtils.sha256(target)
                if (!checksum.equals(sha, ignoreCase = true)) {
                    Logger.e("Checksum mismatch for ${'$'}target: expected ${'$'}sha got ${'$'}checksum")
                    target.delete()
                    throw IllegalStateException("Checksum mismatch")
                }
            }
            Result.success()
        } catch (t: Throwable) {
            Logger.e("Failed downloading ${'$'}url", t)
            Result.retry()
        }
    }

    private suspend fun downloadToFile(url: String, target: File) {
        withContext(Dispatchers.IO) {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 20_000
                readTimeout = 20_000
            }
            connection.inputStream.use { input ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                    }
                }
            }
            connection.disconnect()
        }
    }
}
