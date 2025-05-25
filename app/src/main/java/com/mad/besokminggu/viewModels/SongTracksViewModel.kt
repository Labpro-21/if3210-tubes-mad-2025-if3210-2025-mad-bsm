package com.mad.besokminggu.viewModels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.repositories.OnlineSongRepository
import com.mad.besokminggu.data.repositories.SongRepository
import com.mad.besokminggu.manager.AudioPlayerManager
import com.mad.besokminggu.manager.PlaybackQueueManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val queueManager: PlaybackQueueManager
) : ViewModel() {

    val playedSong: LiveData<Song?> =
        queueManager.currentSong
            .asLiveData()

    private val _isOnlineSong = MutableLiveData<Boolean>(false)
    val isOnlineSong: LiveData<Boolean> get() = _isOnlineSong

    // Keep tracking of previous queue
    private val _previousSongsQueue = MutableLiveData<List<Song>>(emptyList())
    val previousSongsQueue: LiveData<List<Song>> get() = _previousSongsQueue

    // Keep tracking of next queue
//    private val _nextSongsQueue = MutableLiveData<List<Song>>(emptyList())

    // Keep trakcing of playing/pausing
    val isPlaying: LiveData<Boolean> = queueManager.isPlaying.asLiveData()

    // Keep track of the current time of the song has been passed

    val currentSeekPosition: LiveData<Long> = queueManager.currentSeekPosition.asLiveData()

    // an event trigger for full Player fragment shown
    private val _isFullPlayerVisible = MutableLiveData(false)
    val isFullPlayerVisible: LiveData<Boolean> get() = _isFullPlayerVisible

    // This save the max duration of a song and live as a trigger event

    val currentSongDuration: LiveData<Long> = queueManager.currentDuration.asLiveData();

    private val _isLiked = MutableLiveData<Boolean>(false)
    val isLiked : LiveData<Boolean> get() = _isLiked;


    val repeatMode: LiveData<RepeatMode> = queueManager.repeatMode.asLiveData()

    val isShuffle: LiveData<Boolean> = queueManager.isShuffle.asLiveData()

    // Trigger for if there any song was deleted
    private val _anySongDeleted = MutableLiveData<Song>()
    val anySongDeleted : LiveData<Song> get() = _anySongDeleted


    val warningText : LiveData<String> = queueManager.textWarnings.asLiveData()

    fun showFullPlayer() {
        _isFullPlayerVisible.value = true
    }

    fun hideFullPlayer() {
        _isFullPlayerVisible.value = false
    }


    fun updateSeekPosition(position: Long) {
//        _currentSeekPosition.value = position
        queueManager.updateSeekPosition(position);
    }

    fun updateSeekDuration(position: Long){
        queueManager.updateSeekDuration(position);
    }

    fun togglePlayPause() {
//        var newVal = false;
//        if(_isPlaying.value != null)
//            newVal = !_isPlaying.value!!
//
//        _isPlaying.value = newVal
        AudioPlayerManager.togglePlayPause()
        syncWithQueueManager()
    }

//    fun updatePlayPause(newVal : Boolean){
//        _isPlaying.value = newVal
//    }


    suspend fun playSong(song: Song, isOnline: Boolean = false){
//        val newSong = song.copy(lastPlayedAt =  Date())
//        _isOnlineSong.value = isOnline
//        Log.d("MiniPlayer", "Online?: ${_isOnlineSong.value}")
//        _playedSong.value = newSong
//        Log.d("MiniPlayer", "Song playing: ${_playedSong.value}")
//        if (isOnline) {
//            songRepository.update(newSong);
//            resetPrevQueue();
//        }
        AudioPlayerManager.play(song,isOnline);
        showFullPlayer();
        syncWithQueueManager();
    }


    suspend fun skipToNext() {
//        val currentSong : Song = _playedSong.value?.copy(lastPlayedAt =  Date()) ?: return
//        val isOnline = isOnlineSong.value ?: false
//        if (!isOnline) {
//            songRepository.update(currentSong);
//        }
//        when(_repeatMode.value){
//            RepeatMode.REPEAT_ONE -> {
//                _playedSong.value = currentSong.copy(lastPlayedAt = Date())
//                return
//            }
//            RepeatMode.REPEAT_ALL -> {
//                val nextQueue = _nextSongsQueue.value?.toMutableList() ?: return
//                if(nextQueue.isNotEmpty()){
//                    // exist a queue
//                    handleNextSongFromQueue(currentSong);
//                    addToNextQueue(currentSong);
//                }else{
//                    // Only single song played, do the same as repeat_one
//                    _playedSong.value = currentSong.copy(lastPlayedAt = Date())
//                }
//                return;
//            }
//            else -> {
//                handleNextSongFromQueue(currentSong)
//            }
//        }
        AudioPlayerManager.skipToNext()
        syncWithQueueManager()
    }



//    suspend fun handleNextSongFromQueue(currentSong : Song){
//        val nextQueue = _nextSongsQueue.value?.toMutableList() ?: return
//
//        val nextSong = if (_isOnlineSong.value ?: false) {
//            if (_isShuffle.value == true) {
//                onlineSongRepository.getNextRandomSong(currentSong)
//            } else {
//                onlineSongRepository.getNextIteratedSong(currentSong)
//            }
//        } else {
//            if (nextQueue.isNotEmpty()) {
//                // Handle shuffle
//                if (_isShuffle.value == true) {
//                    nextQueue.removeAt((nextQueue.indices).random())
//                } else {
//                    nextQueue.removeAt(0)
//                }
//            } else {
//                if (_isShuffle.value == true) {
//                    songRepository.getNextRandomSong(currentSong, ownerId)
//                } else {
//                    songRepository.getNextIteratedSong(currentSong, ownerId)
//                }
//            }
//        }
//
//        addToPrevQueue(currentSong)
//        _playedSong.value = nextSong
//        _nextSongsQueue.value = nextQueue
//        if (isOnlineSong.value == false) {
//            songRepository.update(currentSong)
//        }
//}

    fun skipToPrevious() {
//        val prevQueue = _previousSongsQueue.value?.toMutableList() ?: return
//        val currentSong = _playedSong.value ?: return
//        if (prevQueue.isNotEmpty()) {
//            val lastIndex = prevQueue.lastIndex
//            val prevSong = prevQueue.removeAt(lastIndex)
//            val nextQueue = _nextSongsQueue.value?.toMutableList() ?: mutableListOf()
//            nextQueue.add(0, currentSong)
//            _playedSong.value = prevSong
//            _previousSongsQueue.value = prevQueue
//            _nextSongsQueue.value = nextQueue
//        }
        AudioPlayerManager.skipToPrevious()
        syncWithQueueManager()
    }


    suspend fun deleteSong(song : Song){

        if(song.id == playedSong.value?.id){
            queueManager.updateWarningText("Cannot delete the Played Song")
            return;
        }
        if (isOnlineSong.value == false)
            songRepository.deleteSong(song)
        _anySongDeleted.value = song;
    }



    suspend fun addToNextQueue(song: Song) {
//        val updatedQueue = _nextSongsQueue.value?.toMutableList() ?: mutableListOf()
//        updatedQueue.add(song)
//
//        Log.d("NextQueue", "Size of Queue: ${updatedQueue.size}")
//
//        if (updatedQueue.size == 1 && _playedSong.value == null) {
//            playSong(song, isOnlineSong.value ?: false)
//        }
//
        //_nextSongsQueue.value = updatedQueue
        AudioPlayerManager.addToQueue(song)
        syncWithQueueManager()
    }


//    private fun addToPrevQueue(song : Song){
//        val prevQueue = _previousSongsQueue.value?.toMutableList() ?: mutableListOf()
//        prevQueue.add(song)
//        _previousSongsQueue.value = prevQueue
//    }


//    fun resetPrevQueue(){
//        _previousSongsQueue.value = emptyList()
//    }

    fun isAnySongPlayed() : Boolean{
        return playedSong.value != null;
    }

    fun updateIsLike(like : Boolean){
        _isLiked.value = like
    }

    suspend fun likeSong() {
        val current = playedSong.value ?: return
        val liked = _isLiked.value ?: false
        val updated = !liked
        val updatedSong = current.copy(isLiked = updated)

        _isLiked.value = updated
        if (isOnlineSong.value == false)
            songRepository.update(updatedSong)
    }

    fun toggleRepeat() {
//        _repeatMode.value = when (_repeatMode.value) {
//            RepeatMode.NONE -> RepeatMode.REPEAT_ONE
//            RepeatMode.REPEAT_ONE -> RepeatMode.REPEAT_ALL
//            RepeatMode.REPEAT_ALL -> RepeatMode.NONE
//            else -> RepeatMode.NONE
//        }
        AudioPlayerManager.toggleRepeat()
    }

    fun toggleShuffle() {
//        _isShuffle.value = !(_isShuffle.value ?: false)
        AudioPlayerManager.toggleShuffle()
    }

    private fun syncWithQueueManager(){
        _isOnlineSong.value = queueManager.isOnline
        _previousSongsQueue.value = queueManager.prevQueue
    }


}