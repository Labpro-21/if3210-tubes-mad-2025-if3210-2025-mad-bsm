package com.mad.besokminggu.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.mad.besokminggu.data.model.Song

abstract class BaseSongAdapter(
    private val onItemClick: (Song) -> Unit
) : RecyclerView.Adapter<BaseSongViewHolder>() {

    protected var songs = listOf<Song>()

    fun submitList(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = songs.size
}
