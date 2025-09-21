package ai.screentalk.common

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

object FileUtils {
    fun ensureDir(dir: File): File {
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getModelsDir(context: Context): File =
        ensureDir(File(context.filesDir, "models"))

    fun getTempDir(context: Context): File =
        ensureDir(File(context.cacheDir, "downloads"))

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }

    fun writeAtomic(target: File, bytes: ByteArray) {
        val temp = File(target.parentFile, target.name + ".tmp")
        FileOutputStream(temp).use { out ->
            out.write(bytes)
        }
        if (!temp.renameTo(target)) {
            throw IllegalStateException("Unable to replace ${'$'}target")
        }
    }
}
