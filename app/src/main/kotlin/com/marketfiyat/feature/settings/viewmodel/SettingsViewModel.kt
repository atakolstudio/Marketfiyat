package com.marketfiyat.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketfiyat.ThemeMode
import com.marketfiyat.core.data.local.datastore.UserPreferencesDataStore
import com.marketfiyat.core.domain.repository.BackupRepository
import com.marketfiyat.core.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val priceAlarmEnabled: Boolean = false,
    val autoBackupEnabled: Boolean = false,
    val lastBackupTime: Long = 0L,
    val showUnitPrice: Boolean = true,
    val defaultUnit: String = "GRAM",
    val exportSuccess: Boolean = false,
    val importSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesDataStore.userPreferences
                .catch { Timber.e(it) }
                .collectLatest { prefs ->
                    _uiState.update {
                        it.copy(
                            themeMode = prefs.themeMode,
                            dynamicColorEnabled = prefs.dynamicColorEnabled,
                            notificationsEnabled = prefs.notificationsEnabled,
                            priceAlarmEnabled = prefs.priceAlarmEnabled,
                            autoBackupEnabled = prefs.autoBackupEnabled,
                            lastBackupTime = prefs.lastBackupTime,
                            showUnitPrice = prefs.showUnitPrice,
                            defaultUnit = prefs.defaultUnit
                        )
                    }
                }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesDataStore.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setDynamicColor(enabled)
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setNotificationsEnabled(enabled)
        }
    }

    fun setPriceAlarm(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setPriceAlarmEnabled(enabled)
        }
    }

    fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setAutoBackupEnabled(enabled)
        }
    }

    fun exportJson(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = backupRepository.exportToJson()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, exportSuccess = true) }
                    onComplete(result.data)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Dışa aktarma başarısız: ${result.message}")
                    }
                }
                else -> Unit
            }
        }
    }

    fun exportCsv(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = backupRepository.exportToCsv()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onComplete(result.data)
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "CSV aktarma başarısız: ${result.message}")
                    }
                }
                else -> Unit
            }
        }
    }

    fun importJson(json: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = backupRepository.importFromJson(json)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, importSuccess = true) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = "İçe aktarma başarısız: ${result.message}")
                    }
                }
                else -> Unit
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null, exportSuccess = false, importSuccess = false) }
    }
}
