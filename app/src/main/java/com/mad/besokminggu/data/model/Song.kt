package com.mad.besokminggu.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mad.besokminggu.data.Converters
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Entity(tableName = "songs")
@TypeConverters(Converters::class)
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val ownerId: Int,
    val coverFileName: String = "",
    val audioFileName: String = "",
    val isLiked: Boolean = false,
    val lastPlayedAt: Date? = null,
    val createdAt: Date,
) : Parcelable
