package com.mad.besokminggu.network

import com.mad.besokminggu.data.model.RefreshTokenBody
import com.mad.besokminggu.data.model.RefreshTokenResponse
import com.mad.besokminggu.data.services.UnprotectedApiService
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class RefreshTokenInterceptor @Inject constructor(
    private val tokenManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!response.shouldRefreshToken()) {
            return response
        }

        if (responseCount(response) > 3) {
            tokenManager.clearToken()
            tokenManager.clearUserProfile()
            return response
        }

        // Get Refresh Token
        val refreshToken = runBlocking {
            tokenManager.getToken().firstOrNull()?.second
        } ?: run {
            tokenManager.clearToken()
            tokenManager.clearUserProfile()
            return response
        }

        // Get New Access Token
        val newTokenResponse = try {
            runBlocking {
                getNewToken(refreshToken ?: "")
            }
        } catch (e: Exception) {
            null // Network failure
        }

        return if (newTokenResponse?.isSuccessful == true) {
            response.close()
            newTokenResponse.body()?.let { newToken ->
                tokenManager.storeAccessToken(newToken.accessToken, newToken.refreshToken)
                val newRequest = request.newBuilder()
                    .header("Authorization", "Bearer ${newToken.accessToken}")
                    .build()
                chain.proceed(newRequest)
            } ?: response
        } else {
            tokenManager.clearToken()
            tokenManager.clearUserProfile()
            response
        }
    }

    private suspend fun getNewToken(refreshToken: String): retrofit2.Response<RefreshTokenResponse> {
        // Create logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        // Create OkHttpClient
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        // Create Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl("http://34.101.226.132:3000/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        // Make the network call
        return try {
            val service = retrofit.create(UnprotectedApiService::class.java)
            service.refreshToken(RefreshTokenBody(refreshToken))
        } catch (e: Exception) {
            // Return an error response if the call fails
            retrofit2.Response.error(500, "{}".toResponseBody("application/json".toMediaType()))
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 0
        var r: Response? = response
        while (r != null) {
            count++
            r = r.priorResponse
        }
        return count
    }

    private fun Response.shouldRefreshToken(): Boolean {
        return code == 401 || code == 403
    }
}