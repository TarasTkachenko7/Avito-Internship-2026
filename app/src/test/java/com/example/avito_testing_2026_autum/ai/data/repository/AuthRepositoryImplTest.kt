package com.example.avito_testing_2026_autum.ai.data.repository

import com.example.avito_testing_2026_autum.ai.data.network.api.GigaChatAuthApi
import com.example.avito_testing_2026_autum.ai.data.network.dto.TokenResponseDto
import com.example.avito_testing_2026_autum.core.dispatchers.DispatchersProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {
    private val api = mockk<GigaChatAuthApi>()

    @Test
    fun concurrent401RefreshesUseOneNewTokenRequest() = runTest {
        val gate = CompletableDeferred<TokenResponseDto>()
        var calls = 0
        coEvery { api.getAccessToken(any(), any(), any()) } coAnswers {
            calls++
            if (calls == 1) TokenResponseDto("old", System.currentTimeMillis() + 3_600_000L)
            else gate.await()
        }
        val repository = AuthRepositoryImpl(api, "key", testDispatchers(UnconfinedTestDispatcher(testScheduler)))

        val initial = async { repository.getAccessToken() }
        runCurrent()
        assertEquals("old", initial.await().getOrThrow())

        val refreshOne = async { repository.refreshAccessToken("old") }
        val refreshTwo = async { repository.refreshAccessToken("old") }
        runCurrent()
        gate.complete(TokenResponseDto("new", System.currentTimeMillis() + 3_600_000L))
        advanceUntilIdle()

        assertEquals("new", refreshOne.await().getOrThrow())
        assertEquals("new", refreshTwo.await().getOrThrow())
        coVerify(exactly = 2) { api.getAccessToken(any(), any(), any()) }
    }

    @Test
    fun concurrentInitialRequestsShareOneOAuthCall() = runTest {
        val gate = CompletableDeferred<TokenResponseDto>()
        coEvery { api.getAccessToken(any(), any(), any()) } coAnswers { gate.await() }
        val repository = AuthRepositoryImpl(api, "key", testDispatchers(UnconfinedTestDispatcher(testScheduler)))

        val first = async { repository.getAccessToken() }
        val second = async { repository.getAccessToken() }
        runCurrent()
        gate.complete(TokenResponseDto("shared-token", System.currentTimeMillis() + 3_600_000L))
        advanceUntilIdle()

        assertEquals("shared-token", first.await().getOrThrow())
        assertEquals("shared-token", second.await().getOrThrow())
        coVerify(exactly = 1) { api.getAccessToken(any(), any(), any()) }
    }

    private fun testDispatchers(dispatcher: CoroutineDispatcher) = object : DispatchersProvider {
        override val main = dispatcher
        override val io = dispatcher
        override val default = dispatcher
        override val unconfined = dispatcher
    }
}
