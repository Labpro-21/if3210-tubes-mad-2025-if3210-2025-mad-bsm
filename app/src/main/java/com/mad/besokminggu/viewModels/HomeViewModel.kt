package com.mad.besokminggu.viewModels

import android.util.Log
import android.view.animation.Transformation
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.android.identity.crypto.EcPrivateKey
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.OnlineSong
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.model.toSong
import com.mad.besokminggu.data.repositories.SongRepository
import com.mad.besokminggu.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val tokenManager: SessionManager
): ViewModel() {

    var profile = tokenManager.getUserProfile()
    var allSongs: LiveData<List<Song>> = songRepository.getAllSongs(profile?.id ?: -1)

    val _newSongs = MutableLiveData<List<Song>>()
    val newSongs: LiveData<List<Song>> = _newSongs

    val _recentlyPlayed = MutableLiveData<List<Song>>()
    val recentlyPlayed: LiveData<List<Song>> = _recentlyPlayed

    val _dailyPlaylist = MutableLiveData<List<Song>>()
    val dailyPlaylist: LiveData<List<Song>> get() = _dailyPlaylist

    val _topSongs = MutableLiveData<List<OnlineSong>>()
    val topSongs: LiveData<List<OnlineSong>> get() = _topSongs

    val _dailyPlaylistDuration = MutableLiveData<String>()
    val dailyPlaylistDuration: LiveData<String> get() = _dailyPlaylistDuration

    fun updateTopSongs(songs: List<OnlineSong>) {
        _topSongs.postValue(songs)
    }

    fun refreshSong(ownerId: Int) {
        profile = tokenManager.getUserProfile()
        allSongs = songRepository.getAllSongs(ownerId)
    }

    fun generateDailyPlaylist(localSongs: List<Song>, onlineSongs: List<OnlineSong>) {
        val likedArtists = localSongs.filter { it.isLiked }.map { it.artist }.toSet()
        val listenedArtists = localSongs.map { it.artist }.toSet()
        val listenedCountries = localSongs.mapNotNull { it.artist.takeIf { it.isNotBlank() } }.toSet()

        val byLikedArtist = onlineSongs.filter { it.artist in likedArtists }
        val byListenedArtist = onlineSongs.filter { it.artist in listenedArtists && it.artist !in likedArtists }
        val byCountry = onlineSongs.filter { it.country in listenedCountries && it.artist !in listenedArtists }

        val combined = (byLikedArtist + byListenedArtist + byCountry)
            .distinctBy { it.id }
            .shuffled()
            .take(30)

        _dailyPlaylist.postValue(combined.map { it.toSong() })
        _dailyPlaylistDuration.postValue(computeTotalDuration(combined))
    }




    private fun formatSecondsToHourMin(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        return if (h > 0) "${h}h ${m}min" else "${m}min"
    }

    fun computeTotalDuration(onlineSongs: List<OnlineSong>): String {
        val totalSeconds = onlineSongs.sumOf { song ->
            val parts = song.duration.split(":")
            if (parts.size == 2) {
                val min = parts[0].toIntOrNull() ?: 0
                val sec = parts[1].toIntOrNull() ?: 0
                min * 60 + sec
            } else 0
        }
        return formatSecondsToHourMin(totalSeconds)
    }





}