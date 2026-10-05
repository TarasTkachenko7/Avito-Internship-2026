package com.example.avito_testing_2026_autum.ai.data.network.api

import com.example.avito_testing_2026_autum.ai.data.network.dto.TokenResponseDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface GigaChatAuthApi {

    @FormUrlEncoded
    @POST("api/v2/oauth")
    suspend fun getAccessToken(
        @Header("Authorization") authHeader: String,
        @Header("RqUID") rqUid: String,
        @Field("scope") scope: String = "GIGACHAT_API_PERS"
    ): TokenResponseDto

}