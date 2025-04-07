package com.mad.besokminggu.ui.viewTracks
import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.mad.besokminggu.audio.AudioPlayerManager
import com.mad.besokminggu.databinding.FragmentTrackViewBinding
import com.mad.besokminggu.viewModels.SongTracksViewModel
import dagger.hilt.android.AndroidEntryPoint
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.playedSong.observe(viewLifecycleOwner) {
            song -> binding.songTitle.text = song.title
            binding.songSinger.text = song.artist
            Glide.with(requireContext())
                .load(song.coverResId)
                .into(binding.songImage)
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

        binding.playButton.setOnClickListener {
            if (AudioPlayerManager.isPlaying()) {
                AudioPlayerManager.pause()
                binding.playButton.setImageResource(R.drawable.ic_media_play)
            } else {
                AudioPlayerManager.resume()
                binding.playButton.setImageResource(R.drawable.ic_media_pause)
            }
            viewModel.togglePlayPause()
        }

        startSeekBarUpdater();

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
                    val duration = AudioPlayerManager.getDuration()

                    binding.progressBar.max = duration
                    binding.progressBar.progress = current

                    viewModel.updateSeekPosition(current)
                }
                delay(1000L)
            }
        }
    }

}