package com.ahmadox.smartcoach.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahmadox.smartcoach.R
import com.ahmadox.smartcoach.data.model.StrategyAction
import com.ahmadox.smartcoach.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCoachApp() {
    val viewModel: MainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.main_title),
                        fontWeight = FontWeight.Bold
                    ) 
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ServiceStatusCard(
                    isRunning = uiState.isServiceRunning,
                    onStartService = viewModel::startService,
                    onStopService = viewModel::stopService
                )
            }
            
            item {
                PermissionsCard(
                    permissions = uiState.permissions,
                    onRequestPermissions = viewModel::requestPermissions
                )
            }
            
            if (uiState.currentStrategy != null) {
                item {
                    StrategyCard(
                        strategy = uiState.currentStrategy,
                        gameState = uiState.gameState
                    )
                }
            }
            
            item {
                ModelDownloadCard(
                    isDownloading = uiState.isDownloadingModel,
                    downloadProgress = uiState.downloadProgress,
                    onDownloadModels = viewModel::downloadModels
                )
            }
            
            if (uiState.showPrivacyNotice) {
                item {
                    PrivacyNoticeCard(
                        onAccept = viewModel::acceptPrivacyNotice,
                        onDecline = viewModel::declinePrivacyNotice
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceStatusCard(
    isRunning: Boolean,
    onStartService: () -> Unit,
    onStopService: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.service_status),
                    style = MaterialTheme.typography.titleMedium
                )
                
                Surface(
                    color = if (isRunning) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(
                            if (isRunning) R.string.service_running else R.string.service_stopped
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartService,
                    enabled = !isRunning
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.start_service))
                }
                
                OutlinedButton(
                    onClick = onStopService,
                    enabled = isRunning
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.stop_service))
                }
            }
        }
    }
}

@Composable
private fun PermissionsCard(
    permissions: Map<String, Boolean>,
    onRequestPermissions: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "الصلاحيات",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            permissions.forEach { (permission, granted) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = permission,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Icon(
                        imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (granted) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("طلب الصلاحيات")
            }
        }
    }
}

@Composable
private fun StrategyCard(
    strategy: com.ahmadox.smartcoach.data.model.StrategyRecommendation,
    gameState: com.ahmadox.smartcoach.data.model.GameState?
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "الاقتراح الاستراتيجي",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = when (strategy.action) {
                    StrategyAction.ATTACK -> MaterialTheme.colorScheme.errorContainer
                    StrategyAction.DEFENSE -> MaterialTheme.colorScheme.primaryContainer
                    StrategyAction.WAIT -> MaterialTheme.colorScheme.tertiaryContainer
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = when (strategy.action) {
                        StrategyAction.ATTACK -> stringResource(R.string.strategy_attack)
                        StrategyAction.DEFENSE -> stringResource(R.string.strategy_defense)
                        StrategyAction.WAIT -> stringResource(R.string.strategy_wait)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = strategy.reasoning,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (gameState != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "إكسيري: ${gameState.myElixir}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "إكسير الخصم: ${gameState.oppElixir}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelDownloadCard(
    isDownloading: Boolean,
    downloadProgress: Float,
    onDownloadModels: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "نماذج الذكاء الاصطناعي",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isDownloading) {
                LinearProgressIndicator(
                    progress = downloadProgress,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "جاري التحميل... ${(downloadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Button(
                    onClick = onDownloadModels,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.download_models))
                }
            }
        }
    }
}

@Composable
private fun PrivacyNoticeCard(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.privacy_notice_title),
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.privacy_notice_description),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.accept))
                }
                
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.decline))
                }
            }
        }
    }
}