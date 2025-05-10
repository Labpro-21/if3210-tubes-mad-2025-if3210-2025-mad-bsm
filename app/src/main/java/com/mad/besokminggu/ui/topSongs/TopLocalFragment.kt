package com.mad.besokminggu.ui.topSongs

import android.os.Bundle
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
import com.mad.besokminggu.databinding.FragmentTopLocalBinding
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.ui.adapter.OnlineSongAdapter
import com.mad.besokminggu.ui.adapter.SongWithMenuAdapter
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.TopSongsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TopLocalFragment: Fragment() {

    private var _binding: FragmentTopLocalBinding? = null

    private val binding get() = _binding!!

    private val topSongsViewModel: TopSongsViewModel by activityViewModels()

    private lateinit var songAdapter: OnlineSongAdapter

    private fun onSongClick(song: OnlineSong){
        Log.d("MiniPlayer", "Song playing: ${song.title}")
        // TODO: Implement play song logic
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopLocalBinding.inflate(inflater, container, false)
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
            Log.d("TopLocalFragment", "Song List: $songList")
            when (songList) {
                is ApiResponse.Loading -> {
//                    binding.progressBar?.visibility = View.VISIBLE
                }
                is ApiResponse.Success -> {
                    Log.d("TopLocalFragment", "Success: ${songList.data}")

                    songAdapter.submitList(songList.data)
                }
                is ApiResponse.Failure -> {
//                    binding.progressBar?.visibility = View.GONE
                    // Handle error state
                }
                else -> {
                    Log.d("TopLocalFragment", "State: ${songList.javaClass}")
                }
            }
        }

        topSongsViewModel.getTopSongsCountry(
            country = "ID",
            coroutinesErrorHandler = object : CoroutinesErrorHandler {
                override fun onError(message: String) {
                    Log.e("TopLocalFragment", "Error: ${message}")
                }
            },
        )
    }

}