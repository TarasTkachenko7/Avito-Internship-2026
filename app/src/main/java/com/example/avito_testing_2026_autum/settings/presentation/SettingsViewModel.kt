package com.example.avito_testing_2026_autum.settings.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.ai.domain.usecases.GetGigaChatBalanceUseCase
import com.example.avito_testing_2026_autum.core.utils.UiText
import com.example.avito_testing_2026_autum.settings.domain.usecases.GetAccentColorUseCase
import com.example.avito_testing_2026_autum.settings.domain.usecases.GetThemeModeUseCase
import com.example.avito_testing_2026_autum.settings.domain.usecases.ResetSettingsUseCase
import com.example.avito_testing_2026_autum.settings.domain.usecases.SetAccentColorUseCase
import com.example.avito_testing_2026_autum.settings.domain.usecases.SetThemeModeUseCase
import com.example.avito_testing_2026_autum.settings.presentation.contract.GigaChatBalanceState
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsEffect
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsEvent
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class SettingsViewModel(
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val getAccentColorUseCase: GetAccentColorUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setAccentColorUseCase: SetAccentColorUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase,
    private val getBalanceUseCase: GetGigaChatBalanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    private val _effect = Channel<SettingsEffect>(capacity = Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var isBalanceLoading = false

    init {
        observeSettings()
        loadBalance()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                getThemeModeUseCase(),
                getAccentColorUseCase()
            ) { theme, color ->
                theme to color
            }.collect { (theme, color) ->
                _state.update { it.copy(themeMode = theme, accentColor = color) }
            }
        }
    }

    private fun loadBalance() {
        if (isBalanceLoading) return
        isBalanceLoading = true
        _state.update { it.copy(balanceState = GigaChatBalanceState.Loading) }

        viewModelScope.launch {
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
                    val errorMessage = if (error is IOException) {
                        UiText.StringResource(R.string.error_network_connection)
                    } else {
                        UiText.StringResource(R.string.error_loading_balance)
                    }
                    _state.update {
                        it.copy(balanceState = GigaChatBalanceState.Error(errorMessage))
                    }
                }
            isBalanceLoading = false
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
                viewModelScope.launch { resetSettingsUseCase() }
            }

            is SettingsEvent.OnRefreshBalanceClicked -> {
                loadBalance()
            }
        }
    }
}