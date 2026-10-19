package com.example.avito_testing_2026_autum.settings.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode
import com.example.avito_testing_2026_autum.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val dispatchers: DispatchersProvider
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
    }

    private val preferencesFlow = dataStore.data.flowOn(dispatchers.io)

    override val themeMode: Flow<ThemeMode> = preferencesFlow
        .map { preferences ->
            val name = preferences[PreferencesKeys.THEME_MODE]
            ThemeMode.entries.find { it.name == name } ?: ThemeMode.SYSTEM
        }

    override val accentColor: Flow<AccentColor> = preferencesFlow
        .map { preferences ->
            val name = preferences[PreferencesKeys.ACCENT_COLOR]
            AccentColor.entries.find { it.name == name } ?: AccentColor.DEFAULT
        }

    override suspend fun setAccentColor(color: AccentColor) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCENT_COLOR] = color.name
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    override suspend fun resetSettings() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.THEME_MODE)
            preferences.remove(PreferencesKeys.ACCENT_COLOR)
        }
    }

}
