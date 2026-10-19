package com.example.avito_testing_2026_autum.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode
import com.example.avito_testing_2026_autum.settings.domain.usecases.GetAccentColorUseCase
import com.example.avito_testing_2026_autum.settings.domain.usecases.GetThemeModeUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

class SettingsThemeViewModel(
    getThemeMode: GetThemeModeUseCase,
    getAccentColor: GetAccentColorUseCase
) : ViewModel() {
    val settings = combine(getThemeMode(), getAccentColor()) { theme, accent ->
        ThemeSettings(theme, accent)
    }.catch { }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeSettings())
}

data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.DEFAULT
)
