package com.example.avito_testing_2026_autum.ai.data.repository

import com.example.avito_testing_2026_autum.ai.data.network.api.GigaChatApi
import com.example.avito_testing_2026_autum.ai.data.network.dto.BalanceResponseDto
import com.example.avito_testing_2026_autum.ai.data.network.dto.ChatResponseDto
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class AiRepositoryImplTest {
    private val api = mockk<GigaChatApi>()
    private val repository = AiRepositoryImpl(api, DispatchersProviderImpl)

    @Test
    fun emptySuccessfulBalanceIsAnErrorInsteadOfPayAsYouGo() = runTest {
        coEvery { api.getBalance() } returns BalanceResponseDto(emptyList())

        val result = repository.getBalance()

        assertTrue(result.isFailure)
    }

    @Test
    fun emptyAiChoicesAreRejected() = runTest {
        coEvery { api.getChatCompletion(any()) } returns ChatResponseDto(emptyList())

        val result = repository.formulateTask("spoken text")

        assertTrue(result.isFailure)
    }

    @Test
    fun blankAiContentIsRejected() = runTest {
        coEvery { api.getChatCompletion(any()) } returns ChatResponseDto(
            listOf(
                com.example.avito_testing_2026_autum.ai.data.network.dto.ChoiceDto(
                    message = com.example.avito_testing_2026_autum.ai.data.network.dto.MessageDto("assistant", "  ")
                )
            )
        )

        val result = repository.formulateTask("spoken text")

        assertTrue(result.isFailure)
    }

    private object DispatchersProviderImpl : DispatchersProvider {
        override val main = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
        override val unconfined = Dispatchers.Unconfined
    }
}
