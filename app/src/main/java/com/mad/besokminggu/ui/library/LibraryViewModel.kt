package com.mad.besokminggu.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.repositories.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: SongRepository
) : ViewModel() {

    val songs: LiveData<List<Song>> = repository.allSongs

    private fun dummySongs(): List<Song> {
        val now = Date()
        return listOf(
            Song(
                id = 1,
                title = "Blinding Lights",
                artist = "The Weeknd",
                coverFileName = "cover_starboy.png",
                audioFileName = "audio_starboy.mp3",
                isLiked = false,
                isPlayed = false,
                createdAt = Date(now.time - 100000),
                lastPlayedAt = null
            ),
            Song(
                id = 2,
                title = "Here Comes The Sun",
                artist = "The Beatles",
                coverFileName = "cover_starboy.png",
                audioFileName = "audio_starboy.mp3",
                isLiked = false,
                isPlayed = false,
                createdAt = Date(now.time - 200000),
                lastPlayedAt = null
            ),
            Song(
                id = 3,
                title = "Midnight Pretenders",
                artist = "Tomoko Aran",
                coverFileName = "cover_blonde.png",
                audioFileName = "audio_starboy.mp3",
                isLiked = false,
                isPlayed = true,
                createdAt = Date(now.time - 300000),
                lastPlayedAt = Date(now.time - 10000)
            ),
            Song(
                id = 4,
                title = "Violent Crimes",
                artist = "Kanye West",
                coverFileName = "cover_blonde.png",
                audioFileName = "audio_starboy.mp3",
                isLiked = false,
                isPlayed = true,
                createdAt = Date(now.time - 400000),
                lastPlayedAt = Date(now.time - 20000)
            )
        )
    }
    fun insertDummySongs() {

        viewModelScope.launch {
            repository.insertDummySongsIfEmpty(dummySongs())
        }
    }

    fun insertSong(song: Song) {
        viewModelScope.launch {
            repository.insert(song)
        }
    }

}