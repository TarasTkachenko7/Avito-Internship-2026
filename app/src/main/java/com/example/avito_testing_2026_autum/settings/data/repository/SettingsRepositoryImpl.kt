package com.example.avito_testing_2026_autum.settings.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode
import com.example.avito_testing_2026_autum.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val dispatchers: DispatchersProvider
): SettingsRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
    }

    override val themeMode: Flow<ThemeMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val name = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            ThemeMode.valueOf(name)
        }

    override val accentColor: Flow<AccentColor> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val name = preferences[PreferencesKeys.ACCENT_COLOR] ?: AccentColor.DEFAULT.name
            AccentColor.valueOf(name)
        }

    override suspend fun setAccentColor(color: AccentColor) {
        withContext(dispatchers.io) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.ACCENT_COLOR] = color.name
            }
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        withContext(dispatchers.io) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.THEME_MODE] = mode.name
            }
        }
    }

}