package com.example.avito_testing_2026_autum.settings.domain.usecases

import com.example.avito_testing_2026_autum.settings.domain.model.AccentColor
import com.example.avito_testing_2026_autum.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetAccentColorUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<AccentColor> = repository.accentColor
}