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

fun OnlineSong.toSong(): Song {
    return Song(
        id = id,
        title = title,
        artist = artist,
        ownerId = -1,
        coverFileName = artwork,
        audioFileName = url,
        isLiked = false,
        lastPlayedAt = null,
        createdAt = createdAt
    )
}