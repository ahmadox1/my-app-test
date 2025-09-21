package ai.screentalk.common

import android.content.Context
import java.io.File

object Files {
    fun modelsDir(context: Context): File = File(context.filesDir, "models").apply { mkdirs() }
    fun sttDir(context: Context): File = File(context.filesDir, "stt").apply { mkdirs() }
    fun tessDir(context: Context): File = File(context.filesDir, "tessdata").apply { mkdirs() }
}
