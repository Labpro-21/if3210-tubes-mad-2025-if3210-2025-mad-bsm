package com.mad.besokminggu.ui.topSongs

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mad.besokminggu.data.model.OnlineSong
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.databinding.FragmentTopGlobalBinding
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.ui.adapter.OnlineSongAdapter
import com.mad.besokminggu.ui.adapter.SongWithMenuAdapter
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.TopSongsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException

@AndroidEntryPoint
class TopGlobalFragment: Fragment() {

    private var _binding: FragmentTopGlobalBinding? = null

    private val binding get() = _binding!!

    private val topSongsViewModel: TopSongsViewModel by activityViewModels()

    private lateinit var songAdapter: OnlineSongAdapter

    private val mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        setScreenOnWhilePlaying(true)
    }

    private fun onSongClick(song: OnlineSong){
        Log.d("MiniPlayer", "Song playing: ${song.title}")
        // TODO: Implement play song logic
        val url = song.url
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.setOnPreparedListener {
                it.start()
            }
            mediaPlayer.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopGlobalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        songAdapter = OnlineSongAdapter(
            onItemClick = { song -> onSongClick(song) },
            onMenuClick = { song -> onSongClick(song) }
        )

        binding.songListRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }

        topSongsViewModel.topSongs.observe(viewLifecycleOwner) { songList ->
            Log.d("TopGlobalFragment", "Song List: $songList")
            when (songList) {
                is ApiResponse.Loading -> {
//                    binding.progressBar?.visibility = View.VISIBLE
                }
                is ApiResponse.Success -> {
                    Log.d("TopGlobalFragment", "Success: ${songList.data}")

                    songAdapter.submitList(songList.data)
                }
                is ApiResponse.Failure -> {
//                    binding.progressBar?.visibility = View.GONE
                    // Handle error state
                }
                else -> {
                    Log.d("TopGlobalFragment", "State: ${songList.javaClass}")
                }
            }
        }

        topSongsViewModel.getTopSongsGlobal(
            coroutinesErrorHandler = object : CoroutinesErrorHandler {
                override fun onError(message: String) {
                    Log.e("TopGlobalFragment", "Error: ${message}")
                }
            },
        )
    }

}