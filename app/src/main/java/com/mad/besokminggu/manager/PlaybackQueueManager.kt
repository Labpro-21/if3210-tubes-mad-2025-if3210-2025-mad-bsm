package com.mad.besokminggu.manager
import androidx.lifecycle.MutableLiveData
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.repositories.OnlineSongRepository
import com.mad.besokminggu.data.repositories.SongRepository
import com.mad.besokminggu.network.SessionManager
import com.mad.besokminggu.viewModels.RepeatMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackQueueManager @Inject constructor(
    private val songRepository: SongRepository,
    private val onlineSongRepository: OnlineSongRepository,
    private val tokenManager: SessionManager
) {
    private val ownerId = tokenManager.getUserProfile()?.id ?: -1

    private val _currentSong = MutableStateFlow<Song?>(null)
    private val _isShuffle   = MutableStateFlow(false)
    private val _repeatMode  = MutableStateFlow(RepeatMode.NONE)
    private val _isPlaying = MutableStateFlow(true);
    private val _currentSeekPosition = MutableStateFlow<Long>(0);
    private val _currentDurationPosition = MutableStateFlow<Long>(0);
    private val _textWarning = MutableStateFlow<String>("")


    val prevQueue = mutableListOf<Song>()
    private val nextQueue = mutableListOf<Song>()

    var isOnline = false

    val currentSong: StateFlow<Song?>      = _currentSong
    val isShuffle:   StateFlow<Boolean>    = _isShuffle
    val repeatMode:  StateFlow<RepeatMode> = _repeatMode
    val currentSeekPosition : StateFlow<Long> = _currentSeekPosition;
    val currentDuration : StateFlow<Long> = _currentDurationPosition;
    val textWarnings : StateFlow<String> = _textWarning

    var isPlaying : StateFlow<Boolean> = _isPlaying;

    suspend fun playInitial(song: Song, online: Boolean) {
        prevQueue.clear()
        nextQueue.clear()
        val newSong = song.copy(lastPlayedAt = Date())
        _currentSong.value = newSong
        isOnline = online
        _isPlaying.value = true;

        if(isOnline){
            songRepository.update(newSong);
        }
    }

    fun updatePlayPause(boolean: Boolean){
        _isPlaying.value = boolean
    }

    fun updateSeekDuration(position : Long){
        _currentDurationPosition.value = position
    }

    fun updateSeekPosition(position : Long){
        _currentSeekPosition.value = position
    }

    suspend fun skipNext(): Song? {
        val curSong = _currentSong.value?.copy(lastPlayedAt = Date()) ?: return null

        if(!isOnline){
            songRepository.update(curSong)
        }

        when (_repeatMode.value) {
            RepeatMode.REPEAT_ONE -> {
                _currentSong.value = curSong.copy(lastPlayedAt = Date())
            }
            RepeatMode.REPEAT_ALL -> {
                if (nextQueue.isEmpty()) {
                    // Do the same like Repeat One
                    _currentSong.value = curSong.copy(lastPlayedAt = Date())
                }
                else{
                    handleNextSongFromQueue(curSong)
                    addToQueue(curSong)
                }
            }
            RepeatMode.NONE -> {
                handleNextSongFromQueue(curSong)
            }
        }

        return _currentSong.value
    }

    fun updateWarningText(text: String){
        _textWarning.value= text;
    }

    fun disconnectedThroughMedia(){
        _textWarning.value= "Error playing media, changing to speaker"
    }


    suspend fun handleNextSongFromQueue(curSong : Song) {
        val nextSong = if (isOnline) {
            if (_isShuffle.value) {
                onlineSongRepository.getNextRandomSong(curSong)
            } else {
                onlineSongRepository.getNextIteratedSong(curSong)
            }
        } else {
            if (nextQueue.isNotEmpty()) {
                // Handle shuffle
                if (_isShuffle.value) {
                    nextQueue.removeAt((nextQueue.indices).random())
                } else {
                    nextQueue.removeAt(0)
                }
            } else {
                if (_isShuffle.value) {
                    songRepository.getNextRandomSong(curSong, ownerId)
                } else {
                    songRepository.getNextIteratedSong(curSong, ownerId)
                }
            }


        }

        addToPrevQueue(nextSong)
        _currentSong.value = nextSong
        if(!isOnline){
            songRepository.update(curSong)
        }
    }

    fun skipPrevious(): Song? {
        val cur = _currentSong.value ?: return null
        if (prevQueue.isEmpty()) return null
        nextQueue.add(0, cur)
        _currentSong.value = prevQueue.removeAt(prevQueue.lastIndex)
        return _currentSong.value
    }

    fun addToQueue(songs: List<Song>) {
        nextQueue += songs
    }

    fun addToPrevQueue(song : Song){
        prevQueue += song
    }

    suspend fun addToQueue(song: Song) {
        nextQueue += song
        if(nextQueue.size == 1 && _currentSong.value == null){
            playInitial(song, isOnline);
        }
    }

    fun toggleShuffle() { _isShuffle.value = !_isShuffle.value }
    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.NONE      -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE-> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL-> RepeatMode.NONE
        }
    }

    fun resetPlayback(){
        _currentSong.value = null;
    }
}
