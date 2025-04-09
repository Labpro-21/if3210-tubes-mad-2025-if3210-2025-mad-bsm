package com.mad.besokminggu.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mad.besokminggu.adapter.SongAdapter
import com.mad.besokminggu.databinding.FragmentLibraryBinding
import com.mad.besokminggu.ui.addsongs.AddSongsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val libraryViewModel =
            ViewModelProvider(this)[LibraryViewModel::class.java]

        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        songAdapter = SongAdapter()
        binding.songListRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }

        libraryViewModel.songs.observe(viewLifecycleOwner) { songList ->
            songAdapter.submitList(songList)
        }

        // Open add songs sheet on clicking "add songs" button
        binding.addButton.setOnClickListener {
            val existingFragment = parentFragmentManager.findFragmentByTag("AddSongsBottomSheet")
            if (existingFragment == null) {
                val bottomSheet = AddSongsFragment()
                bottomSheet.show(parentFragmentManager, "AddSongsBottomSheet")
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}