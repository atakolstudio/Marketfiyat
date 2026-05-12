package com.marketfiyat.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketfiyat.ThemeMode
import com.marketfiyat.core.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesDataStore.userPreferences
                .onEach { prefs ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            themeMode = prefs.themeMode,
                            dynamicColorEnabled = prefs.dynamicColorEnabled
                        )
                    }
                }
                .catch { _uiState.update { it.copy(isLoading = false) } }
                .collect()
        }
    }
}
