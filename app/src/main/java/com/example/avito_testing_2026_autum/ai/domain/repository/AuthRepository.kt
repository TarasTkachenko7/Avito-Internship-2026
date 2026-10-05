package com.example.avito_testing_2026_autum.ai.domain.repository

interface AuthRepository {
    suspend fun getAccessToken(): Result<String>
}