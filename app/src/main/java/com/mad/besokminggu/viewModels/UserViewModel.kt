package com.mad.besokminggu.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.data.model.Profile
import com.mad.besokminggu.data.repositories.ProtectedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val protectedRepository: ProtectedRepository,
): BaseViewModel() {

    private val _profileResponse = MutableLiveData<ApiResponse<Profile>>()
    val profileResponse: LiveData<ApiResponse<Profile>> = _profileResponse

    private val _profileImagePath = MutableLiveData<String>()
    var profileImagePath: LiveData<String> = _profileImagePath

    fun getProfile(coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(
        _profileResponse,
        coroutinesErrorHandler
    ) {
        protectedRepository.getProfile()
    }
}