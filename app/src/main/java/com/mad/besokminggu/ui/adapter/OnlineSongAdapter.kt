package com.mad.besokminggu.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.OnlineSong
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.ui.adapter.SongWithMenuAdapter.SongMenuViewHolder

class OnlineSongAdapter(
    private val onItemClick: (OnlineSong) -> Unit,
    private val onMenuClick: (OnlineSong) -> Unit
) : RecyclerView.Adapter<OnlineSongViewHolder>() {

    protected var songs = listOf<OnlineSong>()

    fun submitList(newSongs: List<OnlineSong>) {
        songs = newSongs
        Log.d("OnlineSongAdapter", "submitList: $songs")
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = songs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnlineSongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        Log.d("OnlineSongAdapter", "onCreateViewHolder: $view")
        return OnlineSongViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnlineSongViewHolder, position: Int) {
        val song = songs[position]
        Log.d("OnlineSongAdapter", "onBindViewHolder: $song")
        if (holder is OnlineSongViewHolder) {
            holder.bind(song, onItemClick, onMenuClick)
        } else {
            holder.bind(song, onItemClick, onMenuClick)
        }
    }
}
