package com.mad.besokminggu.data.repositories

import com.mad.besokminggu.data.services.ProtectedApiService
import com.mad.besokminggu.network.apiRequestFlow
import javax.inject.Inject

class ProtectedRepository @Inject constructor(
    private val protectedApiService: ProtectedApiService,
) {
    fun getProfile() = apiRequestFlow {
        protectedApiService.getProfile()
    }
}