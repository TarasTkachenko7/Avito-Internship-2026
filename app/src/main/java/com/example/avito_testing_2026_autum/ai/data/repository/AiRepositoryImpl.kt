package com.example.avito_testing_2026_autum.ai.data.repository

import com.example.avito_testing_2026_autum.ai.data.network.api.GigaChatApi
import android.util.Log
import com.example.avito_testing_2026_autum.ai.data.network.dto.ChatRequestDto
import com.example.avito_testing_2026_autum.ai.data.network.dto.MessageDto
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

    override suspend fun formulateTask(rawText: String): Result<String> = withContext(dispatchers.io) {
        runCatching {
            val systemMessage = MessageDto(
                role = "system",
                content = "Ты помощник по продуктивности. Пользователь надиктовал текст. Твоя задача — извлечь из него суть задачи и сформулировать её кратко, понятно и в повелительном наклонении. Верни ТОЛЬКО текст задачи без лишних слов, без кавычек и без приветствий."
            )
            val userMessage = MessageDto(
                role = "user",
                content = rawText
            )

            val request = ChatRequestDto(
                model = "GigaChat-3-Lightning", // Самая быстрая и дешёвая модель из доступных
                messages = listOf(systemMessage, userMessage),
                temperature = 0.3f
            )

            val response = api.getChatCompletion(request)

            response.choices.firstOrNull()?.message?.content
                ?: throw IllegalStateException("Empty response from GigaChat")
        }
    }
}