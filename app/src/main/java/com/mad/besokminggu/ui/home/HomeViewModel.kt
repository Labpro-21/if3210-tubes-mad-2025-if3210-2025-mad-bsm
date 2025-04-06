package com.mad.besokminggu.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
            Song(1, "Blinding Lights", "The Weeknd", "https://example.com/blinding.jpg", Date(now.time - 100000)),
            Song(2, "Here Comes The Sun", "The Beatles", "https://example.com/sun.jpg", Date(now.time - 200000)),
            Song(3, "Midnight Pretenders", "Tomoko Aran", "https://example.com/midnight.jpg", Date(now.time - 300000), Date(now.time - 10000)),
            Song(4, "Violent Crimes", "Kanye West", "https://example.com/violent.jpg", Date(now.time - 400000), Date(now.time - 20000))
        )
    }
}

