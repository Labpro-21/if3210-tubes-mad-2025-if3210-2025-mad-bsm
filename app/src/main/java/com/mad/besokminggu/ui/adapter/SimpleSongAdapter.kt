package com.mad.besokminggu.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song

class SimpleSongAdapter(
    val onItemClick: (Song) -> Unit
) : BaseSongAdapter(onItemClick) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseSongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return BaseSongViewHolder(view)
    }

    override fun onBindViewHolder(holder: BaseSongViewHolder, position: Int) {
        holder.bind(songs[position], onItemClick)
    }
}
