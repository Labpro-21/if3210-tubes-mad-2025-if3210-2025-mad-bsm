package com.mad.besokminggu.data.services

import com.mad.besokminggu.data.model.LoginResponse
import com.mad.besokminggu.data.model.LoginBody
import com.mad.besokminggu.data.model.RefreshTokenBody
import com.mad.besokminggu.data.model.RefreshTokenResponse
import retrofit2.Response
import retrofit2.http.*

interface UnprotectedApiService {
    @POST("login")
    suspend fun login(
        @Body loginBody: LoginBody
    ): Response<LoginResponse>

    @POST("refresh-token")
    suspend fun refreshToken(
        @Body refreshToken: RefreshTokenBody
    ): Response<RefreshTokenResponse>
}