package com.mad.besokminggu.data.model

import java.util.Date

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val accessToken: String,
    val refreshToken: String,
) {
    operator fun not(): Boolean {
        if (this.accessToken.isEmpty() ||
            this.refreshToken.isEmpty()
        ) {
            return true
        }

        return false
    }
}

data class LoginBody(
    val email: String,
    val password: String,
)

data class Profile(
    val id: String,
    val username: String,
    val email: String,
    val profilePhoto: String,
    val location: String,
    val createdAt: Date,
    val updatedAt: Date,
)