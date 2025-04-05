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
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: SongRepository
) : ViewModel() {

    val songs: LiveData<List<Song>> = repository.allSongs

    fun insertDummySongs() {
        val dummySongs = listOf(
            Song(title = "Starboy", artist = "The Weeknd, Daft Punk", coverResId = R.drawable.cover_blonde),
            Song(title = "Here Comes The Sun", artist = "The Beatles", coverResId = R.drawable.cover_blonde),
            Song(title = "Midnight Pretenders", artist = "Tomoko Aran", coverResId = R.drawable.cover_blonde),
            Song(title = "Violent Crimes", artist = "Kanye West", coverResId = R.drawable.cover_blonde),
            Song(title = "DENIAL IS A RIVER", artist = "Doechii", coverResId = R.drawable.cover_blonde),
            Song(title = "Doomsday", artist = "MF DOOM, Pebbles The Invisible Girl", coverResId = R.drawable.cover_blonde)
        )

        viewModelScope.launch {
            repository.insertDummySongsIfEmpty(dummySongs)
        }
    }
}