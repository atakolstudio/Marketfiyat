package com.marketfiyat.core.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.marketfiyat.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "market_fiyat_preferences"
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_PRICE_ALARM_ENABLED = booleanPreferencesKey("price_alarm_enabled")
        val KEY_DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val KEY_LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
        val KEY_AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        val KEY_DEFAULT_UNIT = stringPreferencesKey("default_unit")
        val KEY_SHOW_UNIT_PRICE = booleanPreferencesKey("show_unit_price")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.e(exception, "Error reading preferences")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                themeMode = ThemeMode.valueOf(
                    preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
                ),
                dynamicColorEnabled = preferences[KEY_DYNAMIC_COLOR] ?: true,
                notificationsEnabled = preferences[KEY_NOTIFICATIONS_ENABLED] ?: true,
                priceAlarmEnabled = preferences[KEY_PRICE_ALARM_ENABLED] ?: false,
                defaultCurrency = preferences[KEY_DEFAULT_CURRENCY] ?: "TL",
                isOnboardingComplete = preferences[KEY_ONBOARDING_COMPLETE] ?: false,
                lastBackupTime = preferences[KEY_LAST_BACKUP_TIME] ?: 0L,
                autoBackupEnabled = preferences[KEY_AUTO_BACKUP_ENABLED] ?: false,
                defaultUnit = preferences[KEY_DEFAULT_UNIT] ?: "GRAM",
                showUnitPrice = preferences[KEY_SHOW_UNIT_PRICE] ?: true
            )
        }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = themeMode.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setPriceAlarmEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_PRICE_ALARM_ENABLED] = enabled
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETE] = complete
        }
    }

    suspend fun setLastBackupTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_BACKUP_TIME] = time
        }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun setDefaultUnit(unit: String) {
        dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_UNIT] = unit
        }
    }

    suspend fun setShowUnitPrice(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SHOW_UNIT_PRICE] = show
        }
    }
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val priceAlarmEnabled: Boolean = false,
    val defaultCurrency: String = "TL",
    val isOnboardingComplete: Boolean = false,
    val lastBackupTime: Long = 0L,
    val autoBackupEnabled: Boolean = false,
    val defaultUnit: String = "GRAM",
    val showUnitPrice: Boolean = true
)
