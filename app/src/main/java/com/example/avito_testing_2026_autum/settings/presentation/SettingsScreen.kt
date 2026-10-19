package com.example.avito_testing_2026_autum.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avito_testing_2026_autum.R
import com.example.avito_testing_2026_autum.settings.presentation.components.AccentColorSelectionSection
import com.example.avito_testing_2026_autum.settings.presentation.components.GigaChatBalanceCard
import com.example.avito_testing_2026_autum.settings.presentation.components.ResetSettingsButton
import com.example.avito_testing_2026_autum.settings.presentation.components.ThemeSelectionSection
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsEvent
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsUiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val settingsErrorText = state.settingsError?.asString()

    LaunchedEffect(settingsErrorText) {
        settingsErrorText?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.handleEvent(SettingsEvent.OnSettingsErrorDismissed)
        }
    }

    SettingsScreenContent(
        state = state,
        onEvent = viewModel::handleEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
private fun SettingsScreenContent(
    state: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            Text(
                text = stringResource(R.string.navigation_item_settings),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GigaChatBalanceCard(
                    balanceState = state.balanceState,
                    onRefresh = { onEvent(SettingsEvent.OnRefreshBalanceClicked) }
                )

                ThemeSelectionSection(
                    selectedTheme = state.themeMode,
                    onThemeSelected = { onEvent(SettingsEvent.OnThemeChanged(it)) }
                )

                AccentColorSelectionSection(
                    selectedColor = state.accentColor,
                    onColorSelected = { onEvent(SettingsEvent.OnAccentColorChanged(it)) }
                )

                ResetSettingsButton(
                    onResetClicked = { onEvent(SettingsEvent.OnResetClicked) }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
