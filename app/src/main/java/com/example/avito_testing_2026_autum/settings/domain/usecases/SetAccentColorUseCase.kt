package com.example.avito_testing_2026_autum.settings.domain.usecases

import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.repository.SettingsRepository

class SetAccentColorUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(color: AccentColor) {
        repository.setAccentColor(color)
    }
}