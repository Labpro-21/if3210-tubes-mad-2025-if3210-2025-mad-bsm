package com.mad.besokminggu.manager

import android.content.Context
import android.media.MediaPlayer
import com.mad.besokminggu.data.model.Song

object AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var onPreparedCallback: (() -> Unit)? = null

    fun play(song: Song, onComplete: (() -> Unit)? = null, onPrepared: (() -> Unit)? = null) {
        stop()

        if (song.audioFileName.isEmpty()) return

        onPreparedCallback = onPrepared

        mediaPlayer = MediaPlayer().apply {
            setDataSource(FileHelper.getAudioFile(song.audioFileName)?.absolutePath)
            setOnPreparedListener {
                start()
                onPreparedCallback?.invoke()
            }
            setOnCompletionListener {
                onComplete?.invoke()
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
