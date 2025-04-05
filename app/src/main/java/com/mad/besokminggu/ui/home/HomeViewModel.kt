package com.mad.besokminggu.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mad.besokminggu.data.model.Song
import java.util.Date

class HomeViewModel : ViewModel() {

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    init {
        _songs.value = dummySongs() // fungsi dummySongs
    }

    private fun dummySongs(): List<Song> {
        return listOf(
            Song(1, "Blinding Lights", "The Weeknd", "https://example.com/blinding.jpg", Date(System.currentTimeMillis() - 100000)),
            Song(2, "Here Comes The Sun", "The Beatles", "https://example.com/sun.jpg", Date(System.currentTimeMillis() - 200000)),
            Song(3, "Midnight Pretenders", "Tomoko Aran", "https://example.com/midnight.jpg", Date(System.currentTimeMillis() - 300000)),
            Song(4, "Violent Crimes", "Kanye West", "https://example.com/violent.jpg", Date(System.currentTimeMillis() - 400000))
        )
    }
}

