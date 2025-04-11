package com.mad.besokminggu.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mad.besokminggu.R
import com.mad.besokminggu.manager.AudioPlayerManager
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.ui.adapter.SongWithMenuAdapter
import com.mad.besokminggu.ui.optionMenu.SongActionSheet
import com.mad.besokminggu.viewModels.HomeViewModel
import com.mad.besokminggu.viewModels.SongTracksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var rvNewSongs: RecyclerView
    private lateinit var rvRecentlyPlayed: RecyclerView

    private val homeViewModel: HomeViewModel by activityViewModels()
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
            songViewModel.playSong(song);
        }
        songViewModel.showFullPlayer()

    }

    fun onOpenSheet(song : Song){
        SongActionSheet(
            song = song,
            onQueue = {
                songViewModel.addToNextQueue(song)
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


        homeViewModel.allSongs.observe(viewLifecycleOwner){ songs ->
            songs.forEach { song ->
                Log.d("VALEN GANTENG", "${song.title} | Owner : ${song.ownerId}")
            }
        }

        homeViewModel.newSongs.observe(viewLifecycleOwner) { songs ->
            newSongsAdapter.updateSongs(songs)
        }

        homeViewModel.recentlyPlayed.observe(viewLifecycleOwner) { songs ->
            recentlyPlayedAdapter.submitList(songs)
        }
    }
}
