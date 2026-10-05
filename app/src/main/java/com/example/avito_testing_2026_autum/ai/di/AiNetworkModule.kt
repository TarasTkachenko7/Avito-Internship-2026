package com.example.avito_testing_2026_autum.ai.di

import com.example.avito_testing_2026_autum.BuildConfig
import com.example.avito_testing_2026_autum.ai.data.network.api.GigaChatAuthApi
import com.example.avito_testing_2026_autum.ai.data.repository.AuthRepositoryImpl
import com.example.avito_testing_2026_autum.ai.domain.AuthRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val AUTH_BASE_URL = "https://ngw.devices.sberbank.ru:9443/"

val aiNetworkModule = module {

    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single(named("authOkHttpClient")) {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single(named("authRetrofit")) {
        val json = get<Json>()
        val contentType = "application/json".toMediaType()

        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(get(named("authOkHttpClient")))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    single<GigaChatAuthApi> {
        get<Retrofit>(named("authRetrofit")).create(GigaChatAuthApi::class.java)
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            authApi = get(),
            authKey = BuildConfig.GIGACHAT_AUTH_KEY,
            dispatchers = get()
        )
    }

}