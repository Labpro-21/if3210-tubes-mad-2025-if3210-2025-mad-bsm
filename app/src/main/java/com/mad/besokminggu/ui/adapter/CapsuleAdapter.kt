package com.mad.besokminggu.ui.adapter

import MonthlySummaryCapsule
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mad.besokminggu.R
import com.mad.besokminggu.manager.CoverFileHelper


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

            if (!item.topArtistCover.isNullOrBlank()) {
                val file = CoverFileHelper.getFile(item.topArtistCover)
                if (file != null) {
                    if (file.exists()) {
                        holder.imageArtist.setImageURI(android.net.Uri.fromFile(file))
                    } else {
                        holder.imageArtist.setImageResource(R.drawable.cover_blonde)
                    }
                }
            } else {
                holder.imageArtist.setImageResource(R.drawable.cover_streak)
            }


            if (!item.topSongCover.isNullOrBlank()) {
                val file = CoverFileHelper.getFile(item.topSongCover)
                if (file != null) {
                    if (file.exists()) {
                        holder.imageSong.setImageURI(android.net.Uri.fromFile(file)) // ✅ FIXED
                    } else {
                        holder.imageSong.setImageResource(R.drawable.cover_starboy)
                    }
                }
            } else {
                holder.imageSong.setImageResource(R.drawable.cover_starboy)
            }

            // Make sure views are visible
            holder.textArtist.visibility = View.VISIBLE
            holder.textSong.visibility = View.VISIBLE
            holder.imageArtist.visibility = View.VISIBLE
            holder.imageSong.visibility = View.VISIBLE
        }
    }



    override fun getItemCount(): Int = items.size
}
