package com.mad.besokminggu.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.mad.besokminggu.data.LoginRepository
import com.mad.besokminggu.data.Result

import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.LoggedInUser
import com.mad.besokminggu.data.model.LoginBody
import com.mad.besokminggu.data.model.Profile
import com.mad.besokminggu.network.ApiConfig
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _loginResponse = MutableLiveData<LoggedInUser>()
    val loginResponse: LiveData<LoggedInUser> get() = _loginResponse

    private val _profileResponse = MutableLiveData<Profile>()
    val profileResponse: LiveData<Profile> get() = _profileResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> get() = _isError

    fun login(email: String, password: String) {
//        // Coroutine
//        viewModelScope.launch {
//            try {
//                _isLoading.postValue(true)
//
//                // First request: Login
//                val loggedInUser = ApiConfig.getApiService().login(LoginBody(email, password)).execute()
//                val loggedInUserResponse = loggedInUser.body()
//
//                if (loggedInUser.isSuccessful.not() || loggedInUserResponse == null || loggedInUserResponse.accessToken == "") {
//                    _isError.postValue(true)
//                    Toast.makeText(null, "Login failed: ${loggedInUser.message()}", Toast.LENGTH_SHORT).show()
//                    return@launch
//                }
//
//                // Second request: Fetch user details after login succeeds
//                val profile = ApiConfig.getApiService().getProfile(loggedInUserResponse.accessToken).execute()
//
//                _loginResponse.postValue(loggedInUser.body())
//                _profileResponse.postValue(profile.body())
//                _isError.postValue(false)
//
//                if (profile.isSuccessful.not() || profile.body() == null) {
//                    _isError.postValue(true)
//                    Toast.makeText(null, "Failed to fetch profile: ${profile.message()}", Toast.LENGTH_SHORT).show()
//                    return@launch
//                }
//
//                // Update the login result
//                _loginResult.postValue(LoginResult(success = LoggedInUserView(displayName = profile.body()!!.username)))
//            } catch (e: Exception) {
//                _isError.postValue(true)
//                Toast.makeText(null, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//            } finally {
//                _isLoading.postValue(false)
//            }
//        }

        loginRepository.login(email, password) { result ->
            when (result) {
                is Result.Success -> {
                    _isError.postValue(false)
                    _loginResult.postValue(LoginResult(success = LoggedInUserView(displayName = result.data.username)))
                }
                is Result.Error -> {
                    _isError.postValue(true)
                    Toast.makeText(null, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun loginDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _loginForm.value = LoginFormState(emailError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder email validation check
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8
    }
}