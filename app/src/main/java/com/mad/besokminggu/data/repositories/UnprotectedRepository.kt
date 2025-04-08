package com.mad.besokminggu.data.repositories

import com.mad.besokminggu.data.model.LoginBody
import com.mad.besokminggu.data.model.RefreshTokenBody
import com.mad.besokminggu.data.services.UnprotectedApiService
import com.mad.besokminggu.network.apiRequestFlow
import javax.inject.Inject

class UnprotectedRepository @Inject constructor(
    private val unprotectedApiService: UnprotectedApiService,
) {
    fun login(auth: LoginBody) = apiRequestFlow {
        unprotectedApiService.login(auth)
    }

    fun refreshToken(refreshToken: String) = apiRequestFlow {
        unprotectedApiService.refreshToken(RefreshTokenBody(refreshToken))
    }
}