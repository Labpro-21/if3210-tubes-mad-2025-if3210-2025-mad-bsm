package com.mad.besokminggu.data.model

import java.io.File
import java.util.Date

data class Profile(
    val id: Int,
    val username: String,
    val email: String,
    val profilePhoto: String,
    val location: String,
    val createdAt: Date,
    val updatedAt: Date,
)

data class PatchProfileResponse(
    val message: String
)