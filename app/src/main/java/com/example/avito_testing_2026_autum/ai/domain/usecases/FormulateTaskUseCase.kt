package com.example.avito_testing_2026_autum.ai.domain.usecases

import com.example.avito_testing_2026_autum.ai.domain.repository.AiRepository

class FormulateTaskUseCase(
    private val repository: AiRepository
) {
    suspend operator fun invoke(rawVoiceText: String): Result<String> {
        if (rawVoiceText.isBlank()) return Result.failure(IllegalArgumentException("Text is empty"))
        return repository.formulateTask(rawVoiceText)
    }
}