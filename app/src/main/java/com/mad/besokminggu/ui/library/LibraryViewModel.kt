package com.mad.besokminggu.ui.library

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _filteredSongs = MutableLiveData<List<Song>>()
    val filteredSongs: LiveData<List<Song>> = _filteredSongs

    fun insertSong(song: Song) {
        viewModelScope.launch {
            repository.insert(song)
        }
    }

    fun filterSongs(showLikedOnly: Boolean) {
        _filteredSongs.value = if (showLikedOnly) {
            songs.value?.filter { it.isLiked }
        } else {
            songs.value
        }
    }

    init {
        songs.observeForever {
            filterSongs(showLikedOnly = false)
        }
    }

}