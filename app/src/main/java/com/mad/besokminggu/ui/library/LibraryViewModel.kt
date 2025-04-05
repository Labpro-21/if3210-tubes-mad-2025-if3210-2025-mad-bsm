package com.mad.besokminggu.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song

class LibraryViewModel : ViewModel() {

    private val _songs = MutableLiveData<List<Song>>().apply {
        value = listOf(
            Song("Starboy", "The Weeknd, Daft Punk", R.drawable.cover_starboy),
            Song("Here Comes The Sun", "The Beatles", R.drawable.cover_starboy),
            Song("Midnight Pretenders", "Tomoko Aran", R.drawable.cover_starboy),
            Song("Violent Crimes", "Kanye West", R.drawable.cover_starboy),
            Song("DENIAL IS A RIVER", "Doechii", R.drawable.cover_starboy),
            Song("Doomsday", "MF DOOM, Pebbles The Invisible Girl", R.drawable.cover_starboy)
        )
    }

    val songs: LiveData<List<Song>> = _songs
}