package com.example.avito_testing_2026_autum.ai.domain.repository

interface AuthRepository {
    suspend fun getAccessToken(): Result<String>

    /** Refreshes a rejected token, reusing a newer token if another request already refreshed it. */
    suspend fun refreshAccessToken(rejectedToken: String): Result<String>
}
