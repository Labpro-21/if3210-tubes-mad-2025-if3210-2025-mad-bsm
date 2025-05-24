package com.mad.besokminggu.ui.capsule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mad.besokminggu.databinding.FragmentTopsongCapsuleBinding
import com.mad.besokminggu.ui.adapter.TopSongCapsuleAdapter
import com.mad.besokminggu.ui.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class TopSongCapsuleFragment : Fragment() {

    private lateinit var binding: FragmentTopsongCapsuleBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentTopsongCapsuleBinding
        .inflate(inflater, container, false).also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

        val adapter = TopSongCapsuleAdapter()
        binding.recyclerTopSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTopSongs.adapter = adapter

        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewModel.loadTopSongsForCurrentMonth()

        val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        viewModel.monthlyTopSongs.observe(viewLifecycleOwner) { map ->
            val data = map[monthKey]
            val displayMonth = viewModel.formatMonthFromKey(monthKey)

            binding.textMonth.text = displayMonth
            binding.titleCapsule.text = "You played ${data?.totalPlayed ?: 0} different songs this month."
            adapter.submitList(data?.topSongs ?: emptyList())
        }

    }

}

