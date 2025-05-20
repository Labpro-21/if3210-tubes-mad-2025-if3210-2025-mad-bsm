package com.mad.besokminggu.data.repositories

import android.util.Log
import com.mad.besokminggu.data.services.ProtectedApiService
import com.mad.besokminggu.network.apiRequestFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class ProtectedRepository @Inject constructor(
    private val protectedApiService: ProtectedApiService,
) {
    fun getProfile() = apiRequestFlow {
        protectedApiService.getProfile()
    }

    fun patchProfile(
        location: String?,
        profilePhoto: File?,
    ) = apiRequestFlow {
        val locationPart = location?.let {
            RequestBody.create("text/plain".toMediaTypeOrNull(), it)
        }
        Log.d("ProtectedRepository", "patchProfile: $locationPart")
        val photoPart = profilePhoto?.let {
            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profilePhoto", it.name, requestFile)
        }
        protectedApiService.patchProfile(
            location=locationPart,
            profilePhoto=photoPart
        )
    }
}