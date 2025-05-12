package com.mad.besokminggu.ui.viewTracks
import android.annotation.SuppressLint
import com.mad.besokminggu.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.manager.AudioPlayerManager
import com.mad.besokminggu.databinding.FragmentTrackViewBinding
import com.mad.besokminggu.manager.AudioFileHelper
import com.mad.besokminggu.manager.CoverFileHelper
import com.mad.besokminggu.viewModels.RepeatMode
import com.mad.besokminggu.viewModels.SongTracksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewTrackFragment : Fragment(){
    private var _binding : FragmentTrackViewBinding? = null
    private val binding get() = _binding!!

    private val viewModel : SongTracksViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(millis: Int): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%01d:%02d", minutes, seconds)
    }

    private fun skipSong(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.skipToNext()
        }
    }

    private fun handlePlayedSongEvent(){

        val playedSong : LiveData<Song?> = viewModel.playedSong
        val maxTimeText : TextView = binding.maxTime
        val progressBar : SeekBar = binding.progressBar
        val songTitle : TextView= binding.songTitle
        val songSinger : TextView= binding.songSinger
        val songImage : ImageView= binding.songImage

        playedSong.observe(viewLifecycleOwner) { song ->

            if(song == null){
                AudioPlayerManager.stop()
                return@observe
            }

            val isOnline = viewModel.isOnlineSong.value ?: false

            Log.d("ViewTrackFragment", "Song playing: ${song.title}")

            AudioPlayerManager.play(song,
                isOnline = isOnline,
                onComplete = { skipSong() },
                onPrepared = {
                    val duration = AudioPlayerManager.getDuration()
                    viewModel.updateSongDuration(duration)
                    maxTimeText.text = formatTime(duration)
                    progressBar.max = duration
                }
            )


            // General

            songTitle.text = song.title
            songSinger.text = song.artist

            songTitle.isSelected = true
            songSinger.isSelected = true
            Glide.with(requireContext())
                .load(if (isOnline) song.coverFileName else CoverFileHelper.getFile(song.coverFileName))
                .into(songImage)

            // Love Button
            viewModel.updateIsLike(song.isLiked)
            viewModel.updatePlayPause(true)
        }
    }

    private fun handleProgressBar(){
        var isUserSeeking = false
        val seekPosition : LiveData<Int> = viewModel.currentSeekPosition
        val currentTime : TextView = binding.currentTime
        val progressBar : SeekBar = binding.progressBar
        val maxTime : TextView = binding.maxTime


        seekPosition.observe(viewLifecycleOwner){
                time ->
            if (!isUserSeeking) {
                currentTime.text = formatTime(time)
                progressBar.progress = time
            }
        }
        viewModel.currentSongDuration.observe(viewLifecycleOwner) {
                duration ->
            maxTime.text = formatTime(duration)
            progressBar.max = duration
        }
        startSeekBarUpdater()
        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                val seekTo = seekBar?.progress ?: 0
                AudioPlayerManager.seekTo(seekTo)
                viewModel.updateSeekPosition(seekTo)
            }
        })
    }

    private fun handleMediaController(){
        val previousSongQueue:  LiveData<List<Song>> = viewModel.previousSongsQueue
        val isLiked : LiveData<Boolean> = viewModel.isLiked
        val isPlaying : LiveData<Boolean> = viewModel.isPlaying
        val isShuffle : LiveData<Boolean> = viewModel.isShuffle
        val repeatMode : LiveData<RepeatMode> = viewModel.repeatMode


        val nextButton : ImageButton = binding.nextButton
        val loveButton: ImageButton = binding.loveButton
        val previousButton : ImageButton = binding.previousButton
        val playButton : ImageButton = binding.playButton
        val shuffleButton : ImageButton = binding.shuffleButton
        val repeatButton : ImageButton = binding.repeatButton


        // Next Button Event

        nextButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.skipToNext()
            }
        }

        // Love Button on Click
        loveButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.likeSong()
            }
        }

        // Love Button UI change
        isLiked.observe(viewLifecycleOwner) {
                 liked ->
            if(liked){
                loveButton.setImageResource(R.drawable.love_icon_filled)
            }else{
                loveButton.setImageResource(R.drawable.love)
            }
        }

        // Previous Button Event
        previousSongQueue.observe (viewLifecycleOwner){
                queue ->
            if(queue.isEmpty()){
                previousButton.isEnabled = false
                previousButton.setImageResource(R.drawable.previous_icon_disabled)
            }else{
                previousButton.isEnabled = true
                previousButton.setImageResource(R.drawable.previous_icon)
            }
        }
        previousButton.setOnClickListener {
            viewModel.skipToPrevious()
        }


        // Play Button
        isPlaying.observe(viewLifecycleOwner) {
                isPlaying ->
            if (isPlaying) {
                playButton.setImageResource(R.drawable.pause_icon)
            } else {
                playButton.setImageResource(R.drawable.play_icon)
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

        // Shuffle
        shuffleButton.setOnClickListener {
            viewModel.toggleShuffle()
        }
        isShuffle.observe (viewLifecycleOwner){isShuffle ->

            if(isShuffle){
                shuffleButton.setImageResource(R.drawable.shuffle_fill)
            }else{
                shuffleButton.setImageResource(R.drawable.shuffle)
            }
        }

        // Repeat
        repeatButton.setOnClickListener {
            viewModel.toggleRepeat()
        }

        repeatMode.observe(viewLifecycleOwner) { repeatState ->
            if(repeatState == null){
                repeatButton.setImageResource(R.drawable.repeat)
            }
            when(repeatState){
                RepeatMode.NONE -> repeatButton.setImageResource(R.drawable.repeat)
                RepeatMode.REPEAT_ONE -> repeatButton.setImageResource(R.drawable.repeat_one_icon)
                RepeatMode.REPEAT_ALL -> repeatButton.setImageResource(R.drawable.repeat_all_icon)

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handlePlayedSongEvent();
        handleProgressBar();
        handleMediaController()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startSeekBarUpdater() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                if (AudioPlayerManager.isPlaying()) {
                    val current = AudioPlayerManager.getCurrentPosition()
                    viewModel.updateSeekPosition(current)
                }
                delay(1000L)
            }
        }

    }

}