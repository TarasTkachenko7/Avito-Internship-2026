package com.example.avito_testing_2026_autum.settings.presentation

import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.core.presentation.BaseViewModel
import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode
import com.example.avito_testing_2026_autum.settings.domain.repository.SettingsRepository
import com.example.avito_testing_2026_autum.settings.presentation.contract.GigaChatBalanceState
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsEffect
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsEvent
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : BaseViewModel<SettingsUiState, SettingsEvent, SettingsEffect>(
    initialValue = SettingsUiState()
) {

    init {
        observeSettings()
        loadBalance()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            repository.themeMode.collectLatest { theme ->
                setState { it.copy(themeMode = theme) }
            }
        }
        viewModelScope.launch {
            repository.accentColor.collectLatest { color ->
                setState { it.copy(accentColor = color) }
            }
        }
    }

    private fun loadBalance() {
        viewModelScope.launch {
            setState { it.copy(balanceState = GigaChatBalanceState.Loading) }
            delay(1000)
            setState { it.copy(balanceState = GigaChatBalanceState.Success("150 000")) }
        }
    }

    override fun handleEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnThemeChanged -> {
                viewModelScope.launch { repository.setThemeMode(event.themeMode) }
            }
            is SettingsEvent.OnAccentColorChanged -> {
                viewModelScope.launch { repository.setAccentColor(event.accentColor) }
            }
            is SettingsEvent.OnResetClicked -> {
                viewModelScope.launch {
                    repository.setThemeMode(ThemeMode.SYSTEM)
                    repository.setAccentColor(AccentColor.DEFAULT)
                }
            }
            is SettingsEvent.OnRetryBalanceClicked -> {
                loadBalance()
            }
        }
    }
}