package com.mad.besokminggu.manager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.services.MediaNotificationService

object NotificationHelper {
    fun createChannel(context: Context) {
        Log.d("NOTIFICATION_CENTER", "Create Notification Channel");
        val channel = NotificationChannel(
            MediaNotificationService.CHANNEL_ID,
            "Media Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Media playback controls" }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    fun metadataFor(song: Song): MediaMetadataCompat {
        val builder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title).putString(
            MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist
        )

        if (song.coverFileName.isNotBlank()) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.coverFileName);
        }

        return builder.build();
    }

    fun buildNotification(
        context: Context,
        session: MediaSessionCompat,
        isPlaying: Boolean,
        isPrevValid: Boolean,

    ): Notification {

        val shuffleState = session.controller.shuffleMode
        val repeatState = session.controller.repeatMode
        val desc = session.controller.metadata?.description
        val prevIcon = if(isPrevValid) R.drawable.previous_icon else R.drawable.previous_icon_disabled
        val nextIcon = R.drawable.next_icon
        val (playPauseIcon, playPauseTitle) =
            if (isPlaying) R.drawable.play_icon_white to "Play Button" else R.drawable.play_icon_white to "Pause Button"
        val (shuffleIcon, shuffleTitle) =
            if (shuffleState == PlaybackStateCompat.SHUFFLE_MODE_NONE) R.drawable.shuffle_fill to "No Shuffle" else R.drawable.shuffle to "Shuffle"
        val (repeatIcon, repeatTitle) = when (repeatState) {
            PlaybackStateCompat.REPEAT_MODE_NONE -> R.drawable.repeat to "Repeat"
            PlaybackStateCompat.REPEAT_MODE_ALL  -> R.drawable.repeat_all_icon to "Repeat All"
            PlaybackStateCompat.REPEAT_MODE_ONE  -> R.drawable.repeat_one_icon to "Repeat One"
            else -> R.drawable.repeat to "Repeat"
        }
        val builder = NotificationCompat.Builder(context, MediaNotificationService.CHANNEL_ID)
            .setContentTitle(desc?.title)
            .setContentText(desc?.subtitle)
            .setLargeIcon(desc?.iconBitmap)
            .setSmallIcon(R.drawable.delete_song)
            .setOnlyAlertOnce(true)
            .setStyle(
                MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2, 3, 4)
            )

        builder.addAction(shuffleIcon, shuffleTitle, actionIntent(
            context,
            action = MediaNotificationService.ACTION_TOGGLE_SHUFFLE
        )).addAction(prevIcon, "Prev Button",actionIntent(context, if(isPrevValid) MediaNotificationService.ACTION_PREV else MediaNotificationService.ACTION_NONE))
            .addAction(playPauseIcon,playPauseTitle, actionIntent(context, if(isPlaying) MediaNotificationService.ACTION_PAUSE else MediaNotificationService.ACTION_PLAY))
            .addAction(nextIcon, "Next Button", actionIntent(context, MediaNotificationService.ACTION_NEXT))
            .addAction(repeatIcon, repeatTitle, actionIntent(context, MediaNotificationService.ACTION_CYCLE_REPEAT))

        return builder.build()
    }

    private fun actionIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, MediaNotificationService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // ... helper methods for each action button (shuffleAction, prevAction, etc.)
}
