package com.example.avito_testing_2026_autum.ai.data.network.api

import com.example.avito_testing_2026_autum.ai.data.network.dto.BalanceResponseDto
import com.example.avito_testing_2026_autum.ai.data.network.dto.ChatRequestDto
import com.example.avito_testing_2026_autum.ai.data.network.dto.ChatResponseDto
import com.example.avito_testing_2026_autum.ai.data.network.dto.ModelsResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GigaChatApi {
    @GET("v1/balance")
    suspend fun getBalance(): BalanceResponseDto

    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Body request: ChatRequestDto
    ): ChatResponseDto

    // Эндпоинт для получения доступных моделей
    @GET("v1/models")
    suspend fun getModels(): ModelsResponseDto
}