package com.mad.besokminggu.ui.topSongs

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mad.besokminggu.data.model.OnlineSong
import com.mad.besokminggu.data.model.toSong
import com.mad.besokminggu.databinding.FragmentTopSongsBinding
import com.mad.besokminggu.manager.SongDownloadHelper
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.ui.adapter.OnlineSongAdapter
import com.mad.besokminggu.ui.optionMenu.OnlineSongActionSheet
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.SongTracksViewModel
import com.mad.besokminggu.viewModels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import com.mad.besokminggu.R

@AndroidEntryPoint
class TopSongsFragment: Fragment() {

    private var _binding: FragmentTopSongsBinding? = null

    private val binding get() = _binding!!

    private val songViewModel : SongTracksViewModel by activityViewModels()
    private val userViewModel : UserViewModel by activityViewModels()
    private val topSongsViewModel: TopSongsViewModel by activityViewModels()

    private lateinit var songAdapter: OnlineSongAdapter

    private fun onSongClick(song: OnlineSong){
        Log.d("MiniPlayer", "Song playing: ${song.title}")
        if(song.id != songViewModel.playedSong.value?.id){
            lifecycleScope.launch {

                songViewModel.playSong(song.toSong(), isOnline = true)
            }
        }
        songViewModel.showFullPlayer()
    }

    private fun onOpenSheet(song : OnlineSong){
        OnlineSongActionSheet(
            song = song,
            onDownload = { downloadSong(song) }
        ).show(parentFragmentManager, "SongActionSheet")
    }

    private fun downloadAll() {
        val allSongs = topSongsViewModel.topSongs.value
        if (allSongs is ApiResponse.Success) {
            val allSongsData = allSongs.data
            allSongsData.forEach { song ->
                downloadSong(song, false)
            }
            if (allSongsData.isNotEmpty()) {
                Toast.makeText(requireContext(), "All songs have been downloaded", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No songs to download", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isGlobal = arguments?.getBoolean("isGlobal") ?: false

        val back = binding.backButton
        val rv = binding.songListRecyclerView
        val playButton = binding.playButton
        val downloadButton = binding.downloadButton
        val duration = binding.TotalMin
        val month = binding.Date

        val background = binding.background
        val textDescription = binding.textDescription
        val coverImage = binding.coverImage

        if (isGlobal) {
            background.setBackgroundResource(R.drawable.background_gradient)
            textDescription.setText(R.string.desc_top_global)
            coverImage.setImageResource(R.drawable.cover_top_global)
        } else {
            background.setBackgroundResource(R.drawable.background_gradient_red)
            textDescription.setText(R.string.desc_top_local)
            coverImage.setImageResource(R.drawable.cover_top_local)
        }


        back.setOnClickListener {
            findNavController().popBackStack()
        }

        downloadButton.setOnClickListener {
            downloadAll()
        }

        songAdapter = OnlineSongAdapter(
            onItemClick = { song -> onSongClick(song) },
            onMenuClick = { song -> onOpenSheet(song) }
        )

        rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }

        playButton.setOnClickListener {
            val currentSong = songViewModel.playedSong.value
            val firstSong = songAdapter.songs.first()
            if (currentSong == null) {
                lifecycleScope.launch {
                    songViewModel.playSong(firstSong.toSong(), isOnline = true)
                }
            }
            songViewModel.showFullPlayer()
        }

        topSongsViewModel.topSongs.observe(viewLifecycleOwner) { songList ->
            when (songList) {
                is ApiResponse.Loading -> {
//                    binding.progressBar?.visibility = View.VISIBLE
                }
                is ApiResponse.Success -> {
                    songAdapter.submitList(songList.data)
                    topSongsViewModel.updateSongsRepo(songList.data)
                    topSongsViewModel.updateTotalDuration()
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

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)
        val formattedDate = currentDate.format(formatter)

        month.text = formattedDate

        topSongsViewModel.totalDuration.observe(viewLifecycleOwner) {
            Log.d("TopGlobalFragment", "Total Duration: $it")
            val hour = it / 3600
            val min = it / 60 % 60

            val string = String.format("%dh %dmin", hour, min)

            duration.text = string
        }



        if (isGlobal) {
            topSongsViewModel.getTopSongsGlobal(
                coroutinesErrorHandler = object : CoroutinesErrorHandler {
                    override fun onError(message: String) {
                        Log.e("TopGlobalFragment", "Error: ${message}")
                    }
                },
            )
        } else {
            topSongsViewModel.getTopSongsCountry(
                country = userViewModel.profile.value?.location ?: "ID",
                coroutinesErrorHandler = object : CoroutinesErrorHandler {
                    override fun onError(message: String) {
                        Log.e("TopGlobalFragment", "Error: ${message}")
                    }
                },
            )
        }
    }

    private fun downloadSong(song: OnlineSong, showToast: Boolean = true) {
        Log.d("TopGlobalFragment", "Download song: ${song.title}")


        val songPath = SongDownloadHelper.downloadFile(
            url = song.url,
            subDir = "audio",
            ext = song.url.substringAfterLast("."),
        )

        Log.d("TopGlobalFragment", "Downloaded Song path: $songPath")

        val coverPath = SongDownloadHelper.downloadFile(
            url = song.artwork,
            subDir = "cover",
            ext = song.artwork.substringAfterLast("."),
        )

        Log.d("TopGlobalFragment", "Downloaded Cover path: $coverPath")

        // Insert to DB
        val newSongs = song.toSong().copy(
            ownerId = userViewModel.profile.value?.id ?: -1,
            audioFileName = songPath,
            coverFileName = coverPath,
            createdAt = Date(),
        )
        topSongsViewModel.insertSong(newSongs)

        if (showToast)
            Toast.makeText(requireContext(), "Song ${song.title} has been downloaded", Toast.LENGTH_SHORT).show()
    }

}
