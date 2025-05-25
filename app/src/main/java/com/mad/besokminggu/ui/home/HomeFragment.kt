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
import com.mad.besokminggu.data.repositories.OnlineSongRepository
import com.mad.besokminggu.databinding.FragmentHomeBinding

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val songViewModel : SongTracksViewModel by activityViewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onSongClick(song: Song) {
        if (song.id != songViewModel.playedSong.value?.id) {
            lifecycleScope.launch {
                songViewModel.playSong(song)
            }
        }
        songViewModel.showFullPlayer()
    }

    private fun onOpenSheet(song: Song) {
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
                    val editFragment = AddSongsFragment().apply {
                        arguments = Bundle().apply {
                            putBoolean("isEditMode", true)
                            putInt("songID", song.id)
                            putString("songTitle", song.title)
                            putString("artistName", song.artist)
                            putString("songFilePath", song.audioFileName)
                            putString("songImagePath", song.coverFileName)
                        }
                    }
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

        val newSongsAdapter = NewSongAdapter { onSongClick(it) }
        val recentlyPlayedAdapter = SongWithMenuAdapter(
            onItemClick = { onSongClick(it) },
            onMenuClick = { onOpenSheet(it) }
        )
        val dailyAdapter = SongWithMenuAdapter(
            onItemClick = { onSongClick(it) },
            onMenuClick = { onOpenSheet(it) }
        )

        binding.rvNewSongs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvNewSongs.adapter = newSongsAdapter

        binding.rvRecentlyPlayed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentlyPlayed.adapter = recentlyPlayedAdapter

        binding.rvDailyPlaylist?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvDailyPlaylist?.adapter = dailyAdapter

        binding.topGlobal?.setOnClickListener {
            val bundle = bundleOf("isGlobal" to true)
            findNavController().navigate(R.id.navigation_top_songs, bundle)
        }

        binding.topLocal?.setOnClickListener {
            val bundle = bundleOf("isGlobal" to false)
            findNavController().navigate(R.id.navigation_top_songs, bundle)
        }

        userViewModel.profile.observe(viewLifecycleOwner) { profile ->
            homeViewModel.refreshSong(profile.id)

            homeViewModel.allSongs.observe(viewLifecycleOwner) { songs ->
                homeViewModel._newSongs.postValue(songs.filter { it.lastPlayedAt == null })
                homeViewModel._recentlyPlayed.postValue(songs.filter { it.lastPlayedAt != null }.sortedByDescending { it.lastPlayedAt })

                homeViewModel.topSongs.observe(viewLifecycleOwner) { onlineSongs ->
                    homeViewModel.generateDailyPlaylist(songs, onlineSongs)
                }
            }
        }

        homeViewModel.newSongs.observe(viewLifecycleOwner) {
            newSongsAdapter.updateSongs(it)
        }

        homeViewModel.recentlyPlayed.observe(viewLifecycleOwner) {
            recentlyPlayedAdapter.submitList(it)
        }

        homeViewModel.dailyPlaylist.observe(viewLifecycleOwner) {
            dailyAdapter.submitList(it.take(5)) // preview 5 lagu
        }

        homeViewModel.dailyPlaylistDuration.observe(viewLifecycleOwner) { duration ->
            binding.tvDuration!!.text  = duration
        }
    }
}
