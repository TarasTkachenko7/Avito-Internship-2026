package com.example.avito_testing_2026_autum.ai.data.repository

import com.example.avito_testing_2026_autum.ai.data.network.api.GigaChatAuthApi
import com.example.avito_testing_2026_autum.ai.domain.repository.AuthRepository
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import kotlinx.coroutines.withContext
import java.util.UUID

private const val SAFETY_MARGIN_MS = 60_000L

class AuthRepositoryImpl(
    private val authApi: GigaChatAuthApi,
    private val authKey: String,
    private val dispatchers: DispatchersProvider
): AuthRepository {

    private var cachedToken: String? = null
    private var tokenExpiresAt: Long = 0L

    override suspend fun getAccessToken(): Result<String>  = withContext(dispatchers.io){
        runCatching {
            val currentTime = System.currentTimeMillis()

            val currentToken = cachedToken
            if (currentToken != null && currentTime < (tokenExpiresAt - SAFETY_MARGIN_MS)) {
                return@runCatching currentToken
            }

            val rqUid = UUID.randomUUID().toString()
            val authHeader = "Basic $authKey"

            val response = authApi.getAccessToken(
                authHeader = authHeader,
                rqUid = rqUid
            )

            cachedToken = response.accessToken
            tokenExpiresAt = response.expiresAt

            response.accessToken
        }
    }
}