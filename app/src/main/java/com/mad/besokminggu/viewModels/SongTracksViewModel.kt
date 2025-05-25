package com.mad.besokminggu.viewModels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.repositories.OnlineSongRepository
import com.mad.besokminggu.data.repositories.SongRepository
import com.mad.besokminggu.manager.AudioPlayerManager
import com.mad.besokminggu.manager.PlaybackQueueManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
    private val queueManager: PlaybackQueueManager
) : ViewModel() {

    val playedSong: LiveData<Song?> =
        queueManager.currentSong
            .asLiveData()

    val _isOnlineSong = MutableLiveData<Boolean>(false)
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
    val isLiked : LiveData<Boolean> get() = _isLiked


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
        AudioPlayerManager.togglePlayPause()
        syncWithQueueManager()
    }

    suspend fun playSong(song: Song, isOnline: Boolean = false){

        AudioPlayerManager.play(song,isOnline);
        showFullPlayer();
        syncWithQueueManager();
    }


    suspend fun skipToNext() {
        AudioPlayerManager.skipToNext()
        syncWithQueueManager()
    }


    fun skipToPrevious() {

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
        _anySongDeleted.value = song
    }



    suspend fun addToNextQueue(song: Song) {
        AudioPlayerManager.addToQueue(song)
        syncWithQueueManager()
    }


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
        AudioPlayerManager.toggleRepeat()
    }

    fun toggleShuffle() {
        AudioPlayerManager.toggleShuffle()
    }

    private fun syncWithQueueManager(){
        _isOnlineSong.value = queueManager.isOnline
        _previousSongsQueue.value = queueManager.prevQueue
    }

    fun incrementSongPlayedTime(songId: Int, seconds: Int, lastPlayedAt: Date) {
        viewModelScope.launch {
            songRepository.incrementPlayedTime(songId,seconds,lastPlayedAt)
        }
    }
}