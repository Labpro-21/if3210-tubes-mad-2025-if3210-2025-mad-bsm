package com.mad.besokminggu.ui.viewTracks
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaControllerCompat
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.bumptech.glide.Glide
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.manager.AudioPlayerManager
import com.mad.besokminggu.manager.FileHelper
import com.mad.besokminggu.viewModels.SongTracksViewModel
import kotlinx.coroutines.launch

class MiniPlayerView @JvmOverloads constructor(
    context :Context,
    attrs : AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr){
    private val ivCover: ImageView
    private val tvTitle: TextView
    private val tvArtist: TextView
    private val playButton : ImageButton
    private val likeButton : ImageButton
    private val progressBar : ProgressBar

    init {
        LayoutInflater.from(context).inflate(R.layout.fragment_miniplayer, this, true)
        ivCover = findViewById(R.id.songCover)
        tvTitle = findViewById(R.id.songTitle)
        tvArtist = findViewById(R.id.songArtist)
        playButton = findViewById(R.id.pause_button)
        likeButton= findViewById(R.id.like_button)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun handlePlayedSong(viewModel: SongTracksViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.playedSong.observe(lifecycleOwner) { song ->
            if (song == null) return@observe

            Log.d("MiniPlayerView", "Got song: ${song.title}")

            // General
            tvTitle.text = song.title
            tvArtist.text = song.artist
            Glide.with(context)
                .load(FileHelper.getFile(song.coverFileName, "cover"))
                .into(ivCover)
        }
    }

    private fun handleProgressBar(viewModel : SongTracksViewModel, lifecycleOwner : LifecycleOwner){
        viewModel.currentSongDuration.observe(lifecycleOwner) {
                duration ->
            progressBar.max = duration
        }

        viewModel.currentSeekPosition.observe(lifecycleOwner) {
                position ->
            progressBar.progress = position
        }
    }

    private fun handleMediaController(viewModel : SongTracksViewModel, lifecycleOwner : LifecycleOwner){
        // Pause Button
        viewModel.isPlaying.observe(lifecycleOwner) { isPlay ->
            if(isPlay){
                playButton.setImageResource(R.drawable.pause_icon_white)
            }else{
                playButton.setImageResource(R.drawable.play_icon_white)
            }
        }
        playButton.setOnClickListener {
            if (AudioPlayerManager.isPlaying()) {
                AudioPlayerManager.pause()
            } else {
                AudioPlayerManager.resume()
            }
            viewModel.togglePlayPause()

        }


        // Like Button
        likeButton.setOnClickListener {
            lifecycleOwner.lifecycle.coroutineScope.launch{
                viewModel.likeSong()
            }
        }
        viewModel.isLiked.observe(lifecycleOwner) {
                liked ->
            if(liked){
                likeButton.setImageResource(R.drawable.love_icon_filled)
            }else{
                likeButton.setImageResource(R.drawable.love)
            }
        }

    }

    fun observeViewModel() {

        val lifecycleOwner = findViewTreeLifecycleOwner() ?: return
        val viewModelStoreOwner = findViewTreeViewModelStoreOwner() ?: return
        val viewModel = ViewModelProvider(viewModelStoreOwner)[SongTracksViewModel::class.java]

        handlePlayedSong(viewModel, lifecycleOwner)
        handleProgressBar(viewModel, lifecycleOwner)
        handleMediaController(viewModel, lifecycleOwner)
    }
}