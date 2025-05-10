package com.mad.besokminggu.data.model

import java.util.Date

data class OnlineSong (
    val id: Int,
    val title: String,
    val artist: String,
    val artwork: String,
    val url: String,
    val duration: String,
    val country: String,
    val rank: Int,
    val createdAt: Date,
    val updatedAt: Date
)