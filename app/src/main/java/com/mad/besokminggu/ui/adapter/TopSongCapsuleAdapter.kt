package com.mad.besokminggu.ui.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.TopArtistCapsule
import com.mad.besokminggu.data.model.TopSongCapsule
import com.mad.besokminggu.manager.CoverFileHelper

class TopSongCapsuleAdapter :
    ListAdapter<TopSongCapsule, TopSongCapsuleAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rank: TextView = view.findViewById(R.id.text_rank)
        val title: TextView = view.findViewById(R.id.text_song_title)
        val artist: TextView = view.findViewById(R.id.text_artist)
        val plays: TextView = view.findViewById(R.id.text_plays)
        val cover: ImageView = view.findViewById(R.id.image_cover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topsong_capsule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem(position)
        holder.rank.text = (position + 1).toString().padStart(2, '0')
        holder.title.text = song.title
        holder.artist.text = song.artist
        holder.plays.text = "${song.playCount} plays"

        val file = song.coverFileName?.let { CoverFileHelper.getFile(it) }
        if (file != null && file.exists()) {
            holder.cover.setImageURI(Uri.fromFile(file))
        } else {
            holder.cover.setImageResource(R.drawable.cover_blonde)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TopSongCapsule>() {
        override fun areItemsTheSame(oldItem: TopSongCapsule, newItem: TopSongCapsule) = oldItem.title == newItem.title
        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: TopSongCapsule, newItem: TopSongCapsule) = oldItem == newItem
    }
}
