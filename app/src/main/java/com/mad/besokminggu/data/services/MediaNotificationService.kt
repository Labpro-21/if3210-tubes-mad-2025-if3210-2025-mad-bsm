package com.mad.besokminggu.data.services

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.manager.AudioPlayerManager
import com.mad.besokminggu.manager.NotificationHelper

class MediaNotificationService : MediaBrowserServiceCompat(), AudioManager.OnAudioFocusChangeListener {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioManager: AudioManager
    private lateinit var focusRequest: AudioFocusRequest
    private var isPlaying = false
    var isPrevValid = false

    override fun onCreate() {

        Log.d("NOTIFICATION_CENTER", "Create Notification Channel");
        super.onCreate()
        // Audio focus setup
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(this)
            .build()

        // MediaSession setup
        mediaSession = MediaSessionCompat(this, "MediaNotifService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay()    = handlePlay()
                override fun onPause()   = handlePause()
                override fun onStop()    = handleStop()
                override fun onSkipToNext()     = handleNext()
                override fun onSkipToPrevious() = handlePrev()
                override fun onSeekTo(pos: Long) {
                    AudioPlayerManager.seekTo(pos.toInt())
                }
                override fun onCustomAction(action: String?, extras: Bundle?) {
                    when (action) {
                        ACTION_TOGGLE_SHUFFLE -> toggleShuffle()
                        ACTION_CYCLE_REPEAT  -> cycleRepeat()
                        ACTION_SET_PREV_VALID -> isPrevValid = true
                        ACTION_SET_PREV_INVALID -> isPrevValid = false
                    }
                }
            })
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setSessionToken(sessionToken)
            isActive = true
        }

        // Notification channel
        NotificationHelper.createChannel(this)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)

        intent?.action?.let { action ->
            when (action) {
                ACTION_PLAY -> {
                    // Extract Song from extras (you must put it in the intent)
                    val song = intent.getParcelableExtra(EXTRA_SONG, Song::class.java)

                    if (song != null) {
                        playSong(song)
                    } else {
                        handlePlay()  // resume if no song provided
                    }
                }
                ACTION_PAUSE -> handlePause()
                ACTION_STOP  -> handleStop()
                ACTION_NEXT  -> handleNext()
                ACTION_PREV  -> handlePrev()
                ACTION_TOGGLE_SHUFFLE -> toggleShuffle()
                ACTION_CYCLE_REPEAT  -> cycleRepeat()
            }
        }

        return START_STICKY
    }
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot = BrowserRoot(MY_MEDIA_ROOT_ID, null)

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(emptyList())
    }

    override fun onBind(intent: Intent): IBinder? {

        return super.onBind(intent)
    }

    private fun playSong(song: Song) {
        if (audioManager.requestAudioFocus(focusRequest)
            == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        {
            val mediaSes = NotificationHelper.metadataFor(song)
            AudioPlayerManager.play(
                song = song,
                onComplete = { handleStop() },
                onPrepared = {
                    isPlaying = true
                    mediaSession.setMetadata(mediaSes);
                    mediaSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_PLAYING))
                    startForeground(NOTIFICATION_ID, NotificationHelper.buildNotification(this, mediaSession, isPlaying, isPrevValid))
                }
            )
        }
    }

    private fun handlePlay() {
        if (audioManager.requestAudioFocus(focusRequest)
            == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        {
            isPlaying = true
            AudioPlayerManager.resume()
            mediaSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_PLAYING))
            startForeground(NOTIFICATION_ID, NotificationHelper.buildNotification(this, mediaSession, isPlaying, isPrevValid))
        }
    }

    private fun handlePause() {
        AudioPlayerManager.pause()
        isPlaying = false
        mediaSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_PAUSED))
        startForeground(NOTIFICATION_ID, NotificationHelper.buildNotification(this, mediaSession, isPlaying, isPrevValid))
    }

    private fun handleStop() {
        AudioPlayerManager.stop()
        isPlaying = false
        mediaSession.setPlaybackState(buildState(PlaybackStateCompat.STATE_STOPPED))
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf()
        audioManager.abandonAudioFocusRequest(focusRequest)
    }

    private fun handleNext() = mediaSession.controller.transportControls.skipToNext()
    private fun handlePrev() = mediaSession.controller.transportControls.skipToPrevious()

    private fun toggleShuffle() {
        val newMode = if (mediaSession.controller.shuffleMode
            == PlaybackStateCompat.SHUFFLE_MODE_NONE)
            PlaybackStateCompat.SHUFFLE_MODE_ALL
        else
            PlaybackStateCompat.SHUFFLE_MODE_NONE

        mediaSession.setShuffleMode(newMode)
        startForeground(NOTIFICATION_ID, NotificationHelper.buildNotification(this, mediaSession, isPlaying, isPrevValid))
    }


    private fun cycleRepeat() {
        val newMode = when (mediaSession.controller.repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_NONE -> PlaybackStateCompat.REPEAT_MODE_ALL
            PlaybackStateCompat.REPEAT_MODE_ALL -> PlaybackStateCompat.REPEAT_MODE_ONE
            else                                -> PlaybackStateCompat.REPEAT_MODE_NONE
        }
        mediaSession.setRepeatMode(newMode)
        startForeground(NOTIFICATION_ID, NotificationHelper.buildNotification(this, mediaSession, isPlaying, isPrevValid))
    }

    private fun buildState(state: Int) : PlaybackStateCompat {
        return PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                state,
                AudioPlayerManager.getCurrentPosition().toLong(),
                1.0f
            )
            .build()
    }


    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS            -> handleStop()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT  -> handlePause()
            AudioManager.AUDIOFOCUS_GAIN            -> handlePlay()
        }
    }

    companion object {
        private const val MY_MEDIA_ROOT_ID = "media_root"
        const val ACTION_PLAY = "MEDIA.ACTION.PLAY"
        const val ACTION_PAUSE = "MEDIA.ACTION.PAUSE"
        const val ACTION_STOP = "MEDIA.ACTION.STOP"
        const val ACTION_PREV = "MEDIA.ACTION.PREV"
        const val ACTION_NEXT = "MEDIA.ACTION.NEXT"
        const val ACTION_TOGGLE_SHUFFLE = "MEDIA.ACTION.TOGGLE_SHUFFLE"
        const val ACTION_CYCLE_REPEAT = "MEDIA.ACTION.CYCLE_REPEAT"
        const val EXTRA_SONG = "extra_song"
        const val ACTION_NONE = "MEDIA.ACTION.NONE"
        const val ACTION_SET_PREV_VALID = "MEDIA.ACTION.SET_PREV_VALID"
        const val ACTION_SET_PREV_INVALID = "MEDIA.ACTION.SET_PREV_INVALID"
        const val CHANNEL_ID = "media_playback_channel"
        private const val NOTIFICATION_ID = 1
    }
}
