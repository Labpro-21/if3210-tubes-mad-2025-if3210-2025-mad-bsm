package com.mad.besokminggu.manager

import android.net.Uri
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.mad.besokminggu.data.model.Song
import java.io.File
import androidx.core.net.toUri

/**
 * Class used to transport the Command from ViewModel to The Notification controls
 */
object AudioPlayerManager {
    // Injected later, used as control for media playback
    var controller : MediaController? = null

    // Injected later, used as a business layer for media playing logic
    private lateinit var queue: PlaybackQueueManager

    fun init(mc: MediaController, queueManager: PlaybackQueueManager) {
        controller = mc
        queue = queueManager
    }

    /** Append song to queue, (doesn't update the queue of the controller)*/
    suspend fun addToQueue(song: Song) {
        queue.addToQueue(song)
    }

    /**
     * Play the Song
     *
     */
    suspend fun play(song: Song, isOnline: Boolean = false, onComplete: (() -> Unit)? = null) {
        stop()
        queue.playInitial(song, isOnline)
        playCurrent(song, isOnline)

        onComplete?.invoke()
    }

    /** Advance the queue via the queue manager, then transport the result */
    suspend fun skipToNext() {
        queue.skipNext()?.let { next ->
            playCurrent(next, queue.isOnline)
        }
    }

    /** Go back via the queue manager, then transport */
    fun skipToPrevious() {
        queue.skipPrevious()?.let { prev ->
            playCurrent(prev, queue.isOnline)
        }
    }

    /** Flip the shuffle flag (affects future skips) */
    fun toggleShuffle() {
        queue.toggleShuffle()
    }

    /** Cycle repeat mode (affects future skips) */
    fun toggleRepeat() {
        queue.toggleRepeat()
    }

    fun pause() {
        controller?.pause()
    }

    fun resume() {
        controller?.play()
    }

    fun stop() {
        controller?.stop()
    }

    fun togglePlayPause(){
        queue.updatePlayPause(!queue.isPlaying.value)
        if(queue.isPlaying.value){
            resume()
        }else{
            pause()
        }
    }

    fun isPlaying(): Boolean = controller?.isPlaying == true

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        queue.updateSeekPosition(positionMs)
    }

    fun getCurrentPosition(): Long = controller?.currentPosition ?: 0L

    fun getDuration(): Long = controller?.duration ?: 0L

    /**
     * Helper method to play the Song
     */
    private fun playCurrent(song: Song, isOnline: Boolean) {
        val metadata = extractSongMetadata(song, isOnline)
        val item = extractMediaItem(song, isOnline, metadata)

        controller?.apply {
            setMediaItem(item)
            prepare()
            play()
        }

    }

    /**
     * Helper method to get the song metadata
     */
    private fun extractSongMetadata(song : Song, isOnline : Boolean) : MediaMetadata {
        val imageUri: Uri = if(isOnline){
            song.coverFileName.toUri()
        }else{
            val file: File = CoverFileHelper.getFile(song.coverFileName)
                ?: throw IllegalArgumentException("No local file for ${song.coverFileName}")
            Uri.fromFile(file)
        }
        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setArtworkUri(imageUri)
            .build()

        return metadata
    }


    /**
     * Extract the media item based on thee media metadata
     */
    private fun extractMediaItem(song : Song, isOnline : Boolean, metadata : MediaMetadata) : MediaItem{
        val audioUri: Uri = if (isOnline) {
            song.audioFileName.toUri()
        } else {
            val file: File = AudioFileHelper.getFile(song.audioFileName)
                ?: throw IllegalArgumentException("No local file for ${song.audioFileName}")
            Uri.fromFile(file)
        }
        val item = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setMediaMetadata(metadata)
            .setRequestMetadata(
                MediaItem.RequestMetadata.Builder()
                    .setMediaUri(audioUri)
                    .build()
            )
            .build()
        return item
    }

}