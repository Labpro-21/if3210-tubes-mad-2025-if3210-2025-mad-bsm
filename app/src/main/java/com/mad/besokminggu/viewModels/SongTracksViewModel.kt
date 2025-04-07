package com.mad.besokminggu.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.repositories.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SongTracksViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {
    private val _playedSong = MutableLiveData<Song>()
    val playedSong: LiveData<Song> get() = _playedSong

    private val _previousSongsQueue = MutableLiveData<List<Song>>(emptyList())
    val previousSongsQueue: LiveData<List<Song>> get() = _previousSongsQueue

    private val _nextSongsQueue = MutableLiveData<List<Song>>(emptyList())
    val nextSongsQueue: LiveData<List<Song>> get() = _nextSongsQueue

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _currentSeekPosition = MutableLiveData<Int>(0)
    val currentSeekPosition: LiveData<Int> get() = _currentSeekPosition

    private val _isFullPlayerVisible = MutableLiveData(false)
    val isFullPlayerVisible: LiveData<Boolean> get() = _isFullPlayerVisible

    fun showFullPlayer() {
        _isFullPlayerVisible.value = true
    }

    fun hideFullPlayer() {
        _isFullPlayerVisible.value = false
    }


    fun updateSeekPosition(position: Int) {
        _currentSeekPosition.value = position
    }

    fun togglePlayPause() {
        _isPlaying.value = _isPlaying.value?.not() != false
    }


    fun playSong(song: Song){
        _playedSong.value = song;

    }

    suspend fun skipToNext() {
        val nextQueue = _nextSongsQueue.value?.toMutableList() ?: return
        val currentSong = _playedSong.value ?: return
        if (nextQueue.isNotEmpty()) {
            val nextSong = nextQueue.removeAt(0)
            val prevQueue = _previousSongsQueue.value?.toMutableList() ?: mutableListOf()
            prevQueue.add(currentSong)
            _previousSongsQueue.value = prevQueue
            _playedSong.value = nextSong
            _nextSongsQueue.value = nextQueue
        }else{
            addEmptyNextQueue(currentSong);
            val prevQueue = _previousSongsQueue.value?.toMutableList() ?: mutableListOf()
            prevQueue.add(currentSong)
            _previousSongsQueue.value = prevQueue
        }
    }

    fun skipToPrevious() {
        val prevQueue = _previousSongsQueue.value?.toMutableList() ?: return
        val currentSong = _playedSong.value ?: return
        if (prevQueue.isNotEmpty()) {
            val lastIndex = prevQueue.lastIndex
            val prevSong = prevQueue.removeAt(lastIndex)
            val nextQueue = _nextSongsQueue.value?.toMutableList() ?: mutableListOf()
            nextQueue.add(0, currentSong)
            _playedSong.value = prevSong
            _previousSongsQueue.value = prevQueue
            _nextSongsQueue.value = nextQueue
        }
    }

    suspend fun addEmptyNextQueue(currentSong : Song){
        _playedSong.value = songRepository.getNextIteratedSong(currentSong);
    }


    fun addToNextQueue(song: Song) {
        val updatedQueue = _nextSongsQueue.value?.toMutableList() ?: mutableListOf()
        updatedQueue.add(song)
        _nextSongsQueue.value = updatedQueue
    }

    private fun addToPrevQueue(song : Song){
        val prevQueue = _previousSongsQueue.value?.toMutableList() ?: mutableListOf()
        prevQueue.add(song)
        _previousSongsQueue.value = prevQueue
    }

    fun resetPlayback() {

        _previousSongsQueue.value = emptyList()
        _nextSongsQueue.value = emptyList()
        _isPlaying.value = false
    }

}