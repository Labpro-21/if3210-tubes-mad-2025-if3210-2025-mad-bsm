package com.mad.besokminggu.data.model

data class AuthUser (
    val id: Int,
    val username: String,
)

data class VerifyTokenResponse (
    val valid: Boolean,
    val user: AuthUser,
)

data class RefreshTokenBody (
    val refreshToken: String,
)

data class RefreshTokenResponse (
    val accessToken: String,
    val refreshToken: String
)

data class LoginBody(
    val email: String,
    val password: String,
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
)