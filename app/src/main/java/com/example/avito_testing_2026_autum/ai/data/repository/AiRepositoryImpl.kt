package com.example.avito_testing_2026_autum.ai.data.repository

import com.example.avito_testing_2026_autum.ai.data.network.api.GigaChatApi
import com.example.avito_testing_2026_autum.ai.domain.repository.AiRepository
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class AiRepositoryImpl(
    private val api: GigaChatApi,
    private val dispatchers: DispatchersProvider
) : AiRepository {

    override suspend fun getBalance(): Result<Long?> = withContext(dispatchers.io) {
        runCatching {
            try {
                val response = api.getBalance()
                val gigaChatTokens = response.balance.firstOrNull { it.usage == "GigaChat" }?.value
                    ?: response.balance.firstOrNull()?.value
                gigaChatTokens
            } catch (e: HttpException) {
                if (e.code() == 403) {
                    null
                } else {
                    throw e
                }
            }
        }
    }
}