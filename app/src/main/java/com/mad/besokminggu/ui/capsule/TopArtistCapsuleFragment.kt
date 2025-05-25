package com.mad.besokminggu.ui.capsule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mad.besokminggu.databinding.FragmentTopartistCapsuleBinding
import com.mad.besokminggu.ui.adapter.TopArtistCapsuleAdapter
import com.mad.besokminggu.ui.profile.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class TopArtistCapsuleFragment : Fragment() {

    private lateinit var binding: FragmentTopartistCapsuleBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTopartistCapsuleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val adapter = TopArtistCapsuleAdapter()
        binding.recyclerTopArtists.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTopArtists.adapter = adapter
        val formatter = java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val currentMonth = formatter.format(Date())

        profileViewModel.topArtists.observe(viewLifecycleOwner) { artists ->
            adapter.submitList(artists)
            binding.textMonth.text = currentMonth

            binding.titleCapsule.text = "You listened to ${artists.size} artists this month."
        }

        profileViewModel.loadTopArtistsForCurrentMonth()
    }
}
