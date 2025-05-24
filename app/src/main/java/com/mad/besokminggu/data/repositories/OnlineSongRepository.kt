package com.mad.besokminggu.data.repositories

import android.util.Log
import com.mad.besokminggu.data.model.Song
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnlineSongRepository @Inject constructor() {

    private var _allSongs: List<Song>

    init {
        _allSongs = emptyList()
    }

    fun updateAllSongs(songs: List<Song>) {
        _allSongs = songs
        Log.d("OnlineSongRepository", "updateAllSongs: $_allSongs")
    }

//    fun getAllSongs() : List<Song> {
//        return _allSongs
//    }

    fun getNextIteratedSong(currentSong: Song): Song {
        val currentIndex = _allSongs.indexOfFirst { it.id == currentSong.id }
        val nextIndex = if (currentIndex >= 0) currentIndex + 1 else 0
        return _allSongs.getOrNull(nextIndex) ?: _allSongs.first()
    }

    fun getNextRandomSong(currentSong: Song): Song {
//        val currentIndex = _allSongs.indexOfFirst { it.id == currentSong.id }
        val nextIndex = _allSongs.indices.random()
        return _allSongs.getOrNull(nextIndex) ?: _allSongs.first()
    }
}