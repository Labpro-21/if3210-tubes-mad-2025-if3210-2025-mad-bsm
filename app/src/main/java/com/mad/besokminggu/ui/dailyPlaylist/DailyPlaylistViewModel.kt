package com.mad.besokminggu.ui.dailyPlaylist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.besokminggu.data.model.OnlineSong
import com.mad.besokminggu.data.model.PlaylistSong
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.model.toPlaylistSong
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

    private val _dailyPlaylist = MutableLiveData<List<PlaylistSong>>()
    val dailyPlaylist: LiveData<List<PlaylistSong>> = _dailyPlaylist

    private val _playlistDuration = MutableLiveData<String>()
    val playlistDuration: LiveData<String> = _playlistDuration

    private var cachedDate: String? = null
    private var cachedPlaylist: List<PlaylistSong>? = null

    fun generateDailyPlaylist(localSongs: List<Song>, onlineSongs: List<OnlineSong>) {
        val today = getTodayLabel()

        if (today == cachedDate && cachedPlaylist != null) {
            _dailyPlaylist.postValue(cachedPlaylist!!)
            _playlistDuration.postValue(computeDurationString(cachedPlaylist!!.sumOf { it.song.durationInSeconds }))
            Log.d("DailyPlaylist", "Using cached playlist for $today")
            return
        }


        val localPlaylists = localSongs.map { it -> it.toPlaylistSong(false) }
        val onlinePlaylists = onlineSongs.map { it -> it.toSong().toPlaylistSong(true) }


        val likedArtists = localPlaylists.filter { it.song.isLiked }.map { it.song.artist }.toSet()
        val listenedArtists = localPlaylists.map { it.song.artist }.toSet()

        val byLikedArtist = onlinePlaylists.filter { it.song.artist in likedArtists }
        val byListenedArtist = onlinePlaylists.filter { it.song.artist in listenedArtists && it.song.artist !in likedArtists }

        val combined = (byLikedArtist + byListenedArtist)
            .distinctBy { it.song.id }
            .shuffled()

        val finalList = if (combined.size >= 30) {
            combined.take(30)
        } else {
            val additional = onlinePlaylists
                .filter { online -> combined.none { it.song.id == online.song.id } }
                .shuffled()
                .take(30 - combined.size)

            (combined + additional).take(30)
        }


        cachedDate = today
        cachedPlaylist = finalList

        _dailyPlaylist.postValue(finalList)
        _playlistDuration.postValue(computeDurationString(finalList.sumOf { it.song.durationInSeconds }))
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
            val localPlaylits = localSongs.map { it -> it.toPlaylistSong(false) }

            if (topSongsResponse is ApiResponse.Success) {
                val onlineSongs = topSongsResponse.data
                val onlinePlayList = onlineSongs.map {it -> it.toSong().toPlaylistSong(true)}

                if (localPlaylits.isEmpty()) {
                    val playlist = onlinePlayList
                        .distinctBy { it.song.id }
                        .shuffled()
                        .take(30)

                    _dailyPlaylist.postValue(playlist)
                    _playlistDuration.postValue(computeDurationString(playlist.sumOf { it.song.durationInSeconds }))
                } else {
                    // normal combine
                    generateDailyPlaylist(localSongs, onlineSongs)
                }
            }
        }
    }


}
