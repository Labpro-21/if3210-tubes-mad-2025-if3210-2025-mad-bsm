package com.mad.besokminggu.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song

class SongWithMenuAdapter(
    private val onItemClick: (Song) -> Unit,
    private val onMenuClick: (Song) -> Unit
) : BaseSongAdapter(onItemClick) {

    class SongMenuViewHolder(view: View) : BaseSongViewHolder(view) {
        private val menuButton: ImageButton = view.findViewById(R.id.menu_button)

        fun bindWithMenu(song: Song, onClick: (Song) -> Unit, onMenuClick: (Song) -> Unit) {
            super.bind(song, onClick)
            menuButton.setOnClickListener { onMenuClick(song) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseSongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongMenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: BaseSongViewHolder, position: Int) {
        val song = songs[position]
        if (holder is SongMenuViewHolder) {
            holder.bindWithMenu(song, onItemClick, onMenuClick)
        } else {
            holder.bind(song, onItemClick)
        }
    }
}
