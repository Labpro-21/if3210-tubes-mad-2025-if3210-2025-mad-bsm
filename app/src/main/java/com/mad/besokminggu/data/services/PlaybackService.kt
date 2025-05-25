package com.mad.besokminggu.data.services
import com.mad.besokminggu.manager.PlaybackQueueManager
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaSession.Callback
import dagger.hilt.android.AndroidEntryPoint
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession.ConnectionResult.AcceptedResultBuilder
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.manager.AudioFileHelper
import com.mad.besokminggu.manager.AudioPlayerManager.controller
import com.mad.besokminggu.manager.CoverFileHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val CHANNEL_ID = "playback_channel"
private const val NOTIFICATION_ID = 1


/**
 * Media Service (Unstable), implementing the new Media3 API for mediaHandling and Notification
 */

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    @Inject lateinit var queueManager: PlaybackQueueManager
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        createChannel()

        player = ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
        }

        startForeground(NOTIFICATION_ID, makePlaceholderNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)


        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                queueManager.updatePlayPause(isPlaying)
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                super.onTimelineChanged(timeline, reason)
                if (!timeline.isEmpty) {
                    val currentWindow = timeline.getWindow(player.currentMediaItemIndex, Timeline.Window())
                    val durationMs = currentWindow.durationMs // Duration in milliseconds

                    if (durationMs != C.TIME_UNSET ) {
                        queueManager.updateSeekDuration(durationMs);

                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Log.d("PlaybackService", "Playback state changed: $playbackState")

                when (playbackState) {
                    Player.STATE_ENDED -> {
                        // Skip the song once the timer ended
                        serviceScope.launch {
                            val nextSong = queueManager.skipNext()
                            if (nextSong != null) {
                                Log.d("PlaybackService", "Playing next song: ${nextSong.title}")
                                playThroughPlayer(nextSong, queueManager.isOnline)
                            }
                        }
                    }
                }
            }
        })

        // Custom NextButton On Notification
        val skipNext = CommandButton.Builder(CommandButton.ICON_NEXT)
            .setSessionCommand(SessionCommand("NEXT", Bundle.EMPTY))
            .setDisplayName("Next")
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setId("BesokMingguSession")
            .setCustomLayout(ImmutableList.of(skipNext))
            .setCallback(MediaSessionCallback())
            .build()
    }


    private inner class MediaSessionCallback : Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>
        ): ListenableFuture<List<MediaItem>> {

            //Any media inserted, extract the URI
            val updated = mediaItems.map { item ->
                val uri = item.requestMetadata.mediaUri
                item.buildUpon()
                    .setUri(uri)
                    .setMediaId(item.mediaId)
                    .setMediaMetadata(item.mediaMetadata)
                    .build()
            }
            return Futures.immediateFuture(updated)
        }

        override fun onPlayerInteractionFinished(
            session: MediaSession,
            controllerInfo: MediaSession.ControllerInfo,
            playerCommands: Player.Commands
        ) {
            // Handle Previous Button clicked
            if(playerCommands.contains(Player.COMMAND_SEEK_TO_PREVIOUS)){
                val song = queueManager.skipPrevious();
                if(song == null){
                    player.seekTo(0);
                    return;
                }

                // Handle back don't change song if the duration is > 3
                if(player.duration > 3f){
                    player.seekTo(0);

                }else{
                    playThroughPlayer(song, queueManager.isOnline)
                }
                player.duration
            }
            super.onPlayerInteractionFinished(session, controllerInfo, playerCommands)
        }


        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {

            // Adding Next Button on the Notification
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                .buildUpon()
                .add(SessionCommand(
                    "NEXT",
                    Bundle.EMPTY
                ))
                .build()
            return AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        @SuppressLint("ServiceCast")
        override fun onDisconnected(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ) {

            // Handle case if the audio is disconnect
            super.onDisconnected(session, controller)
            queueManager.updateWarningText("External audio disconnected, routing back to default output.")
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {

            // Invoked on the Next Notification
            if(customCommand.customAction == "NEXT"){
                serviceScope.launch {
                    val song = queueManager.skipNext()
                    if(song == null){
                        return@launch
                    }
                    playThroughPlayer(song,queueManager.isOnline)
                }
            }

            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private fun makePlaceholderNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.play)
            .setContentTitle("")
            .setContentText("")
            .setOngoing(true)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        mediaSession

    override fun onDestroy() {
        player.release()
        mediaSession.release()
        stopForeground(true);
        super.onDestroy()
    }

    /**
     * Helper method to play the Song
     */
    private fun playThroughPlayer(song: Song, isOnline: Boolean) {
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

    private fun createChannel() {
        val chan = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.playback_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply { setShowBadge(false) }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(chan)
    }

}




