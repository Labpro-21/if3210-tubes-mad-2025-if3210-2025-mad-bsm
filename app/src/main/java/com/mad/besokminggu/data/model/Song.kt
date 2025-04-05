package com.mad.besokminggu.data.model

import java.util.Date

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val coverImage: String,
    val createdAt: Date
)