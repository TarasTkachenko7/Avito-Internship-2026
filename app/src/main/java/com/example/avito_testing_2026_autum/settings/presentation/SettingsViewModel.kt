package com.example.avito_testing_2026_autum.settings.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.ai.domain.usecases.GetGigaChatBalanceUseCase
import com.example.avito_testing_2026_autum.core.utils.UiText
import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode
import com.example.avito_testing_2026_autum.settings.domain.usecases.GetAccentColorUseCase
import com.example.avito_testing_2026_autum.settings.domain.usecases.GetThemeModeUseCase
import com.example.avito_testing_2026_autum.settings.domain.usecases.SetAccentColorUseCase
import com.example.avito_testing_2026_autum.settings.domain.usecases.SetThemeModeUseCase
import com.example.avito_testing_2026_autum.settings.presentation.contract.GigaChatBalanceState
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsEffect
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsEvent
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsUiState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.avito_testing_2026_autum.R

class SettingsViewModel(
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val getAccentColorUseCase: GetAccentColorUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setAccentColorUseCase: SetAccentColorUseCase,
    private val getBalanceUseCase: GetGigaChatBalanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _effect = Channel<SettingsEffect>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    val effect = _effect.receiveAsFlow()

    init {
        observeSettings()
        loadBalance()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getThemeModeUseCase()
                .catch { Log.e("SettingsViewModel", "Error observing theme mode", it) }
                .collectLatest { theme ->
                    _state.update { it.copy(themeMode = theme) }
                }
        }
        viewModelScope.launch {
            getAccentColorUseCase()
                .catch { Log.e("SettingsViewModel", "Error observing accent color", it) }
                .collectLatest { color ->
                    _state.update { it.copy(accentColor = color) }
                }
        }
    }

    private fun loadBalance() {
        viewModelScope.launch {
            _state.update { it.copy(balanceState = GigaChatBalanceState.Loading) }

            getBalanceUseCase()
                .onSuccess { tokens ->
                    val balanceText = if (tokens != null) {
                        UiText.DynamicString("%,d".format(tokens).replace(',', ' '))
                    } else {
                        UiText.StringResource(R.string.balance_pay_as_you_go)
                    }
                    _state.update {
                        it.copy(balanceState = GigaChatBalanceState.Success(balanceText))
                    }
                }
                .onFailure { error ->
                    Log.e("SettingsViewModel", "Failed to load GigaChat balance", error)

                    val errorMessage = if (error is java.io.IOException) {
                        UiText.StringResource(R.string.error_network_connection)
                    } else {
                        UiText.StringResource(R.string.error_loading_balance)
                    }

                    _state.update {
                        it.copy(balanceState = GigaChatBalanceState.Error(errorMessage))
                    }
                }
        }
    }

    fun handleEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnThemeChanged -> {
                viewModelScope.launch { setThemeModeUseCase(event.themeMode) }
            }

            is SettingsEvent.OnAccentColorChanged -> {
                viewModelScope.launch { setAccentColorUseCase(event.accentColor) }
            }

            is SettingsEvent.OnResetClicked -> {
                viewModelScope.launch {
                    setThemeModeUseCase(ThemeMode.SYSTEM)
                    setAccentColorUseCase(AccentColor.DEFAULT)
                }
            }

            is SettingsEvent.OnRetryBalanceClicked -> {
                loadBalance()
            }
        }
    }
}