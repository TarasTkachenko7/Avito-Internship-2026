package com.example.avito_testing_2026_autum.settings.presentation.contract

import com.example.avito_testing_2026_autum.core.utils.UiText
import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode

sealed interface GigaChatBalanceState {
    data object Loading : GigaChatBalanceState
    data class Success(val balanceText: UiText) : GigaChatBalanceState
    data class Error(val message: UiText) : GigaChatBalanceState
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.DEFAULT,
    val balanceState: GigaChatBalanceState = GigaChatBalanceState.Loading,
    val settingsError: UiText? = null
)

sealed interface SettingsEvent {
    data class OnThemeChanged(val themeMode: ThemeMode) : SettingsEvent
    data class OnAccentColorChanged(val accentColor: AccentColor) : SettingsEvent
    data object OnResetClicked : SettingsEvent
    data object OnRefreshBalanceClicked : SettingsEvent
    data object OnSettingsErrorDismissed : SettingsEvent
}

sealed interface SettingsEffect
