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
import com.mad.besokminggu.data.model.OnlineSong

class OnlineSongActionSheet(
    private val song: OnlineSong,
    private val onDownload: () -> Unit,
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_online_song_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = view.findViewById<TextView>(R.id.songTitle)
        val artist = view.findViewById<TextView>(R.id.songArtist)
        val cover = view.findViewById<ImageView>(R.id.songCover)
        val download = view.findViewById<LinearLayout>(R.id.download_button)

        title.text = song.title
        artist.text = song.artist
        Glide.with(requireContext())
            .load(song.artwork)
            .into(cover)

        download.setOnClickListener {
            onDownload()
            dismiss()
        }
    }

}
