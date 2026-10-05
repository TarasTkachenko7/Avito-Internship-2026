package com.example.avito_testing_2026_autum.ai.domain.repository

interface AiRepository {
    suspend fun getBalance(): Result<Long?>
}