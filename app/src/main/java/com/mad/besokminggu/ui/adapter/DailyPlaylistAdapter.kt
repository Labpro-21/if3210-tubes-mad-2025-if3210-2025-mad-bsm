package com.mad.besokminggu.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.PlaylistSong

class DailyPlaylistAdapter(
    private val onItemClick: (PlaylistSong) -> Unit
) : RecyclerView.Adapter<DailyPlaylistAdapter.DailyPlaylistViewHolder>() {

    private val songs = mutableListOf<PlaylistSong>()

    fun submitList(newSongs: List<PlaylistSong>) {
        songs.clear()
        songs.addAll(newSongs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyPlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_playlist, parent, false)
        return DailyPlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: DailyPlaylistViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int = songs.size

    inner class DailyPlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.songTitle)
        private val artistText: TextView = itemView.findViewById(R.id.songArtist)
        private val coverImage: ImageView = itemView.findViewById(R.id.songCover)

        fun bind(playlist: PlaylistSong) {
            titleText.text = playlist.song.title
            artistText.text = playlist.song.artist

            Glide.with(itemView.context)
                .load(playlist.song.coverFileName)
                .placeholder(R.drawable.cover_daylist)
                .into(coverImage)

            itemView.setOnClickListener {
                onItemClick(playlist)
            }
        }
    }
}
