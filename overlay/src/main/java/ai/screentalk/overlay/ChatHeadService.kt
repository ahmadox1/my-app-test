package ai.screentalk.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import ai.screentalk.common.AppResult
import ai.screentalk.common.Logger
import ai.screentalk.ml.LocalModelEngine
import ai.screentalk.ml.llm.EchoEngine
import ai.screentalk.ml.llm.LlamaCppEngine
import ai.screentalk.ml.prompt.PromptBuilder
import ai.screentalk.ml.stt.VoskStt
import ai.screentalk.ml.tts.Tts
import ai.screentalk.overlay.ui.ChatMessage
import ai.screentalk.overlay.ui.ChatPanel
import ai.screentalk.screen.ScreenContextBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatHeadService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var panelView: ComposeView? = null

    private val messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val input = MutableStateFlow("")
    private val ttsEnabled = MutableStateFlow(true)
    private val isStreaming = MutableStateFlow(false)
    private val isRecording = MutableStateFlow(false)

    private lateinit var llamaEngine: LlamaCppEngine
    private lateinit var echoEngine: EchoEngine
    private lateinit var stt: VoskStt
    private lateinit var tts: Tts

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        llamaEngine = LlamaCppEngine(this)
        echoEngine = EchoEngine()
        stt = VoskStt(this)
        tts = Tts(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Bubble ready"))
        createBubble()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeBubble()
        removePanel()
        serviceScope.launch {
            tts.shutdown()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createBubble() {
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }
        val bubble = LayoutInflater.from(this).inflate(R.layout.overlay_bubble, null) as ImageView
        var lastX = 0
        var lastY = 0
        var initialX = 0
        var initialY = 0
        var isDragging = false

        bubble.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    lastX = event.rawX.toInt()
                    lastY = event.rawY.toInt()
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX.toInt() - lastX
                    val deltaY = event.rawY.toInt() - lastY
                    if (kotlin.math.abs(deltaX) > 10 || kotlin.math.abs(deltaY) > 10) {
                        isDragging = true
                        layoutParams.x = initialX + deltaX
                        layoutParams.y = initialY + deltaY
                        windowManager.updateViewLayout(bubble, layoutParams)
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        togglePanel()
                    }
                    true
                }

                else -> false
            }
        }

        bubbleView = bubble
        windowManager.addView(bubble, layoutParams)
    }

    private fun removeBubble() {
        bubbleView?.let { windowManager.removeView(it) }
        bubbleView = null
    }

    private fun togglePanel() {
        if (panelView == null) {
            showPanel()
        } else {
            removePanel()
        }
    }

    private fun showPanel() {
        val params = WindowManager.LayoutParams(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType(),
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 100
        }

        val composeView = ComposeView(this).apply {
            setContent {
                MaterialTheme {
                    val messagesState by messages.collectAsState()
                    val inputState by input.collectAsState()
                    val streaming by isStreaming.collectAsState()
                    val recording by isRecording.collectAsState()
                    val ttsState by ttsEnabled.collectAsState()
                    ChatPanel(
                        messages = messagesState,
                        input = inputState,
                        isStreaming = streaming,
                        isRecording = recording,
                        ttsEnabled = ttsState,
                        onInputChanged = { input.value = it },
                        onSend = { handleSend(input.value) },
                        onMic = { handleMic() },
                        onToggleTts = { ttsEnabled.update { enabled -> !enabled } },
                        onClose = { removePanel() }
                    )
                }
            }
        }

        panelView = composeView
        windowManager.addView(composeView, params)
    }

    private fun removePanel() {
        panelView?.let { windowManager.removeView(it) }
        panelView = null
    }

    private fun handleSend(text: String) {
        val question = text.trim()
        if (question.isEmpty()) return
        input.value = ""
        messages.update { it + ChatMessage.User(question) + ChatMessage.Assistant("") }
        val context = ScreenContextBuilder.lastContext()
        val prompt = PromptBuilder.build(question, context)
        val engine = activeEngine()
        serviceScope.launch {
            isStreaming.value = true
            val params = LocalModelEngine.GenParams(temperature = 0.7f, maxTokens = 256, topP = 0.95f)
            when (val result = engine.generateStream(prompt, params) { token ->
                updateAssistant(token)
            }) {
                is ai.screentalk.common.AppResult.Success -> {
                    isStreaming.value = false
                    if (ttsEnabled.value) {
                        tts.speak(result.value)
                    }
                }

                is ai.screentalk.common.AppResult.Error -> {
                    isStreaming.value = false
                    Logger.e("Generation failed", result.throwable)
                    updateAssistant("Failed: ${result.throwable.message}")
                }
            }
        }
    }

    private fun handleMic() {
        serviceScope.launch {
            isRecording.value = true
            when (val result = stt.listen("ar")) {
                is AppResult.Success -> {
                    isRecording.value = false
                    val transcript = result.value
                    if (transcript.isNotBlank()) {
                        input.value = transcript
                        handleSend(transcript)
                    }
                }

                is AppResult.Error -> {
                    isRecording.value = false
                    Logger.e("STT failed", result.throwable)
                }
            }
        }
    }

    private fun updateAssistant(delta: String) {
        messages.update { current ->
            if (current.isEmpty()) return@update current
            val updated = current.toMutableList()
            val lastIndex = updated.lastIndex
            val last = updated[lastIndex]
            if (last is ChatMessage.Assistant) {
                updated[lastIndex] = last.copy(text = last.text + delta)
            }
            updated
        }
    }

    private fun activeEngine(): LocalModelEngine {
        return if (llamaEngine.isReady()) llamaEngine else echoEngine
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Chat bubble",
            NotificationManager.IMPORTANCE_MIN
        )
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("ScreenTalk")
            .setContentText(text)
            .setOngoing(true)
            .build()

    private fun windowType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

    companion object {
        private const val NOTIFICATION_ID = 404
        private const val CHANNEL_ID = "overlay_chat"
    }
}
