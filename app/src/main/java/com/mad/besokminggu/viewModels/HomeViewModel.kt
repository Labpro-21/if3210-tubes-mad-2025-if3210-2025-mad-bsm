package com.mad.besokminggu.viewModels

import android.view.animation.Transformation
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.repositories.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository
): ViewModel() {

    private val allSongs: LiveData<List<Song>> = songRepository.allSongs

    val newSongs: LiveData<List<Song>> = allSongs.map { songs ->
        songs.filter { it.lastPlayedAt == null }

    }

    val recentlyPlayed: LiveData<List<Song>> = allSongs.map{ songs ->
        songs.filter { it.lastPlayedAt != null }
            .sortedByDescending { it.lastPlayedAt }
    }

    init {
        viewModelScope.launch {

            songRepository.insertDummySongsIfEmpty(dummySongs())

        }
    }

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
}