package com.example.avito_testing_2026_autum.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode
import com.example.avito_testing_2026_autum.settings.presentation.contract.GigaChatBalanceState
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsEvent
import com.example.avito_testing_2026_autum.settings.presentation.contract.SettingsUiState
import com.example.avito_testing_2026_autum.ui.theme.AccentBlue
import com.example.avito_testing_2026_autum.ui.theme.AccentGreen
import com.example.avito_testing_2026_autum.ui.theme.AccentOrange
import com.example.avito_testing_2026_autum.ui.theme.AccentPurple
import com.example.avito_testing_2026_autum.ui.theme.Purple40
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenRoot(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreenContent(
        state = state,
        onEvent = viewModel::handleEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    state: SettingsUiState,
    onEvent: (SettingsEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Настройки") })
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
            BalanceSection(
                balanceState = state.balanceState,
                onRetry = { onEvent(SettingsEvent.OnRetryBalanceClicked) }
            )

            HorizontalDivider()

            ThemeSection(
                selectedTheme = state.themeMode,
                onThemeSelected = { onEvent(SettingsEvent.OnThemeChanged(it)) }
            )

            HorizontalDivider()

            AccentColorSection(
                selectedColor = state.accentColor,
                onColorSelected = { onEvent(SettingsEvent.OnAccentColorChanged(it)) }
            )

            HorizontalDivider()

            ResetSection(
                onResetClicked = { onEvent(SettingsEvent.OnResetClicked) }
            )
        }
    }
}

@Composable
private fun BalanceSection(
    balanceState: GigaChatBalanceState,
    onRetry: () -> Unit
) {
    Column {
        Text(
            text = "Баланс GigaChat",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (balanceState) {
                    is GigaChatBalanceState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                    is GigaChatBalanceState.Success -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Доступно токенов", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = balanceState.balance,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    is GigaChatBalanceState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = balanceState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onRetry) {
                                Text("Повторить")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSection(
    selectedTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Column {
        Text(
            text = "Тема оформления",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ThemeMode.entries.forEach { themeMode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelected(themeMode) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedTheme == themeMode,
                    onClick = { onThemeSelected(themeMode) }
                )
                Text(
                    text = when (themeMode) {
                        ThemeMode.SYSTEM -> "Системная"
                        ThemeMode.LIGHT -> "Светлая"
                        ThemeMode.DARK -> "Темная"
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AccentColorSection(
    selectedColor: AccentColor,
    onColorSelected: (AccentColor) -> Unit
) {
    Column {
        Text(
            text = "Цветовая схема",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val colorOptions = listOf(
                AccentColor.DEFAULT to Purple40, // Дефолтный фиолетовый из Material 3
                AccentColor.BLUE to AccentBlue,
                AccentColor.GREEN to AccentGreen,
                AccentColor.ORANGE to AccentOrange,
                AccentColor.PURPLE to AccentPurple
            )

            colorOptions.forEach { (accentColor, colorValue) ->
                val isSelected = selectedColor == accentColor
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colorValue)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(accentColor) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Выбрано",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetSection(onResetClicked: () -> Unit) {
    OutlinedButton(
        onClick = onResetClicked,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
    ) {
        Text("Сбросить настройки")
    }
}