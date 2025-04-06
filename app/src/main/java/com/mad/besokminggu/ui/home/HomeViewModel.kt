package com.mad.besokminggu.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import java.util.Date

class HomeViewModel : ViewModel() {

    private val _newSongs = MutableLiveData<List<Song>>()
    val newSongs: LiveData<List<Song>> get() = _newSongs

    private val _recentlyPlayed = MutableLiveData<List<Song>>()
    val recentlyPlayed: LiveData<List<Song>> get() = _recentlyPlayed

    init {
        val songs = dummySongs()
        _newSongs.value = songs.filter { it.lastPlayedAt == null }
        _recentlyPlayed.value = songs.filter { it.lastPlayedAt != null }
            .sortedByDescending { it.lastPlayedAt } // biar yang terakhir diputar muncul duluan
    }

    private fun dummySongs(): List<Song> {
        val now = Date()
        return listOf(
            Song(
                id = 1,
                title = "Blinding Lights",
                artist = "The Weeknd",
                coverResId = R.drawable.cover_starboy,
                filePath = "",
                isLiked = false,
                isPlayed = false,
                createdAt = Date(now.time - 100000),
                lastPlayedAt = null
            ),
            Song(
                id = 2,
                title = "Here Comes The Sun",
                artist = "The Beatles",
                coverResId = R.drawable.cover_starboy,
                filePath = "",
                isLiked = false,
                isPlayed = false,
                createdAt = Date(now.time - 200000),
                lastPlayedAt = null
            ),
            Song(
                id = 3,
                title = "Midnight Pretenders",
                artist = "Tomoko Aran",
                coverResId = R.drawable.cover_starboy,
                filePath = "",
                isLiked = false,
                isPlayed = true,
                createdAt = Date(now.time - 300000),
                lastPlayedAt = Date(now.time - 10000)
            ),
            Song(
                id = 4,
                title = "Violent Crimes",
                artist = "Kanye West",
                coverResId = R.drawable.cover_starboy,
                filePath = "",
                isLiked = false,
                isPlayed = true,
                createdAt = Date(now.time - 400000),
                lastPlayedAt = Date(now.time - 20000)
            )
        )
    }
}