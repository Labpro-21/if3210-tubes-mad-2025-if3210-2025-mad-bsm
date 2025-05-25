package com.mad.besokminggu.ui.adapter

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.OnlineSong
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.manager.CoverFileHelper

open class OnlineSongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.songTitle)
    val artist: TextView = view.findViewById(R.id.songArtist)
    val cover: ImageView = view.findViewById(R.id.songCover)
    private val menuButton: ImageButton = view.findViewById(R.id.menu_button)

    open fun bind(song: OnlineSong, onClick: (OnlineSong) -> Unit, onMenuClick: (OnlineSong) -> Unit) {
        Log.d("OnlineSongViewHolder", "bind: $song")

        title.text = song.title
        artist.text = song.artist
        Glide.with(itemView.context)
            .load(song.artwork)
            .into(cover)

        itemView.setOnClickListener { onClick(song) }
        menuButton.setOnClickListener { onMenuClick(song) }
    }
}
