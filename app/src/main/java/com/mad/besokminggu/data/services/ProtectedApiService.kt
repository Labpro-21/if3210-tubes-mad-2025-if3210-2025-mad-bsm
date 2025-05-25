package com.mad.besokminggu.data.services

import com.mad.besokminggu.data.model.PatchProfileResponse
import com.mad.besokminggu.data.model.Profile
import com.mad.besokminggu.data.model.VerifyTokenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ProtectedApiService {
    @GET("profile")
    suspend fun getProfile(): Response<Profile>

    @GET("verify-token")
    suspend fun verifyToken(): Response<VerifyTokenResponse>

    @Multipart
    @PATCH("profile")
    suspend fun patchProfile(
        @Part("location") location: RequestBody?,
        @Part profilePhoto: MultipartBody.Part?
    ): Response<PatchProfileResponse>
}