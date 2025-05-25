package com.mad.besokminggu.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.ui.adapter.SongWithMenuAdapter
import com.mad.besokminggu.ui.optionMenu.SongActionSheet
import com.mad.besokminggu.viewModels.HomeViewModel
import com.mad.besokminggu.viewModels.SongTracksViewModel
import com.mad.besokminggu.viewModels.UserViewModel
import com.mad.besokminggu.ui.addsongs.AddSongsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.mad.besokminggu.MainActivity
import com.mad.besokminggu.data.model.toSong
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.ui.topSongs.TopSongsViewModel
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.OnlineSongsViewModel

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var rvNewSongs: RecyclerView
    private lateinit var rvRecentlyPlayed: RecyclerView

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val songViewModel : SongTracksViewModel by activityViewModels()
    private val onlineSongsViewModel : OnlineSongsViewModel by activityViewModels()
    private val topSongsViewModel : TopSongsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private fun onSongClick(song: Song){
        Log.d("MiniPlayer", "Song playing: ${song.title}")
        if(song.id != songViewModel.playedSong.value?.id){
            lifecycleScope.launch {

            songViewModel.playSong(song)
            }
        }
        songViewModel.showFullPlayer()

    }

    fun onOpenSheet(song : Song){
        SongActionSheet(
            song = song,
            onQueue = {
                lifecycleScope.launch {
                songViewModel.addToNextQueue(song)
                }
            },
            onEdit = {
                val existingFragment = parentFragmentManager.findFragmentByTag("AddSongsBottomSheet")
                if (existingFragment == null) {
                    val editFragment = AddSongsFragment()

                    val args = Bundle().apply {
                        putBoolean("isEditMode", true)
                        putInt("songID", song.id)
                        putString("songTitle", song.title)
                        putString("artistName", song.artist)
                        putString("songFilePath", song.audioFileName)
                        putString("songImagePath", song.coverFileName)
                    }

                    editFragment.arguments = args
                    editFragment.show(parentFragmentManager, "AddSongsBottomSheet")
                }
            },
            onDelete = {
                lifecycleScope.launch {
                    songViewModel.deleteSong(song)
                }
            }
        ).show(parentFragmentManager, "SongActionSheet")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val receivedSongId = arguments?.getInt("songId")

        rvNewSongs = view.findViewById(R.id.rvNewSongs)
        rvRecentlyPlayed = view.findViewById(R.id.rvRecentlyPlayed)

        // Set up on button click listeners
        val topGlobalButton = view.findViewById<ImageButton>(R.id.topGlobal)
        val topLocalButton = view.findViewById<ImageButton>(R.id.topLocal)
        val qrButton = view.findViewById<ImageButton>(R.id.qrButton)

        // Redirect to fragment
        topGlobalButton.setOnClickListener {
            val bundle = bundleOf("isGlobal" to true)
            findNavController().navigate(R.id.navigation_top_songs, bundle)
        }

        topLocalButton.setOnClickListener {
            val bundle = bundleOf("isGlobal" to false)
            findNavController().navigate(R.id.navigation_top_songs, bundle)
        }

        qrButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_qr_scanner)
        }

        val newSongsAdapter = NewSongAdapter { song ->
            onSongClick(song)
        }

        val recentlyPlayedAdapter = SongWithMenuAdapter(
            onItemClick = { song ->
                onSongClick(song)
            },
            onMenuClick = {song ->
                onOpenSheet(song)
            },
        )

        rvNewSongs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvNewSongs.adapter = newSongsAdapter

        rvRecentlyPlayed.layoutManager = LinearLayoutManager(requireContext())
        rvRecentlyPlayed.adapter = recentlyPlayedAdapter


        userViewModel.profile.observe(viewLifecycleOwner) {
            homeViewModel.refreshSong(it.id)

            homeViewModel.allSongs.observe(viewLifecycleOwner) { songs ->
                val newSongs = songs.filter { newSong ->
                    newSong.lastPlayedAt == null
                }
                homeViewModel._newSongs.postValue(newSongs)

                val recentlyPlayedSongs = songs
                    .filter { it.lastPlayedAt != null }
                    .sortedByDescending { it.lastPlayedAt }
                homeViewModel._recentlyPlayed.postValue(recentlyPlayedSongs)
            }
        }

        homeViewModel.allSongs.observe(viewLifecycleOwner){ songs ->
            val newSongs = songs.filter { newSong ->
                newSong.lastPlayedAt == null
            }
            homeViewModel._newSongs.postValue(newSongs)

            val recentlyPlayedSongs = songs
                .filter { it.lastPlayedAt != null }
                .sortedByDescending { it.lastPlayedAt }
            homeViewModel._recentlyPlayed.postValue(recentlyPlayedSongs)
        }

        homeViewModel.newSongs.observe(viewLifecycleOwner) { songs ->
            newSongsAdapter.updateSongs(songs)
        }

        homeViewModel.recentlyPlayed.observe(viewLifecycleOwner) { songs ->
            recentlyPlayedAdapter.submitList(songs)
        }

        if (receivedSongId != null) {
            Log.d("HomeFragment", "Received song ID: $receivedSongId")
            if (receivedSongId != -1) {
                loadSong(receivedSongId)
            }
        }
    }

    private fun loadSong(songId: Int) {
        onlineSongsViewModel.getSongById(songId, object : CoroutinesErrorHandler {
            override fun onError(message: String) {
                Log.e("DeepLink", "Error loading song: $message")
            }
        })

        topSongsViewModel.topSongs.observe(this) { songList ->
            when (songList) {
                is ApiResponse.Loading -> {
//                    binding.progressBar?.visibility = View.VISIBLE
                }

                is ApiResponse.Success -> {
//                    songAdapter.submitList(songList.data)
                    topSongsViewModel.updateSongsRepo(songList.data)
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

        songViewModel._isOnlineSong.postValue(true)

        onlineSongsViewModel.song.observe(this) { response ->
            when (response) {
                is ApiResponse.Success -> {
                    val song = response.data
                    Log.d("DeepLink", "Loaded song: ${song.title}")
                    lifecycleScope.launch {
                        songViewModel.playSong(
                            song = song.toSong(),
                            isOnline = true
                        )
                        songViewModel.showFullPlayer()
                    }
                }
                is ApiResponse.Failure -> {
                    Log.e("DeepLink", "Failed to load song")
                }
                is ApiResponse.Loading -> {
                    Log.d("DeepLink", "Loading song...")
                }
            }
        }
    }
}
