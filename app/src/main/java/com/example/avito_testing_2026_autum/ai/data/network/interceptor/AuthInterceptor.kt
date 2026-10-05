package com.example.avito_testing_2026_autum.ai.data.network.interceptor


import com.example.avito_testing_2026_autum.ai.domain.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authRepository: AuthRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val tokenResult = runBlocking {
            authRepository.getAccessToken()
        }

        val token = tokenResult.getOrNull()

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("User-Agent", "AvitoNotesTasksApp")

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}