package com.mad.besokminggu.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val coverResId: Int,
    val filePath: String = "",
    val isLiked: Boolean = false
)