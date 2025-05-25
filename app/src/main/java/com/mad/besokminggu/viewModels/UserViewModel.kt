package com.mad.besokminggu.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mad.besokminggu.data.model.PatchProfileResponse
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.data.model.Profile
import com.mad.besokminggu.data.repositories.ProtectedRepository
import com.mad.besokminggu.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val tokenManager: SessionManager,
    private val protectedRepository: ProtectedRepository,
): BaseViewModel() {

    private val _profileResponse = MutableLiveData<ApiResponse<Profile>>()
    val profileResponse: LiveData<ApiResponse<Profile>> = _profileResponse

    private val _profilePhotoResponse = MutableLiveData<ApiResponse<PatchProfileResponse>>()
    val profilePhotoResponse: LiveData<ApiResponse<PatchProfileResponse>> = _profilePhotoResponse

    val _profile = MutableLiveData<Profile>()
    val profile : LiveData<Profile> = _profile

    private val _profileImagePath = MutableLiveData<String>()
    var profileImagePath: LiveData<String> = _profileImagePath

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getProfile(object : CoroutinesErrorHandler {
                override fun onError(message: String) {
                    Log.e("USER_VIEW_MODEL", "Error :  ${message}")
                }
            })
            getProfileData()
        }
    }

    fun patchProfile(coroutinesErrorHandler: CoroutinesErrorHandler, location: String? = null, profilePhoto: File? = null) = baseRequest(
        _profilePhotoResponse,
        coroutinesErrorHandler
    ) {
        protectedRepository.patchProfile(
            location = location,
            profilePhoto = profilePhoto
        )
    }

    fun getProfile(coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(
        _profileResponse,
        coroutinesErrorHandler
    ) {
        protectedRepository.getProfile()
    }

    fun getProfileData() {
        viewModelScope.launch(Dispatchers.IO) {
            val temp = tokenManager.getUserProfile()
            if (temp != null) {
                Log.d("USER_VIEW_MODEL", "Profile: ${temp}")
                _profile.postValue(temp!!)
            } else {
                protectedRepository.getProfile().collect { apiResponse ->
                    when (apiResponse) {
                        is ApiResponse.Success -> {
                            val profileResponse = apiResponse.data
                            _profile.postValue(profileResponse)
                            _profileImagePath.postValue(profileResponse.profilePhoto)
                            tokenManager.storeUserProfile(profileResponse)
                        }

                        is ApiResponse.Failure -> {

                        }

                        is ApiResponse.Loading -> {

                        }
                    }
                }
            }
        }
    }
}