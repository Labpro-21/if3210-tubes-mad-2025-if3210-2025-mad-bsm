package com.mad.besokminggu.ui.library

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.ui.adapter.SongAdapter
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
        songViewModel.resetPrevQueue();
        songViewModel.playSong(song);
        songViewModel.showFullPlayer();
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
            songAdapter.submitList(songList)
        }

        binding.addButton.setOnClickListener {
            val existingFragment = parentFragmentManager.findFragmentByTag("AddSongsBottomSheet")
            if (existingFragment == null) {
                AddSongsFragment().show(parentFragmentManager, "AddSongsBottomSheet")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}