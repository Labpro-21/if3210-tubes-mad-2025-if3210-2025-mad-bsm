package com.mad.besokminggu.ui.dailyPlaylist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.besokminggu.data.model.OnlineSong
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.model.toSong
import com.mad.besokminggu.data.repositories.SongRepository
import com.mad.besokminggu.network.ApiResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DailyPlaylistViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {

    private val _dailyPlaylist = MutableLiveData<List<Song>>()
    val dailyPlaylist: LiveData<List<Song>> = _dailyPlaylist

    private val _playlistDuration = MutableLiveData<String>()
    val playlistDuration: LiveData<String> = _playlistDuration

    private var cachedDate: String? = null
    private var cachedPlaylist: List<Song>? = null

    fun generateDailyPlaylist(localSongs: List<Song>, onlineSongs: List<OnlineSong>) {
        val today = getTodayLabel()

        if (today == cachedDate && cachedPlaylist != null) {
            _dailyPlaylist.postValue(cachedPlaylist!!)
            _playlistDuration.postValue(computeDurationString(cachedPlaylist!!.sumOf { it.durationInSeconds }))
            Log.d("DailyPlaylist", "Using cached playlist for $today")
            return
        }

        val likedArtists = localSongs.filter { it.isLiked }.map { it.artist }.toSet()
        val listenedArtists = localSongs.map { it.artist }.toSet()

        val byLikedArtist = onlineSongs.filter { it.artist in likedArtists }
        val byListenedArtist = onlineSongs.filter { it.artist in listenedArtists && it.artist !in likedArtists }

        val combined = (byLikedArtist + byListenedArtist)
            .distinctBy { it.id }
            .shuffled()

        val finalList = if (combined.size >= 30) {
            combined.take(30)
        } else {
            val additional = onlineSongs
                .filter { online -> combined.none { it.id == online.id } }
                .shuffled()
                .take(30 - combined.size)

            (combined + additional).take(30)
        }

        val songs = finalList.map { it.toSong() }

        cachedDate = today
        cachedPlaylist = songs

        _dailyPlaylist.postValue(songs)
        _playlistDuration.postValue(computeDurationString(songs.sumOf { it.durationInSeconds }))
    }


    fun getTodayLabel(): String {
        val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun computeDurationString(totalSec: Int): String {
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        return if (hours > 0) "${hours}h ${minutes}min" else "${minutes}min"
    }

    fun loadLocalSongsAndGenerate(
        ownerId: Int,
        topSongsResponse: ApiResponse<List<OnlineSong>>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val localSongs = songRepository.getAllSongsSync(ownerId)

            if (topSongsResponse is ApiResponse.Success) {
                val onlineSongs = topSongsResponse.data

                if (localSongs.isEmpty()) {
                    val playlist = onlineSongs
                        .distinctBy { it.id }
                        .shuffled()
                        .take(30)
                        .map { it.toSong() }

                    _dailyPlaylist.postValue(playlist)
                    _playlistDuration.postValue(computeDurationString(playlist.sumOf { it.durationInSeconds }))
                } else {
                    // normal combine
                    generateDailyPlaylist(localSongs, onlineSongs)
                }
            }
        }
    }


}
