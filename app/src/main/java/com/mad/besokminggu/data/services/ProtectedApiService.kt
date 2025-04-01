package com.mad.besokminggu.data.services

import com.mad.besokminggu.data.model.Profile
import com.mad.besokminggu.data.model.VerifyTokenResponse
import retrofit2.Response
import retrofit2.http.*

interface ProtectedApiService {
    @GET("profile")
    suspend fun getProfile(): Response<Profile>

    @GET("verify-token")
    suspend fun verifyToken(): Response<VerifyTokenResponse>
}