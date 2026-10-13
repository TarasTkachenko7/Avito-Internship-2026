package com.example.avito_testing_2026_autum.settings.domain.usecases

import com.example.avito_testing_2026_autum.settings.domain.repository.SettingsRepository

class ResetSettingsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() {
        repository.resetSettings()
    }
}