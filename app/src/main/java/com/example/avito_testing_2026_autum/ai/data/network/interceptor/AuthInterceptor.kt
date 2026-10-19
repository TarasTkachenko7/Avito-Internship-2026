package com.example.avito_testing_2026_autum.ai.data.network.interceptor

import com.example.avito_testing_2026_autum.ai.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authRepository: AuthRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runBlocking { authRepository.getAccessToken() }.getOrThrow()
        require(token.isNotBlank()) { "OAuth returned an empty access token" }

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("User-Agent", "AvitoNotesTasksApp")

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())
        if (response.code != 401) return response

        val refreshResult = runBlocking { authRepository.refreshAccessToken(token) }
        val refreshedToken = refreshResult.getOrElse { error ->
            response.close()
            throw error
        }
        if (refreshedToken.isBlank() || refreshedToken == token) return response

        response.close()
        return chain.proceed(
            requestBuilder.header("Authorization", "Bearer $refreshedToken").build()
        )
    }
}
