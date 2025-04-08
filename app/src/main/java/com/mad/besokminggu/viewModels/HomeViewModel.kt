package com.mad.besokminggu.viewModels

import android.util.Log
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

}