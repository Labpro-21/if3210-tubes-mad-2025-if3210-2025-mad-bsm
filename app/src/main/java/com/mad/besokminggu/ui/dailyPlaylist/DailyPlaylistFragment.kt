package com.mad.besokminggu.ui.dailyPlaylist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mad.besokminggu.R
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.ui.adapter.DailyPlaylistAdapter
import com.mad.besokminggu.ui.topSongs.TopSongsViewModel
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.SongTracksViewModel
import com.mad.besokminggu.viewModels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DailyPlaylistFragment : Fragment() {

    private val dailyPlaylistViewModel: DailyPlaylistViewModel by activityViewModels()
    private val songTracksViewModel: SongTracksViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val topSongsViewModel: TopSongsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_daily_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvDailyPlaylistSongs)
        val dateText = view.findViewById<TextView>(R.id.tvDate)
        val durationText = view.findViewById<TextView>(R.id.tvTotalDuration)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = DailyPlaylistAdapter { playlist ->
            lifecycleScope.launch {
                songTracksViewModel.playSong(playlist.song, playlist.isOnline)
            }
        }
        recyclerView.adapter = adapter

        dailyPlaylistViewModel.dailyPlaylist.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Observe user profile to fetch local songs
        userViewModel.profile.observe(viewLifecycleOwner) { user ->
            topSongsViewModel.getTopSongsGlobal(
                coroutinesErrorHandler = object : CoroutinesErrorHandler {
                    override fun onError(message: String) {
                        Log.e("TopGlobalFragment", "Error: ${message}")
                    }
                },
            )
            topSongsViewModel.topSongs.observe(viewLifecycleOwner) { topSongRes ->
                if (topSongRes is ApiResponse.Success) {
                    dailyPlaylistViewModel.loadLocalSongsAndGenerate(
                        ownerId = user.id,
                        topSongsResponse = topSongRes
                    )
                }
            }
        }

        dailyPlaylistViewModel.dailyPlaylist.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        dailyPlaylistViewModel.playlistDuration.observe(viewLifecycleOwner) {
            durationText.text = it
        }

        dateText.text = dailyPlaylistViewModel.getTodayLabel()
    }
}
