package com.mad.besokminggu.data

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mad.besokminggu.data.model.LoggedInUser
import com.mad.besokminggu.data.model.LoginBody
import com.mad.besokminggu.data.model.Profile
import com.mad.besokminggu.network.ApiConfig
import java.io.IOException
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import java.util.Date

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    private val _loginResponse = MutableLiveData<LoggedInUser>()
    val loginResponse: LiveData<LoggedInUser> get() = _loginResponse

    private val _profileResponse = MutableLiveData<Profile>()
    val profileResponse: LiveData<Profile> get() = _profileResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> get() = _isError

    var errorMessage: String = ""
        private set

    fun login(email: String, password: String, callback: (Result<Profile>) -> Unit) {
        val client = ApiConfig.getApiService().login(LoginBody(email, password))
        _isLoading.postValue(true)

        // Login
        client.enqueue(object : Callback<LoggedInUser> {
            override fun onResponse(
                call: Call<LoggedInUser>,
                response: Response<LoggedInUser>
            ) {
                val responseBody = response.body()
                if (!response.isSuccessful || responseBody == null) {
                    Toast.makeText(
                        null,
                        "Login failed: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                _isError.postValue(false)

                // Fetch profile
                val profileClient = ApiConfig.getApiService().getProfile("Bearer ${responseBody.accessToken}")
                println("Profile client: $profileClient")

                profileClient.enqueue(object : Callback<Profile> {
                    override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
                        val profileResponseBody = response.body()
                        if (!response.isSuccessful || profileResponseBody == null) {
                            Toast.makeText(
                                null,
                                "Fetch profile failed: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        _isError.postValue(false)

                        _profileResponse.postValue(profileResponseBody.copy())

                        callback(Result.Success(profileResponseBody))
                    }

                    override fun onFailure(call: Call<Profile>, t: Throwable) {
                        println("Error: ${t.message}")
                        _isLoading.postValue(false)
                        _isError.postValue(true)
                        Toast.makeText(
                            null,
                            "Fetch profile failed: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        t.printStackTrace()
                        return
                    }
                })
            }

            override fun onFailure(call: Call<LoggedInUser>, t: Throwable) {
                _isLoading.postValue(false)
                _isError.postValue(true)
                Toast.makeText(
                    null,
                    "Login failed: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                t.printStackTrace()
                return
            }
        })
    }

    fun logout() {
        // TODO: revoke authentication
    }
}