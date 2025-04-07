package com.mad.besokminggu.audio

import android.content.Context
import android.media.MediaPlayer
import com.mad.besokminggu.data.model.Song

object AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null

    fun play(context: Context, song: Song, onComplete : (() -> Unit)? = null) {
        stop()
        if(song.filePath == ""){
            return;
        }
        mediaPlayer = MediaPlayer().apply {
            setDataSource(song.filePath)
            setOnPreparedListener {
                start()
            }
            setOnCompletionListener {
                // You could auto-skip or update UI here
                onComplete?.invoke();
            }
            prepareAsync()
        }
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        mediaPlayer?.start()
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }
}
