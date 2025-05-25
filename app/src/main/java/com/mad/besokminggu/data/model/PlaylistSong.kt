package com.mad.besokminggu.data.model

data class PlaylistSong(
    val song : Song,
    val isOnline: Boolean
)

fun Song.toPlaylistSong(isOnline : Boolean) : PlaylistSong {
    return PlaylistSong(
        song = Song(
            id = id,
            title = title,
            artist = artist,
            ownerId = ownerId,
            coverFileName = coverFileName,
            audioFileName = audioFileName,
            isLiked = isLiked,
            lastPlayedAt = lastPlayedAt,
            createdAt = createdAt
        ),
        isOnline = isOnline
    )
}
