package com.example.avito_testing_2026_autum.ai.data.network.api

import com.example.avito_testing_2026_autum.ai.data.network.dto.BalanceResponseDto
import retrofit2.http.GET

interface GigaChatApi {

    @GET("v1/balance")
    suspend fun getBalance(): BalanceResponseDto

}