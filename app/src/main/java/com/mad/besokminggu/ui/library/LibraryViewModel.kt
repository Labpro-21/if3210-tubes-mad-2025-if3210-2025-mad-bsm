package com.mad.besokminggu.ui.library

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.repositories.SongRepository
import com.mad.besokminggu.manager.FileHelper
import com.mad.besokminggu.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: SongRepository,
    private val tokenManager: SessionManager
) : ViewModel() {

    val profile = tokenManager.getUserProfile()
    val songs: LiveData<List<Song>> = repository.getAllSongs(profile?.id ?: -1)

    private val _filteredSongs = MediatorLiveData<List<Song>>()
    val filteredSongs: LiveData<List<Song>> = _filteredSongs

    private var showLikedOnly = false

    private var searchQuery: String = ""

    fun getSong(id: Int): LiveData<Song> {
        return repository.getSong(id)
    }

    fun insertSong(song: Song) {
        viewModelScope.launch {
            repository.insert(song)
        }
    }

    fun updateSong(song: Song) {
        viewModelScope.launch {
            repository.update(song)
        }
    }

    init {
        _filteredSongs.addSource(songs) { newSongs ->
            applyFilter(newSongs)
        }
    }

    fun filterSongs(showLikedOnly: Boolean) {
        this.showLikedOnly = showLikedOnly
        applyFilter(songs.value)
    }

    private fun applyFilter(songs: List<Song>?) {
        _filteredSongs.value = if (showLikedOnly) {
            songs?.filter {
                it.isLiked && (
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true)
                )
            }
        } else {
            songs?.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true)
            }
        } ?: emptyList()
    }

    fun searchSongs(query: String) {
        _filteredSongs.value = songs.value?.filter { song ->
            ((showLikedOnly && song.isLiked) || !showLikedOnly) && (
                song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true)
            )
        }
        searchQuery = query
    }
}