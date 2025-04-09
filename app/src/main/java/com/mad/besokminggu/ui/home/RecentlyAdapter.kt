package com.mad.besokminggu.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.manager.CoverFileHelper
import com.mad.besokminggu.manager.FileHelper

class RecentlyAdapter (
    private val onItemClick: (Song) -> Unit,
    private val onMenuClick : (Song) -> Unit,
) :
    ListAdapter<Song, RecentlyAdapter.SongViewHolder>(DIFF_CALLBACK) {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val ivCover: ImageView = itemView.findViewById(R.id.songCover)
        val tvTitle: TextView = itemView.findViewById(R.id.songArtist)
        val tvArtist: TextView = itemView.findViewById(R.id.songTitle)
        val menuButton : ImageButton = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {

        val song = getItem(position)

        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist
        Glide.with(holder.itemView).load(CoverFileHelper.getFile(song.coverFileName)).into(holder.ivCover)

        holder.itemView.setOnClickListener(){
            onItemClick(song)
        }

        holder.menuButton.setOnClickListener() {
            onMenuClick(song)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
                oldItem == newItem
        }
    }
}
