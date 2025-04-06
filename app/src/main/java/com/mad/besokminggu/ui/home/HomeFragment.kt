package com.mad.besokminggu.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mad.besokminggu.R


class HomeFragment : Fragment() {

    private lateinit var newSongsAdapter: NewSongAdapter


    private lateinit var rvNewSongs: RecyclerView
    private lateinit var rvRecentlyPlayed: RecyclerView

    private val viewModel: HomeViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvNewSongs = view.findViewById(R.id.rvNewSongs)
        rvRecentlyPlayed = view.findViewById(R.id.rvRecentlyPlayed)

        newSongsAdapter = NewSongAdapter(emptyList())
        val recentlyPlayedAdapter = RecentlyAdapter()

        rvNewSongs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvNewSongs.adapter = newSongsAdapter

        rvRecentlyPlayed.layoutManager = LinearLayoutManager(requireContext())
        rvRecentlyPlayed.adapter = recentlyPlayedAdapter

        viewModel.newSongs.observe(viewLifecycleOwner) { songs ->
            newSongsAdapter.updateSongs(songs)
        }

        viewModel.recentlyPlayed.observe(viewLifecycleOwner) { songs ->
            recentlyPlayedAdapter.submitList(songs)
        }
    }
}
