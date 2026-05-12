package com.marketfiyat.feature.settings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marketfiyat.ThemeMode
import com.marketfiyat.core.ui.components.*
import com.marketfiyat.feature.settings.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MarketFiyatTopBar(title = "Ayarlar", onNavigateBack = onNavigateBack)
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            )
        ) {
            // ─── Görünüm ───────────────────────────────────────────────
            item {
                SettingsSectionHeader("Görünüm")
            }
            item {
                SettingsClickItem(
                    icon = Icons.Default.DarkMode,
                    title = "Tema",
                    subtitle = when (uiState.themeMode) {
                        ThemeMode.LIGHT -> "Açık"
                        ThemeMode.DARK -> "Koyu"
                        ThemeMode.SYSTEM -> "Sistem"
                    },
                    onClick = { showThemeDialog = true }
                )
            }
            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Palette,
                    title = "Dinamik Renk",
                    subtitle = "Material You dinamik renk teması",
                    checked = uiState.dynamicColorEnabled,
                    onCheckedChange = viewModel::setDynamicColor
                )
            }

            // ─── Bildirimler ───────────────────────────────────────────
            item { SettingsSectionHeader("Bildirimler") }
            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Bildirimler",
                    subtitle = "Fiyat değişim bildirimlerini al",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::setNotifications
                )
            }
            item {
                SettingsSwitchItem(
                    icon = Icons.Default.NotificationImportant,
                    title = "Fiyat Alarmı",
                    subtitle = "Belirlenen fiyata düşünce bildir",
                    checked = uiState.priceAlarmEnabled,
                    onCheckedChange = viewModel::setPriceAlarm,
                    enabled = uiState.notificationsEnabled
                )
            }

            // ─── Yedekleme ─────────────────────────────────────────────
            item { SettingsSectionHeader("Yedekleme") }
            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Backup,
                    title = "Otomatik Yedekleme",
                    subtitle = "Günlük otomatik yedekleme yap",
                    checked = uiState.autoBackupEnabled,
                    onCheckedChange = viewModel::setAutoBackup
                )
            }
            item {
                SettingsClickItem(
                    icon = Icons.Default.FileDownload,
                    title = "JSON Dışa Aktar",
                    subtitle = "Tüm verileri JSON olarak kaydet",
                    onClick = {
                        viewModel.exportJson { content ->
                            // In real app: share or save to file
                        }
                    }
                )
            }
            item {
                SettingsClickItem(
                    icon = Icons.Default.FileUpload,
                    title = "JSON İçe Aktar",
                    subtitle = "JSON dosyasından verileri yükle",
                    onClick = {
                        // In real app: pick file and import
                        // viewModel.importJson(content)
                    }
                )
            }
            item {
                SettingsClickItem(
                    icon = Icons.Default.TableChart,
                    title = "CSV Dışa Aktar",
                    subtitle = "Fiyat geçmişini CSV olarak kaydet",
                    onClick = {
                        viewModel.exportCsv { content ->
                            // share or save
                        }
                    }
                )
            }
            if (uiState.lastBackupTime > 0) {
                item {
                    val dateStr = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("tr"))
                        .format(Date(uiState.lastBackupTime))
                    Text(
                        "Son yedekleme: $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 72.dp, vertical = 4.dp)
                    )
                }
            }

            // ─── Hakkında ──────────────────────────────────────────────
            item { SettingsSectionHeader("Hakkında") }
            item {
                SettingsClickItem(
                    icon = Icons.Default.Info,
                    title = "Uygulama Sürümü",
                    subtitle = "1.0.0",
                    onClick = {}
                )
            }
        }
    }

    // Theme dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Tema Seç") },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = uiState.themeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Text(
                                when (mode) {
                                    ThemeMode.LIGHT -> "Açık"
                                    ThemeMode.DARK -> "Koyu"
                                    ThemeMode.SYSTEM -> "Sistem Teması"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("İptal") }
            }
        )
    }

    // Loading overlay
    LoadingOverlay(isVisible = uiState.isLoading)
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    ListItem(
        headlineContent = {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        supportingContent = {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    )
}

@Composable
private fun SettingsClickItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        ListItem(
            headlineContent = {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            },
            supportingContent = {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            leadingContent = {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingContent = {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }
        )
    }
}
