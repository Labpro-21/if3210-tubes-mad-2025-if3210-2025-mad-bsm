package com.mad.besokminggu.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mad.besokminggu.databinding.FragmentCapsuleBinding
import com.mad.besokminggu.ui.profile.ProfileViewModel

class MonthlyCapsuleFragment : Fragment() {

    companion object {
        private const val ARG_MONTH_LABEL = "ARG_MONTH_LABEL"

        fun newInstance(monthLabel: String): MonthlyCapsuleFragment {
            return MonthlyCapsuleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MONTH_LABEL, monthLabel)
                }
            }
        }
    }

    private lateinit var binding: FragmentCapsuleBinding
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCapsuleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val monthLabel = requireArguments().getString(ARG_MONTH_LABEL) ?: return

        profileViewModel.monthlySummaryMap.observe(viewLifecycleOwner) { map ->
            val summary = map[monthLabel]

            if (summary == null) {

                binding.textMonthYear.text = monthLabel
                binding.textMinutes.text = "No data available"
                binding.textTopArtist.visibility = View.GONE
                binding.textTopSong.visibility = View.GONE
                binding.imageArtist.visibility = View.GONE
                binding.imageSong.visibility = View.GONE
            } else {
                binding.textMonthYear.text = summary.month
                binding.textMinutes.text = "${summary.totalMinutes} minutes"
                binding.textTopArtist.text = summary.topArtist
                binding.textTopSong.text = summary.topSong
                summary.topArtistImageRes?.let { binding.imageArtist.setImageResource(it) }
                summary.topSongImageRes?.let { binding.imageSong.setImageResource(it) }


                binding.textTopArtist.visibility = View.VISIBLE
                binding.textTopSong.visibility = View.VISIBLE
                binding.imageArtist.visibility = View.VISIBLE
                binding.imageSong.visibility = View.VISIBLE
            }
        }
    }

}
