package com.example.avito_testing_2026_autum.settings.domain.repository

import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val themeMode: Flow<ThemeMode>
    val accentColor: Flow<AccentColor>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAccentColor(color: AccentColor)

}