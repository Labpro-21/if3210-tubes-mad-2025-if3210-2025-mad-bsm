package com.mad.besokminggu.data.model

import java.util.Date

data class StreakInfo(
    val startDate: Date,
    val endDate: Date,
    val streakLength: Int,
    val streakSongTitle: String,
    val streakSongArtist: String,
    val coverFileName: String
)

