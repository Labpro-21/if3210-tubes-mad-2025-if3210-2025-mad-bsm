package com.mad.besokminggu.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.besokminggu.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TokenViewModel @Inject constructor(
    private val tokenManager: SessionManager,
): ViewModel() {

    val _accessToken = MutableLiveData<String?>()
    val _refreshToken = MutableLiveData<String?>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            tokenManager.getToken().collect{
                withContext(Dispatchers.Main) {
                    _accessToken.value = it.first
                    _refreshToken.value = it.second
                }
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
}