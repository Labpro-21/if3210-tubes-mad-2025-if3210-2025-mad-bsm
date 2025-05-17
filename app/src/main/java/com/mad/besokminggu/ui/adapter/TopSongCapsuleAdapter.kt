package com.mad.besokminggu.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.TopSongCapsule

class TopSongCapsuleAdapter(
    private val songs: List<TopSongCapsule>
) : RecyclerView.Adapter<TopSongCapsuleAdapter.ViewHolder>() {

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val rank = view.findViewById<TextView>(R.id.text_rank)
        val title = view.findViewById<TextView>(R.id.text_song_title)
        val artist = view.findViewById<TextView>(R.id.text_artist)
        val cover = view.findViewById<ImageView>(R.id.image_cover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_topsong_capsule, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = minOf(songs.size, 5)


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
        holder.rank.text = song.rank
        holder.title.text = song.title
        holder.artist.text = song.artist
        holder.cover.setImageResource(song.coverResId)
    }
}
