package com.mad.besokminggu.ui.adapter

import MonthlySummaryCapsule
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mad.besokminggu.R

class CapsuleAdapter(private val items: List<MonthlySummaryCapsule>) :
    RecyclerView.Adapter<CapsuleAdapter.CapsuleViewHolder>() {

    inner class CapsuleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textMonth: TextView = view.findViewById(R.id.text_month_year)
        val textMinutes: TextView = view.findViewById(R.id.text_minutes)
        val textArtist: TextView = view.findViewById(R.id.text_top_artist)
        val textSong: TextView = view.findViewById(R.id.text_top_song)
        val imageArtist: ImageView = view.findViewById(R.id.image_artist)
        val imageSong: ImageView = view.findViewById(R.id.image_song)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapsuleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_capsule, parent, false)
        return CapsuleViewHolder(view)
    }



    override fun onBindViewHolder(holder: CapsuleViewHolder, position: Int) {
        val item = items[position]
        holder.textMonth.text = item.month

        if (item.isEmpty) {
            holder.textMinutes.text = "No data available"
            holder.textArtist.visibility = View.GONE
            holder.textSong.visibility = View.GONE
            holder.imageArtist.visibility = View.GONE
            holder.imageSong.visibility = View.GONE
        } else {
            holder.textMinutes.text = "${item.totalMinutes} minutes"
            holder.textArtist.text = "Top Artist: ${item.topArtist}"
            holder.textSong.text = "Top Song: ${item.topSong}"
            item.topArtistImageRes?.let { holder.imageArtist.setImageResource(it) }
            item.topSongImageRes?.let { holder.imageSong.setImageResource(it) }

            // make sure to show views if they were hidden earlier
            holder.textArtist.visibility = View.VISIBLE
            holder.textSong.visibility = View.VISIBLE
            holder.imageArtist.visibility = View.VISIBLE
            holder.imageSong.visibility = View.VISIBLE
        }
    }


    override fun getItemCount(): Int = items.size
}
