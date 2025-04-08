package com.mad.besokminggu.ui.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.manager.FileHelper

class NewSongAdapter(
    private val onClick: (Song) -> Unit = {}
) : RecyclerView.Adapter<NewSongAdapter.SongViewHolder>() {

    private var songs: List<Song> = emptyList()

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvArtist)
        val ivCover: ImageView = view.findViewById(R.id.ivCover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_newsong, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        val song = songs[position]

        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist
        Glide.with(holder.itemView.context).load(FileHelper.getCoverImage(song.coverFileName)).into(holder.ivCover)

        holder.tvTitle.isSelected = true
        holder.tvArtist.isSelected = true

        holder.itemView.setOnClickListener {
            onClick(song)
        }
    }


    override fun getItemCount() = songs.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateSongs(newSongs: List<Song>) {
        val diffResult = DiffUtil.calculateDiff(UpdateSong(this.songs, newSongs))
        this.songs = newSongs
        diffResult.dispatchUpdatesTo(this)
    }

}
