package com.mad.besokminggu.ui.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.manager.CoverFileHelper
import com.mad.besokminggu.manager.FileHelper

open class BaseSongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.songTitle)
    val artist: TextView = view.findViewById(R.id.songArtist)
    val cover: ImageView = view.findViewById(R.id.songCover)

    open fun bind(song: Song, onClick: (Song) -> Unit) {
        title.text = song.title
        artist.text = song.artist
        Glide.with(itemView.context)
            .load(CoverFileHelper.getFile(song.coverFileName))
            .into(cover)

        itemView.setOnClickListener { onClick(song) }
    }
}
