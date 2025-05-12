package com.mad.besokminggu.ui.topSongs;

import androidx.lifecycle.MutableLiveData
import com.mad.besokminggu.data.model.OnlineSong
import com.mad.besokminggu.data.model.toSong
import com.mad.besokminggu.data.repositories.OnlineSongRepository
import com.mad.besokminggu.data.repositories.UnprotectedRepository
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.viewModels.BaseViewModel
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class TopSongsViewModel @Inject constructor(
    private val unprotectedRepository: UnprotectedRepository,
    private val songRepository: OnlineSongRepository,
): BaseViewModel() {

    private val _topSongs = MutableLiveData<ApiResponse<List<OnlineSong>>>()
    val topSongs: MutableLiveData<ApiResponse<List<OnlineSong>>>
        get() = _topSongs

    private val _totalDuration = MutableLiveData<Int>()
    val totalDuration: MutableLiveData<Int>
        get() = _totalDuration

    fun updateSongsRepo(songs: List<OnlineSong>) {
        val mappedSongs = songs.map { it.toSong() }
        songRepository.updateAllSongs(mappedSongs)
    }

    fun updateTotalDuration() {
        when (val duration = _topSongs.value) {
            is ApiResponse.Success -> {
                val total = duration.data.sumOf { parseDuration(it.duration) }
                _totalDuration.value = total
            }
            is ApiResponse.Failure -> {
                _totalDuration.value = 0
            }
            is ApiResponse.Loading -> {
                _totalDuration.value = 0
            }

            null -> _totalDuration.value = 0
        }
    }

    fun getTopSongsGlobal(coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(
        _topSongs,
        coroutinesErrorHandler
    ) {
        unprotectedRepository.topSongsGlobal()
    }

    fun getTopSongsCountry(country: String, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(
        _topSongs,
        coroutinesErrorHandler
    ) {
        unprotectedRepository.topSongsCountry(country)
    }

    private fun parseDuration(duration: String): Int {
        val parts = duration.split(":")
        return if (parts.size == 2) {
            val minutes = parts[0].toIntOrNull() ?: 0
            val seconds = parts[1].toIntOrNull() ?: 0
            (minutes * 60) + seconds
        } else {
            0
        }
    }
}
