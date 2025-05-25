package com.mad.besokminggu.manager

import android.content.Context
import android.content.Intent

object DeepLinkHelper {
    fun createSongShareLink(songId: Int): String {
        return "purrytify://song/$songId"
    }

    fun shareSongLink(context: Context, songId: Int, songArtist : String, songTitle: String) {
        val shareText = "Check out this song: $songTitle by $songArtist\n${DeepLinkHelper.createSongShareLink(songId)}"

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Song via")
        context.startActivity(shareIntent)
    }
}