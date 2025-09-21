package ai.screentalk.overlay.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ScreenTalk", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }
            }
            OutlinedTextField(
                value = input,
                onValueChange = onInputChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (isRecording) "Listening…" else "Ask about the screen") },
                singleLine = false,
                enabled = !isStreaming
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMic, enabled = !isRecording && !isStreaming) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Record")
                }
                TextButton(onClick = onToggleTts) {
                    Icon(imageVector = Icons.Outlined.VolumeUp, contentDescription = null)
                    Text(text = if (ttsEnabled) "Voice on" else "Voice off", modifier = Modifier.padding(start = 4.dp))
                }
                Button(
                    onClick = onSend,
                    enabled = input.isNotBlank() && !isStreaming,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null)
                    Text("Send", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val (label, weight, color) = when (message) {
        is ChatMessage.User -> Triple("You", FontWeight.SemiBold, MaterialTheme.colorScheme.primaryContainer)
        is ChatMessage.Assistant -> Triple("ScreenTalk", FontWeight.Normal, MaterialTheme.colorScheme.secondaryContainer)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = weight)
        Card(colors = CardDefaults.cardColors(containerColor = color)) {
            Text(
                text = when (message) {
                    is ChatMessage.User -> message.text
                    is ChatMessage.Assistant -> message.text.ifBlank { "…" }
                },
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
