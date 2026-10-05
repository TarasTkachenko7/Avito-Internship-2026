package com.example.avito_testing_2026_autum.ai.domain.usecases

import com.example.avito_testing_2026_autum.ai.domain.repository.AiRepository

class GetGigaChatBalanceUseCase(
    private val repository: AiRepository
) {
    suspend operator fun invoke(): Result<Long?> {
        return repository.getBalance()
    }
}