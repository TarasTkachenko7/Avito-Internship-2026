package com.example.avito_testing_2026_autum.settings.domain.usecases

import com.example.avito_testing_2026_autum.settings.domain.model.ThemeMode
import com.example.avito_testing_2026_autum.settings.domain.repository.SettingsRepository

class SetThemeModeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }
}