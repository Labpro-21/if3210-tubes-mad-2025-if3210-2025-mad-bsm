package com.mad.besokminggu.ui.profile

import android.se.omapi.Session
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.besokminggu.data.repositories.SongRepository
import com.mad.besokminggu.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: SongRepository,
    private val tokenManager: SessionManager
) : ViewModel() {

    val _likedSongsCount = MutableLiveData<Int>()
    var likedSongsCount: LiveData<Int> = _likedSongsCount

    val _songsCount = MutableLiveData<Int>()
    var songsCount: LiveData<Int> = _songsCount

    val _listenedSongsCount = MutableLiveData<Int>()
    var listenedSongsCount: LiveData<Int> = _listenedSongsCount

    fun loadCounts() {
        viewModelScope.launch {
            val profile = tokenManager.getUserProfile()

            // Observe each count and update the corresponding MutableLiveData
            repository.getLikedSongsCount(profile?.id ?: -1).observeForever { count ->
                _likedSongsCount.postValue(count)
            }

            repository.getTotalSongsCount(profile?.id ?: -1).observeForever { count ->
                _songsCount.postValue(count)
            }

            repository.getListenedSongsCount(profile?.id ?: -1).observeForever { count ->
                _listenedSongsCount.postValue(count)
            }
        }
    }
}