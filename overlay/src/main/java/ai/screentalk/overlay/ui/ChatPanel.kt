package ai.screentalk.overlay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class ChatMessage {
    abstract val text: String

    data class User(override val text: String) : ChatMessage()
    data class Assistant(override val text: String) : ChatMessage()
}

@Composable
fun ChatPanel(
    messages: List<ChatMessage>,
    input: String,
    isStreaming: Boolean,
    isRecording: Boolean,
    ttsEnabled: Boolean,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onMic: () -> Unit,
    onToggleTts: () -> Unit,
    onClose: () -> Unit
) {
    Surface(tonalElevation = 3.dp) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ScreenTalk",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onToggleTts) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (ttsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    reverseLayout = true
                ) {
                    val reversed = remember(messages) { messages.asReversed() }
                    items(reversed) { message ->
                        ChatBubble(message)
                    }
                }

                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = if (isRecording) "Recording…" else "اكتب سؤالك") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onMic) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isRecording) "إيقاف" else "تحدث")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onSend, enabled = input.isNotBlank() && !isStreaming) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                    }
                }

                if (isStreaming) {
                    Text(
                        text = "جاري التحليل…",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val arrangement = if (message is ChatMessage.User) Arrangement.End else Arrangement.Start
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = arrangement) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(
                containerColor = when (message) {
                    is ChatMessage.User -> MaterialTheme.colorScheme.primaryContainer
                    is ChatMessage.Assistant -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                fontSize = 16.sp,
                textAlign = if (message is ChatMessage.User) TextAlign.End else TextAlign.Start
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Preview(showBackground = true)
@Composable
private fun ChatPanelPreview() {
    ChatPanel(
        messages = listOf(
            ChatMessage.Assistant("مرحبًا!"),
            ChatMessage.User("ما هو الزر في الأعلى؟")
        ),
        input = "",
        isStreaming = false,
        isRecording = false,
        ttsEnabled = true,
        onInputChanged = {},
        onSend = {},
        onMic = {},
        onToggleTts = {},
        onClose = {}
    )
}
