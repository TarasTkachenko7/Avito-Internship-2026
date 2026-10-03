package com.example.avito_testing_2026_autum.settings.domain.usecases

import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode
import com.example.avito_testing_2026_autum.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetThemeModeUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<ThemeMode> = repository.themeMode
}