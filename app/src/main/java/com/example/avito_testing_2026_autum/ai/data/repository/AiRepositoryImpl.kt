package com.example.avito_testing_2026_autum.ai.data.repository

import com.example.avito_testing_2026_autum.ai.data.network.api.GigaChatApi
import com.example.avito_testing_2026_autum.ai.data.network.dto.ChatRequestDto
import com.example.avito_testing_2026_autum.ai.data.network.dto.MessageDto
import com.example.avito_testing_2026_autum.ai.domain.repository.AiRepository
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

class AiRepositoryImpl(
    private val api: GigaChatApi,
    private val dispatchers: DispatchersProvider
) : AiRepository {

    override suspend fun getBalance(): Result<Long?> = withContext(dispatchers.io) {
        try {
            val response = api.getBalance()
            val tokens = response.balance.firstOrNull { it.usage == "GigaChat" }?.value
                ?: response.balance.firstOrNull()?.value
                ?: throw IllegalStateException("Empty balance response")
            Result.success(tokens)
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() == 403) Result.success(null) else Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun formulateTask(rawText: String): Result<String> = withContext(dispatchers.io) {
        try {
            val systemMessage = MessageDto(
                role = "system",
                content = "Ты помощник по продуктивности. Пользователь надиктовал текст. Твоя задача — извлечь из него суть задачи и сформулировать её кратко, понятно и в повелительном наклонении. Верни ТОЛЬКО текст задачи без лишних слов, без кавычек и без приветствий."
            )
            val userMessage = MessageDto(
                role = "user",
                content = rawText
            )

            val request = ChatRequestDto(
                model = "GigaChat-3-Lightning",
                messages = listOf(systemMessage, userMessage),
                temperature = 0.7f
            )

            val response = api.getChatCompletion(request)

            val formulatedText = response.choices.firstOrNull()?.message?.content?.trim()
                ?.takeIf(String::isNotBlank)
                ?: throw IllegalStateException("Empty response from GigaChat")
            Result.success(formulatedText)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
