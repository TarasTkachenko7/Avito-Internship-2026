package com.example.avito_testing_2026_autum.ai.data.network.interceptor

import com.example.avito_testing_2026_autum.ai.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class AuthInterceptorTest {
    private val authRepository = mockk<AuthRepository>()
    private val chain = mockk<Interceptor.Chain>()

    @Test
    fun refreshes401OnceAndRetriesWithNewBearerToken() {
        val request = Request.Builder().url("https://api.giga.chat/test").build()
        val requests = mutableListOf<Request>()
        every { chain.request() } returns request
        every { chain.proceed(any()) } answers {
            val sentRequest = firstArg<Request>()
            requests += sentRequest
            responseFor(sentRequest, if (requests.size == 1) 401 else 200)
        }
        coEvery { authRepository.getAccessToken() } returns Result.success("old-token")
        coEvery { authRepository.refreshAccessToken("old-token") } returns Result.success("new-token")

        val response = AuthInterceptor(authRepository).intercept(chain)

        assertEquals(200, response.code)
        assertEquals("Bearer old-token", requests[0].header("Authorization"))
        assertEquals("Bearer new-token", requests[1].header("Authorization"))
        verify(exactly = 2) { chain.proceed(any()) }
    }

    @Test
    fun doesNotRetry401WhenRefreshDoesNotProduceANewToken() {
        val request = Request.Builder().url("https://api.giga.chat/test").build()
        every { chain.request() } returns request
        every { chain.proceed(any()) } answers { responseFor(firstArg(), 401) }
        coEvery { authRepository.getAccessToken() } returns Result.success("same-token")
        coEvery { authRepository.refreshAccessToken("same-token") } returns Result.success("same-token")

        val response = AuthInterceptor(authRepository).intercept(chain)

        assertEquals(401, response.code)
        verify(exactly = 1) { chain.proceed(any()) }
    }

    @Test
    fun oauthFailureIsPropagatedWithoutSendingUnauthenticatedApiRequest() {
        val request = Request.Builder().url("https://api.giga.chat/test").build()
        val cause = IllegalStateException("OAuth unavailable")
        every { chain.request() } returns request
        coEvery { authRepository.getAccessToken() } returns Result.failure(cause)

        try {
            AuthInterceptor(authRepository).intercept(chain)
            fail("Expected the original OAuth failure")
        } catch (error: IllegalStateException) {
            assertEquals(cause, error)
        }

        verify(exactly = 0) { chain.proceed(any()) }
    }

    @Test
    fun refreshFailureIsPropagatedWithoutRepeatingTheRejectedRequest() {
        val request = Request.Builder().url("https://api.giga.chat/test").build()
        val cause = IllegalStateException("OAuth refresh unavailable")
        val body = mockk<ResponseBody>(relaxed = true)
        val rejectedResponse = responseFor(request, 401).newBuilder().body(body).build()
        every { chain.request() } returns request
        every { chain.proceed(any()) } returns rejectedResponse
        coEvery { authRepository.getAccessToken() } returns Result.success("expired-token")
        coEvery { authRepository.refreshAccessToken("expired-token") } returns Result.failure(cause)

        try {
            AuthInterceptor(authRepository).intercept(chain)
            fail("Expected the original OAuth refresh failure")
        } catch (error: IllegalStateException) {
            assertEquals(cause, error)
        }

        verify(exactly = 1) { chain.proceed(any()) }
        verify(exactly = 1) { body.close() }
    }

    private fun responseFor(request: Request, code: Int) = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(if (code == 401) "Unauthorized" else "OK")
        .body("".toResponseBody())
        .build()
}
