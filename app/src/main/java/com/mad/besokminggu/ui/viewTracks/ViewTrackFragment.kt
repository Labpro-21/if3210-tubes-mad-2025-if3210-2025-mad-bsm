package com.mad.besokminggu.ui.viewTracks
import android.annotation.SuppressLint
import com.mad.besokminggu.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.mad.besokminggu.manager.AudioPlayerManager
import com.mad.besokminggu.databinding.FragmentTrackViewBinding
import com.mad.besokminggu.manager.FileHelper
import com.mad.besokminggu.viewModels.SongTracksViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.ViewModelLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewTrackFragment : Fragment(){
    private var _binding : FragmentTrackViewBinding? = null;
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
            viewModel.skipToNext();
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var isUserSeeking = false

        viewModel.playedSong.observe(viewLifecycleOwner) {
            song ->

            if(song == null){
                return@observe
            }

            AudioPlayerManager.play(song,
                onComplete = { skipSong() },
                onPrepared = {
                    val duration = AudioPlayerManager.getDuration()
                    viewModel.updateSongDuration(duration)
                    binding.maxTime.text = formatTime(duration)
                    binding.progressBar.max = duration
                }
            )


            // General
            binding.songTitle.text = song.title
            binding.songSinger.text = song.artist
            Glide.with(requireContext())
                .load(FileHelper.getCoverImage(song.coverFileName))
                .into(binding.songImage)

            // Love Button
            viewModel.updateIsLike(song.isLiked)
            viewModel.updatePlayPause(true);
        }
        viewModel.currentSongDuration.observe(viewLifecycleOwner) {
            duration ->
            binding.maxTime.text = formatTime(duration)
            binding.progressBar.max = duration
        }

        // Love Button
        binding.loveButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.likeSong()
            }
        }

        viewModel.isLiked.observe(viewLifecycleOwner) {
            liked ->
            if(liked){
                binding.loveButton.setImageResource(R.drawable.love_icon_filled)
            }else{
                binding.loveButton.setImageResource(R.drawable.love)
            }
        }

        viewModel.previousSongsQueue.observe (viewLifecycleOwner){
            queue ->
            if(queue.isEmpty()){
                binding.previousButton.isEnabled = false
            }
        }

        binding.nextButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.skipToNext()
            }
        }

        binding.previousButton.setOnClickListener {
            viewModel.skipToPrevious()
        }

        // Play Button
        viewModel.isPlaying.observe(viewLifecycleOwner) {
            isPlaying ->
            if (isPlaying) {
                binding.playButton.setImageResource(R.drawable.pause_icon)
            } else {
                binding.playButton.setImageResource(R.drawable.play_icon)
            }

        }

        binding.playButton.setOnClickListener {
            if (AudioPlayerManager.isPlaying()) {
                AudioPlayerManager.pause()
            } else {
                AudioPlayerManager.resume()
            }
            viewModel.togglePlayPause()
        }

        // Progress
        viewModel.currentSeekPosition.observe(viewLifecycleOwner){
            time ->
            if (!isUserSeeking) {
                binding.currentTime.text = formatTime(time)
                binding.progressBar.progress = time
            }
        }

        startSeekBarUpdater();

        binding.progressBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {


            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.currentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                isUserSeeking = false
                val seekTo = seekBar?.progress ?: 0
                AudioPlayerManager.seekTo(seekTo)
                viewModel.updateSeekPosition(seekTo)
            }
        })


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