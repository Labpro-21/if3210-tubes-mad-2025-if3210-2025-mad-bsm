package com.mad.besokminggu.data.model

import java.util.Date

data class Profile(
    val id: String,
    val username: String,
    val email: String,
    val profilePhoto: String,
    val location: String,
    val createdAt: Date,
    val updatedAt: Date,
)