package com.mad.besokminggu.ui.optionMenu


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.manager.FileHelper

class SongActionSheet(
    private val song: Song,
    private val onQueue: () -> Unit,
    private val onDelete: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_song_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = view.findViewById<TextView>(R.id.songTitle)
        val artist = view.findViewById<TextView>(R.id.songArtist)
        val cover = view.findViewById<ImageView>(R.id.songCover)
        val queue = view.findViewById<LinearLayout>(R.id.queue_button)
        val delete = view.findViewById<LinearLayout>(R.id.delete_button)

        title.text = song.title
        artist.text = song.artist
        Glide.with(requireContext())
            .load(FileHelper.getCoverImage(song.coverFileName))
            .into(cover)

        queue.setOnClickListener {
            onQueue()
            dismiss()
        }

        delete.setOnClickListener {
            onDelete()
            dismiss()
        }
    }


}
