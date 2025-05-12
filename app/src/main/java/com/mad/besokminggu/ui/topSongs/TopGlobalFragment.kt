package com.mad.besokminggu.ui.topSongs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mad.besokminggu.data.model.OnlineSong
import com.mad.besokminggu.data.model.toSong
import com.mad.besokminggu.databinding.FragmentTopGlobalBinding
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.ui.adapter.OnlineSongAdapter
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.SongTracksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class TopGlobalFragment: Fragment() {

    private var _binding: FragmentTopGlobalBinding? = null

    private val binding get() = _binding!!

    private val songViewModel : SongTracksViewModel by activityViewModels()
    private val topSongsViewModel: TopSongsViewModel by activityViewModels()

    private lateinit var songAdapter: OnlineSongAdapter

    private fun onSongClick(song: OnlineSong){
        Log.d("MiniPlayer", "Song playing: ${song.title}")
        if(song.id != songViewModel.playedSong.value?.id){
            lifecycleScope.launch {

                songViewModel.playSong(song.toSong(), isOnline = true);
            }
        }
        songViewModel.showFullPlayer()
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


        val back = binding.backButton
        val rv = binding.songListRecyclerView
        val playButton = binding.playButton
        val duration = binding.TotalMin
        val month = binding.Date

        back.setOnClickListener {
            findNavController().popBackStack()
        }

        songAdapter = OnlineSongAdapter(
            onItemClick = { song -> onSongClick(song) },
            onMenuClick = { song -> onSongClick(song) }
        )

        rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }

        playButton.setOnClickListener {
            val currentSong = songViewModel.playedSong.value
            val firstSong = songAdapter.songs.first()
            if (currentSong == null) {
                lifecycleScope.launch {
                    songViewModel.playSong(firstSong.toSong(), isOnline = true);
                }
            }
            songViewModel.showFullPlayer()
        }

        topSongsViewModel.topSongs.observe(viewLifecycleOwner) { songList ->
            when (songList) {
                is ApiResponse.Loading -> {
//                    binding.progressBar?.visibility = View.VISIBLE
                }
                is ApiResponse.Success -> {
                    songAdapter.submitList(songList.data)
                    topSongsViewModel.updateSongsRepo(songList.data)
                    topSongsViewModel.updateTotalDuration()
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

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)
        val formattedDate = currentDate.format(formatter)

        month.text = formattedDate

        topSongsViewModel.totalDuration.observe(viewLifecycleOwner) {
            Log.d("TopGlobalFragment", "Total Duration: $it")
            val hour = it / 3600
            val min = it / 60 % 60

            val string = String.format("%dh %dmin", hour, min)

            duration.text = string
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