package com.example.avito_testing_2026_autum.ai.domain

interface AuthRepository {
    suspend fun getAccessToken(): Result<String>
}