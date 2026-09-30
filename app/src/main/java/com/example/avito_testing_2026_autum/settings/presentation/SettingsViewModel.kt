package com.example.avito_testing_2026_autum.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode
import com.example.avito_testing_2026_autum.settings.domain.repository.SettingsRepository
import com.example.avito_testing_2026_autum.settings.presentation.contract.GigaChatBalanceState
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsEffect
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsEvent
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
): ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _effect = Channel<SettingsEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeSettings()
        loadBalance()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            repository.themeMode.collectLatest { theme ->
                _state.update { it.copy(themeMode = theme) }
            }
        }
        viewModelScope.launch {
            repository.accentColor.collectLatest { color ->
                _state.update { it.copy(accentColor = color) }
            }
        }
    }

    private fun loadBalance() {
        viewModelScope.launch {
            _state.update { it.copy(balanceState = GigaChatBalanceState.Loading) }
            delay(1000)
            _state.update { it.copy(balanceState = GigaChatBalanceState.Success("150 000")) }
        }
    }

    fun handleEvent(event: SettingsEvent) {
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