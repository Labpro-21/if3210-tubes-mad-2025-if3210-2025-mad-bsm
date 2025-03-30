package com.mad.besokminggu.network

import com.mad.besokminggu.data.model.LoggedInUser
import com.mad.besokminggu.data.model.LoginBody
import com.mad.besokminggu.data.model.Profile
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("api/login")
    fun login(
        @Body loginBody: LoginBody
    ): Call<LoggedInUser>

    @GET("api/profile")
    fun getProfile(
        @Header("Authorization") token: String
    ): Call<Profile>
}