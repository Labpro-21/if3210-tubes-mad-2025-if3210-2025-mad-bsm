package com.mad.besokminggu.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.besokminggu.data.model.LoginBody
import com.mad.besokminggu.data.model.RefreshTokenResponse
import com.mad.besokminggu.data.repositories.ProtectedRepository
import com.mad.besokminggu.data.repositories.UnprotectedRepository
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TokenViewModel @Inject constructor(
    private val tokenManager: SessionManager,
    private val unprotectedRepository: UnprotectedRepository
): BaseViewModel() {

    val _accessToken = MutableLiveData<String?>()
    val _refreshToken = MutableLiveData<String?>()

    val _refreshTokenResponse = MutableLiveData<ApiResponse<RefreshTokenResponse>>()
    val refreshTokenResponse = _refreshTokenResponse

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getToken()
        }
    }

    suspend fun getToken(){
        tokenManager.getToken().collect{
            withContext(Dispatchers.Main) {
                _accessToken.value = it.first
                _refreshToken.value = it.second
            }
        }
    }

    fun saveToken(accessToken: String, refreshToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tokenManager.storeAccessToken(accessToken, refreshToken )
        }
    }

    fun deleteToken() {
        viewModelScope.launch(Dispatchers.IO) {
            tokenManager.clearToken()
        }
    }

    fun refreshToken(coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(
        _refreshTokenResponse,
        coroutinesErrorHandler
    ) {
        unprotectedRepository.refreshToken(_refreshToken.value ?: "")
    }
}