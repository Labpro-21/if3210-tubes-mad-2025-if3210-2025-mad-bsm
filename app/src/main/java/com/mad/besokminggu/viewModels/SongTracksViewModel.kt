package com.mad.besokminggu.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.repositories.SongRepository
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
    private val songRepository: SongRepository
) : ViewModel() {

    private val _playedSong = MutableLiveData<Song?>()
    val playedSong: LiveData<Song?> get() = _playedSong

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

    private val _currentSongDuration = MutableLiveData<Int>()
    val currentSongDuration: LiveData<Int> get() = _currentSongDuration

    private val _isLiked = MutableLiveData<Boolean>(false)
    val isLiked : LiveData<Boolean> get() = _isLiked;

    private val _repeatMode = MutableLiveData(RepeatMode.NONE)
    val repeatMode: LiveData<RepeatMode> get() = _repeatMode

    private val _isShuffle = MutableLiveData(false)
    val isShuffle: LiveData<Boolean> get() = _isShuffle

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


    fun playSong(song: Song){
        val newSong = song.copy(lastPlayedAt =  Date())
        _playedSong.value = newSong;
    }

    suspend fun skipToNext() {
        val nextQueue = _nextSongsQueue.value?.toMutableList() ?: return
        val currentSong = _playedSong.value?.copy(lastPlayedAt =  Date()) ?: return

        handleRepeatOne(nextQueue, currentSong);


        if (nextQueue.isNotEmpty()) {
            val nextSong = nextQueue.removeAt(0)
            addToPrevQueue(currentSong)
            _playedSong.value = nextSong
            _nextSongsQueue.value = nextQueue
        }else{
            addEmptyNextQueue(currentSong);
            val prevQueue = _previousSongsQueue.value?.toMutableList() ?: mutableListOf()
            prevQueue.add(currentSong)
            _previousSongsQueue.value = prevQueue
        }

        songRepository.update(currentSong)
    }



    private suspend fun handleRepeatOne(nextQueue : MutableList<Song>, currentSong : Song){
        if (nextQueue.isNotEmpty()) {
            val nextSong = if (_isShuffle.value == true) {
                nextQueue.removeAt((0 until nextQueue.size).random())
            } else {
                nextQueue.removeAt(0)
            }

            val prevQueue = _previousSongsQueue.value?.toMutableList() ?: mutableListOf()
            prevQueue.add(currentSong)

            _playedSong.value = nextSong
            _previousSongsQueue.value = prevQueue
            _nextSongsQueue.value = nextQueue

        }
    }

    private suspend fun handleRepeatAll(currentSong : Song){
        if (_repeatMode.value == RepeatMode.REPEAT_ALL) {
            val allSongs = songRepository.allSongs.value.orEmpty()
            val restartQueue = allSongs.filterNot { it.id == currentSong.id }

            val nextSong = if (_isShuffle.value == true) {
                restartQueue.random()
            } else {
                restartQueue.firstOrNull()
            }

            if (nextSong != null) {
                _playedSong.value = nextSong
                _previousSongsQueue.value = mutableListOf(currentSong)
                _nextSongsQueue.value = restartQueue.filterNot { it.id == nextSong.id }
            }
        } else {
            addEmptyNextQueue(currentSong)
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

    suspend fun deleteSong(song : Song){
        songRepository.deleteSong(song)
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