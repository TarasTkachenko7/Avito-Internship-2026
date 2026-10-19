package com.example.avito_testing_2026_autum.ai.data.repository

import com.example.avito_testing_2026_autum.ai.data.network.api.GigaChatAuthApi
import com.example.avito_testing_2026_autum.ai.domain.repository.AuthRepository
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException
import java.util.UUID

private const val SAFETY_MARGIN_MS = 60_000L

class AuthRepositoryImpl(
    private val authApi: GigaChatAuthApi,
    private val authKey: String,
    private val dispatchers: DispatchersProvider
) : AuthRepository {

    private var cachedToken: String? = null
    private var tokenExpiresAt: Long = 0L
    private val refreshMutex = Mutex()

    override suspend fun getAccessToken(): Result<String> = getToken(rejectedToken = null)

    override suspend fun refreshAccessToken(rejectedToken: String): Result<String> =
        getToken(rejectedToken = rejectedToken)

    private suspend fun getToken(rejectedToken: String?): Result<String> = withContext(dispatchers.io) {
        try {
            refreshMutex.withLock {
                val now = System.currentTimeMillis()
                val currentToken = cachedToken
                val tokenIsValid = currentToken != null && now < tokenExpiresAt - SAFETY_MARGIN_MS
                if (tokenIsValid && (rejectedToken == null || currentToken != rejectedToken)) {
                    return@withLock Result.success(currentToken)
                }

                val response = authApi.getAccessToken(
                    authHeader = "Basic $authKey",
                    rqUid = UUID.randomUUID().toString()
                )
                if (response.accessToken.isBlank()) {
                    return@withLock Result.failure(IllegalStateException("OAuth returned an empty access token"))
                }
                cachedToken = response.accessToken
                tokenExpiresAt = response.expiresAt
                Result.success(response.accessToken)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
