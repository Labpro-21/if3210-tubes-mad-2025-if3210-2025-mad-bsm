package com.mad.besokminggu.ui.viewTracks

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.bumptech.glide.Glide
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.manager.FileHelper
import com.mad.besokminggu.viewModels.SongTracksViewModel
import dagger.hilt.android.EntryPointAccessors

class MiniPlayerView @JvmOverloads constructor(
    context :Context,
    attrs : AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr){
    private val ivCover: ImageView
    private val tvTitle: TextView
    private val tvArtist: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.fragment_miniplayer, this, true)
        ivCover = findViewById(R.id.songCover)
        tvTitle = findViewById(R.id.songTitle)
        tvArtist = findViewById(R.id.songArtist)
    }

    fun observeViewModel() {
        val lifecycleOwner = findViewTreeLifecycleOwner() ?: return

        // Access Hilt-injected ViewModel manually (outside Fragment/Activity)
        val viewModelStoreOwner = findViewTreeViewModelStoreOwner() ?: return

        val viewModel = ViewModelProvider(viewModelStoreOwner)[SongTracksViewModel::class.java]

        viewModel.playedSong.observe(lifecycleOwner) { song: Song ->
            Log.d("MiniPlayerView", "Got song: ${song.title}")
            tvTitle.text = song.title
            tvArtist.text = song.artist
            Glide.with(context)
                .load(FileHelper.getFile(song.coverFileName, "cover"))
                .into(ivCover)
        }
    }


}