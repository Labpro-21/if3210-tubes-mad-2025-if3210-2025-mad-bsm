package com.mad.besokminggu.ui.capsule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mad.besokminggu.databinding.FragmentTopsongCapsuleBinding
import com.mad.besokminggu.ui.adapter.TopSongCapsuleAdapter
import com.mad.besokminggu.ui.profile.ProfileViewModel

class TopSongCapsuleFragment : Fragment() {

    companion object {
        private const val ARG_MONTH_LABEL = "ARG_MONTH_LABEL"

        fun newInstance(monthLabel: String): TopSongCapsuleFragment {
            return TopSongCapsuleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MONTH_LABEL, monthLabel)
                }
            }
        }
    }

    private lateinit var binding: FragmentTopsongCapsuleBinding

    private val sharedVm: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentTopsongCapsuleBinding
        .inflate(inflater, container, false).also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val monthLabel = requireArguments().getString("ARG_MONTH_LABEL")!!

        sharedVm.monthlyTopSongs.observe(viewLifecycleOwner) { map ->
            val data = map[monthLabel]

            binding.textMonth.text = monthLabel
            binding.titleCapsule.text = "You played ${data?.totalPlayed ?: 0} different songs this month."

            binding.recyclerTopSongs.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = TopSongCapsuleAdapter(data?.topSongs ?: emptyList())
            }
        }

    }
}

