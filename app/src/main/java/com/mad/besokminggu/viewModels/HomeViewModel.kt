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
import com.mad.besokminggu.data.model.Song
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

    fun refreshSong(ownerId: Int) {
        profile = tokenManager.getUserProfile()
        allSongs = songRepository.getAllSongs(ownerId)
    }

}