package com.example.avito_testing_2026_autum.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.settings.presentation.components.AccentColorSelectionSection
import com.example.avito_testing_2026_autum.settings.presentation.components.GigaChatBalanceCard
import com.example.avito_testing_2026_autum.settings.presentation.components.ResetSettingsButton
import com.example.avito_testing_2026_autum.settings.presentation.components.ThemeSelectionSection
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsEvent
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsUiState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreenContent(
        state = state,
        onEvent = viewModel::handleEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    state: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.navigation_item_settings)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            GigaChatBalanceCard(
                balanceState = state.balanceState,
                onRetry = { onEvent(SettingsEvent.OnRetryBalanceClicked) }
            )

            HorizontalDivider()

            ThemeSelectionSection(
                selectedTheme = state.themeMode,
                onThemeSelected = { onEvent(SettingsEvent.OnThemeChanged(it)) }
            )

            HorizontalDivider()

            AccentColorSelectionSection(
                selectedColor = state.accentColor,
                onColorSelected = { onEvent(SettingsEvent.OnAccentColorChanged(it)) }
            )

            HorizontalDivider()

            ResetSettingsButton(
                onResetClicked = { onEvent(SettingsEvent.OnResetClicked) }
            )
        }
    }
}