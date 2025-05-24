package com.mad.besokminggu.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.repositories.OnlineSongRepository
import com.mad.besokminggu.data.repositories.SongRepository
import com.mad.besokminggu.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject

enum class RepeatMode {
    NONE,
    REPEAT_ONE,
    REPEAT_ALL
}


@HiltViewModel
class SongTracksViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val onlineSongRepository: OnlineSongRepository,
    private val tokenManager: SessionManager
) : ViewModel() {

    private val ownerId = tokenManager.getUserProfile()?.id ?: -1

    private val _playedSong = MutableLiveData<Song?>()
    val playedSong: LiveData<Song?> get() = _playedSong

    val _isOnlineSong = MutableLiveData<Boolean>(false)
    val isOnlineSong: LiveData<Boolean> get() = _isOnlineSong

    // Keep tracking of previous queue
    private val _previousSongsQueue = MutableLiveData<List<Song>>(emptyList())
    val previousSongsQueue: LiveData<List<Song>> get() = _previousSongsQueue

    // Keep tracking of next queue
    private val _nextSongsQueue = MutableLiveData<List<Song>>(emptyList())

    // Keep trakcing of playing/pausing
    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    // Keep track of the current time of the song has been passed
    private val _currentSeekPosition = MutableLiveData<Int>(0)
    val currentSeekPosition: LiveData<Int> get() = _currentSeekPosition

    // an event trigger for full Player fragment shown
    private val _isFullPlayerVisible = MutableLiveData(false)
    val isFullPlayerVisible: LiveData<Boolean> get() = _isFullPlayerVisible

    // This save the max duration of a song and live as a trigger event
    private val _currentSongDuration = MutableLiveData<Int>()
    val currentSongDuration: LiveData<Int> get() = _currentSongDuration

    private val _isLiked = MutableLiveData<Boolean>(false)
    val isLiked : LiveData<Boolean> get() = _isLiked;

    private val _repeatMode = MutableLiveData(RepeatMode.NONE)
    val repeatMode: LiveData<RepeatMode> get() = _repeatMode

    private val _isShuffle = MutableLiveData(false)
    val isShuffle: LiveData<Boolean> get() = _isShuffle

    // Trigger for if there any song was deleted
    private val _anySongDeleted = MutableLiveData<Song>()
    val anySongDeleted : LiveData<Song> get() = _anySongDeleted

    fun updateSongDuration(duration: Int) {
        _currentSongDuration.value = duration
    }

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
        var newVal = false;
        if(_isPlaying.value != null)
            newVal = !_isPlaying.value!!

        _isPlaying.value = newVal
    }

    fun updatePlayPause(newVal : Boolean){
        _isPlaying.value = newVal
    }


    suspend fun playSong(song: Song, isOnline: Boolean = false){
        val newSong = song.copy(lastPlayedAt =  Date())
        _isOnlineSong.value = isOnline
        Log.d("MiniPlayer", "Online?: ${_isOnlineSong.value}")
        _playedSong.value = newSong
        Log.d("MiniPlayer", "Song playing: ${_playedSong.value}")
        if (isOnline) {
            songRepository.update(newSong);
            resetPrevQueue();
        }
        showFullPlayer();
    }


    suspend fun skipToNext() {
        val currentSong : Song = _playedSong.value?.copy(lastPlayedAt =  Date()) ?: return
        val isOnline = isOnlineSong.value ?: false
        if (!isOnline) {
            songRepository.update(currentSong);
        }
        when(_repeatMode.value){
            RepeatMode.REPEAT_ONE -> {
                _playedSong.value = currentSong.copy(lastPlayedAt = Date())
                return
            }
            RepeatMode.REPEAT_ALL -> {
                val nextQueue = _nextSongsQueue.value?.toMutableList() ?: return
                if(nextQueue.isNotEmpty()){
                    // exist a queue
                    handleNextSongFromQueue(currentSong);
                    addToNextQueue(currentSong);
                }else{
                    // Only single song played, do the same as repeat_one
                    _playedSong.value = currentSong.copy(lastPlayedAt = Date())
                }
                return;
            }
            else -> {
                handleNextSongFromQueue(currentSong)
            }
        }

    }



    suspend fun handleNextSongFromQueue(currentSong : Song){
        val nextQueue = _nextSongsQueue.value?.toMutableList() ?: return

        val nextSong = if (_isOnlineSong.value ?: false) {
            if (_isShuffle.value == true) {
                onlineSongRepository.getNextRandomSong(currentSong)
            } else {
                onlineSongRepository.getNextIteratedSong(currentSong)
            }
        } else {
            if (nextQueue.isNotEmpty()) {
                // Handle shuffle
                if (_isShuffle.value == true) {
                    nextQueue.removeAt((nextQueue.indices).random())
                } else {
                    nextQueue.removeAt(0)
                }
            } else {
                if (_isShuffle.value == true) {
                    songRepository.getNextRandomSong(currentSong, ownerId)
                } else {
                    songRepository.getNextIteratedSong(currentSong, ownerId)
                }
            }
        }

        addToPrevQueue(currentSong)
        _playedSong.value = nextSong
        _nextSongsQueue.value = nextQueue
        if (isOnlineSong.value == false) {
            songRepository.update(currentSong)
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


    suspend fun deleteSong(song : Song){
        if(song.id == _playedSong.value?.id){
            resetPlayback()
        }
        if (isOnlineSong.value == false)
            songRepository.deleteSong(song)
        _anySongDeleted.value = song;
    }


    suspend fun addToNextQueue(song: Song) {
        val updatedQueue = _nextSongsQueue.value?.toMutableList() ?: mutableListOf()
        updatedQueue.add(song)

        Log.d("NextQueue", "Size of Queue: ${updatedQueue.size}")

        if (updatedQueue.size == 1 && _playedSong.value == null) {
            playSong(song, isOnlineSong.value ?: false)
        }

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
        _isFullPlayerVisible.value = false
        _playedSong.value = null
        _currentSeekPosition.value = 0

    }

    fun resetPrevQueue(){
        _previousSongsQueue.value = emptyList()
    }

    fun isAnySongPlayed() : Boolean{
        return _playedSong.value != null;
    }

    fun updateIsLike(like : Boolean){
        _isLiked.value = like
    }

    suspend fun likeSong() {
        val current = _playedSong.value ?: return
        val liked = _isLiked.value ?: false
        val updated = !liked
        val updatedSong = current.copy(isLiked = updated)

        _isLiked.value = updated
        if (isOnlineSong.value == false)
            songRepository.update(updatedSong)
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.NONE
            else -> RepeatMode.NONE
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !(_isShuffle.value ?: false)
    }


}