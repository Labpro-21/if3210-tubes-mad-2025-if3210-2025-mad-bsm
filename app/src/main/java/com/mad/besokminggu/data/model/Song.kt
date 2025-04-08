package com.mad.besokminggu.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mad.besokminggu.data.Converters
import java.util.Date

@Entity(tableName = "songs")
@TypeConverters(Converters::class)
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val coverFileName: String = "",
    val audioFileName: String = "",
    val isLiked: Boolean = false,
    val isPlayed: Boolean = false,
    val createdAt: Date,
    val lastPlayedAt: Date? = null
)
