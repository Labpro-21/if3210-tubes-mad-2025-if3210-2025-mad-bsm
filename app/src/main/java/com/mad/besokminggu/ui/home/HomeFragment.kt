package com.mad.besokminggu.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mad.besokminggu.R
import com.mad.besokminggu.manager.AudioPlayerManager
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.databinding.FragmentTopGlobalBinding
import com.mad.besokminggu.ui.adapter.SongWithMenuAdapter
import com.mad.besokminggu.ui.optionMenu.SongActionSheet
import com.mad.besokminggu.viewModels.HomeViewModel
import com.mad.besokminggu.viewModels.SongTracksViewModel
import com.mad.besokminggu.viewModels.UserViewModel
import com.mad.besokminggu.ui.addsongs.AddSongsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var rvNewSongs: RecyclerView
    private lateinit var rvRecentlyPlayed: RecyclerView

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val songViewModel : SongTracksViewModel by activityViewModels()


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

            songViewModel.playSong(song);
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

        rvNewSongs = view.findViewById(R.id.rvNewSongs)
        rvRecentlyPlayed = view.findViewById(R.id.rvRecentlyPlayed)

        // Set up on button click listeners
        val topGlobalButton = view.findViewById<ImageButton>(R.id.topGlobal)
        val topLocalButton = view.findViewById<ImageButton>(R.id.topLocal)

        // Redirect to fragment
        topGlobalButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_top_global)
        }

        topLocalButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_top_local)
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
    }
}
