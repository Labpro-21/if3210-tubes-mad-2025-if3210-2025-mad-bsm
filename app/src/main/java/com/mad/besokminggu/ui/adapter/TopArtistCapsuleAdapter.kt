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
import com.mad.besokminggu.manager.CoverFileHelper

class TopArtistCapsuleAdapter : ListAdapter<TopArtistCapsule, TopArtistCapsuleAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rank: TextView = view.findViewById(R.id.text_rank)
        val name: TextView = view.findViewById(R.id.text_artist_name)
        val cover: ImageView = view.findViewById(R.id.image_cover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topartist_capsule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artist = getItem(position)
        holder.rank.text = (position + 1).toString().padStart(2, '0')
        holder.name.text = artist.name

        val file = CoverFileHelper.getFile(artist.coverFileName)
        if (file != null && file.exists()) {
            holder.cover.setImageURI(Uri.fromFile(file))
        } else {
            holder.cover.setImageResource(R.drawable.cover_blonde)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TopArtistCapsule>() {
        override fun areItemsTheSame(oldItem: TopArtistCapsule, newItem: TopArtistCapsule) = oldItem.name == newItem.name
        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: TopArtistCapsule, newItem: TopArtistCapsule) = oldItem == newItem
    }
}
