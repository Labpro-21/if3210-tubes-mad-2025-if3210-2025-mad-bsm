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
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.databinding.FragmentTopGlobalBinding
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.ui.adapter.SongWithMenuAdapter
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.TopSongsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TopGlobalFragment: Fragment() {

    private var _binding: FragmentTopGlobalBinding? = null

    private val binding get() = _binding!!

    private val topSongsViewModel: TopSongsViewModel by activityViewModels()

    private lateinit var songAdapter: SongWithMenuAdapter

    private fun onSongClick(song: Song){
        Log.d("MiniPlayer", "Song playing: ${song.title}")
        // TODO: Implement play song logic
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

        binding.backButton?.setOnClickListener {
            findNavController().popBackStack()
        }

        songAdapter = SongWithMenuAdapter(
            onItemClick = { song -> onSongClick(song) },
            onMenuClick = { {} }
        )

        binding.songListRecyclerView?.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(context)
        }

        topSongsViewModel.topSongs.observe(viewLifecycleOwner) { songList ->
            Log.d("TopGlobalFragment", "Song List: ${songList}")
            when (songList) {
                is ApiResponse.Loading -> {
//                    binding.progressBar?.visibility = View.VISIBLE
                }
                is ApiResponse.Success -> {
                    Log.d("TopGlobalFragment", "Success: ${songList.data}")
                    songList.data.forEach { song ->
                        Log.d("TopGlobalFragment", "Song: ${song}")
                    }
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
        Log.d("TopGlobalFragment", "Top Songs: ${topSongsViewModel.topSongs.value}")
        topSongsViewModel.getTopSongsGlobal(
            coroutinesErrorHandler = object : CoroutinesErrorHandler {
                override fun onError(message: String) {
                    Log.e("TopGlobalFragment", "Error: ${message}")
                }
            },
        )
    }

}

private fun Any.forEach(any: Any) {

}
