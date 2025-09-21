package ai.screentalk.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScreenTalkApp(
    hasOverlayPermission: Boolean,
    isCapturing: Boolean,
    onRequestOverlay: () -> Unit,
    onStartBubble: () -> Unit,
    onStartCapture: () -> Unit,
    onStopCapture: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ScreenTalk",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (!hasOverlayPermission) {
            Text("Overlay permission required to show the chat head.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRequestOverlay) {
                Text("Grant overlay permission")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStartBubble, enabled = hasOverlayPermission) {
            Text("Start chat bubble")
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isCapturing) "Screen capture running" else "Screen capture stopped",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { if (isCapturing) onStopCapture() else onStartCapture() }
        ) {
            Text(if (isCapturing) "Stop capture" else "Start capture")
        }
    }
}
