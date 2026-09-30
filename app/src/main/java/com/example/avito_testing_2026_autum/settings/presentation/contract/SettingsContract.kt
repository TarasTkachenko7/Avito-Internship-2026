package com.example.avito_testing_2026_autum.settings.presentation.contract

import com.example.avito_testing_2026_autum.core.presentation.UiEffect
import com.example.avito_testing_2026_autum.core.presentation.UiEvent
import com.example.avito_testing_2026_autum.core.presentation.UiState
import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode

sealed interface GigaChatBalanceState {
    data object Loading : GigaChatBalanceState
    data class Success(val balance: String) : GigaChatBalanceState
    data class Error(val message: String) : GigaChatBalanceState
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.DEFAULT,
    val balanceState: GigaChatBalanceState = GigaChatBalanceState.Loading
) : UiState

sealed interface SettingsEvent : UiEvent {
    data class OnThemeChanged(val themeMode: ThemeMode) : SettingsEvent
    data class OnAccentColorChanged(val accentColor: AccentColor) : SettingsEvent
    data object OnResetClicked : SettingsEvent
    data object OnRetryBalanceClicked : SettingsEvent
}

sealed interface SettingsEffect : UiEffect