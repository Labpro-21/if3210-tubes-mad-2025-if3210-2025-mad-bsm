package com.mad.besokminggu.network

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: SessionManager,
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            tokenManager.getAccessToken()
                .catch { e -> emit(null) }
                .firstOrNull()
        }
        val request = chain.request().newBuilder()
        request.addHeader("Authorization", "Bearer ${token}")
        return chain.proceed(request.build())
    }
}