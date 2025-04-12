package com.mad.besokminggu.ui.library

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song

import com.mad.besokminggu.databinding.FragmentLibraryBinding
import com.mad.besokminggu.ui.adapter.SongWithMenuAdapter
import com.mad.besokminggu.ui.addsongs.AddSongsFragment
import com.mad.besokminggu.ui.optionMenu.SongActionSheet
import com.mad.besokminggu.viewModels.SongTracksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val songViewModel : SongTracksViewModel by activityViewModels()
    private val libraryViewModel : LibraryViewModel by activityViewModels()

    private lateinit var songAdapter: SongWithMenuAdapter

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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        songAdapter = SongWithMenuAdapter(
            onItemClick = { song -> onSongClick(song) },
            onMenuClick = { song -> onOpenSheet(song) }
        )

        binding.songListRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }

        libraryViewModel.songs.observe(viewLifecycleOwner) { songList ->
            Log.d("VALEN LIBRARY", "onViewCreated: $songList")
            songList.forEach {
                Log.d("VALEN LIBRARY", "onViewCreated: ${it.title} | ${it.artist} | ${it.ownerId}")
            }
            songAdapter.submitList(songList)
        }

        libraryViewModel.filteredSongs.observe(viewLifecycleOwner) { songs ->
            songAdapter.submitList(songs ?: emptyList())
        }

        libraryViewModel.filterSongs(showLikedOnly = false)

        binding.addButton.setOnClickListener {
            val existingFragment = parentFragmentManager.findFragmentByTag("AddSongsBottomSheet")
            if (existingFragment == null) {
                AddSongsFragment().show(parentFragmentManager, "AddSongsBottomSheet")
            }
        }

        binding.btnAll.setOnClickListener {
            libraryViewModel.filterSongs(showLikedOnly = false)
            setButtonSelected(binding.btnAll, binding.btnLiked)
        }

        binding.btnLiked.setOnClickListener {
            libraryViewModel.filterSongs(showLikedOnly = true)
            setButtonSelected(binding.btnLiked, binding.btnAll)
        }

        binding.searchBar!!.doOnTextChanged { text, _, _, _ ->
            libraryViewModel.searchSongs(text.toString())
        }
    }

    private fun setButtonSelected(selected: Button, other: Button) {
        selected.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green)
        selected.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.black))
        other.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.disabled_2)
        other.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.white))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}