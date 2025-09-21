package ai.screentalk.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ai.screentalk.app.settings.OcrEngine
import ai.screentalk.app.settings.SettingsViewModel

@Composable
fun ScreenTalkApp(
    capturing: Boolean,
    hasOverlay: Boolean,
    onRequestOverlay: () -> Unit,
    onStartBubble: () -> Unit,
    onStartCapture: () -> Unit,
    onStopCapture: () -> Unit
) {
    val navController = rememberNavController()
    val settingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel<SettingsViewModel>()

    NavHost(navController = navController, startDestination = Destinations.Home.route) {
        composable(Destinations.Home.route) {
            HomeScreen(
                capturing = capturing,
                hasOverlay = hasOverlay,
                onRequestOverlay = onRequestOverlay,
                onStartBubble = onStartBubble,
                onStartCapture = onStartCapture,
                onStopCapture = onStopCapture,
                onOpenSettings = { navController.navigate(Destinations.Settings.route) }
            )
        }
        composable(Destinations.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private enum class Destinations(val route: String) { Home("home"), Settings("settings") }

@Composable
private fun HomeScreen(
    capturing: Boolean,
    hasOverlay: Boolean,
    onRequestOverlay: () -> Unit,
    onStartBubble: () -> Unit,
    onStartCapture: () -> Unit,
    onStopCapture: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ScreenTalk",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(text = "المساعد المحلي للشاشة — كل المعالجة تتم على جهازك.")

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "الخطوات")
                    Button(onClick = { if (hasOverlay) onStartBubble() else onRequestOverlay() }) {
                        Text(text = if (hasOverlay) "ابدأ الفقاعة" else "امنح صلاحية الفقاعة")
                    }
                    Button(onClick = { if (capturing) onStopCapture() else onStartCapture() }) {
                        Text(text = if (capturing) "إيقاف التقاط الشاشة" else "بدء التقاط الشاشة")
                    }
                    TextButton(onClick = onOpenSettings) {
                        Text(text = "الإعدادات")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "الإعدادات", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onBack) { Text("رجوع") }
            }
            Divider()

            Text(text = "محرك التعرف الضوئي على الحروف")
            OcrOptionRow(
                label = "ML Kit",
                selected = state.ocrEngine == OcrEngine.MLKit,
                onSelected = { viewModel.setOcrEngine(OcrEngine.MLKit) }
            )
            OcrOptionRow(
                label = "Tesseract",
                selected = state.ocrEngine == OcrEngine.Tesseract,
                onSelected = { viewModel.setOcrEngine(OcrEngine.Tesseract) }
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = "استخدام خدمة الوصول", modifier = Modifier.weight(1f))
                Switch(checked = state.useAccessibility, onCheckedChange = viewModel::toggleAccessibility)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = "تفعيل TTS", modifier = Modifier.weight(1f))
                Switch(checked = state.ttsEnabled, onCheckedChange = viewModel::setTtsEnabled)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = "حفظ لقطات الشاشة", modifier = Modifier.weight(1f))
                Switch(checked = state.saveScreenshots, onCheckedChange = viewModel::setSaveScreens)
            }

            Text(text = "فاصل الالتقاط الحالي: ${state.captureIntervalMs} مللي ثانية")
        }
    }
}

@Composable
private fun OcrOptionRow(label: String, selected: Boolean, onSelected: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        RadioButton(selected = selected, onClick = onSelected)
        Text(text = label)
    }
}
