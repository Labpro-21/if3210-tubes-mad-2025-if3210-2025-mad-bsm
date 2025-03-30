package com.mad.besokminggu.data

import com.mad.besokminggu.data.model.LoggedInUser
import com.mad.besokminggu.data.model.Profile

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: Profile? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    fun login(email: String, password: String, callback: (Result<Profile>) -> Unit) {
        dataSource.login(email, password) { result ->
            when (result) {
                is Result.Success -> {
                    val profile = result.data
                    println("Profile fetched successfully: $profile")
                    setLoggedProfile(profile)
                    callback(Result.Success(profile))
                }
                is Result.Error -> {
                    println("Error: ${result.exception.message}")
                    callback(Result.Error(result.exception))
                }
            }
        }
    }

    private fun setLoggedProfile(profile: Profile) {
        this.user = profile
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}