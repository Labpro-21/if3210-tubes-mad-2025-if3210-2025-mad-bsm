package com.mad.besokminggu.ui.topSongCapsule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.TopSongCapsule
import com.mad.besokminggu.databinding.FragmentTopsongCapsuleBinding
import com.mad.besokminggu.ui.adapter.TopSongCapsuleAdapter

class TopSongCapsuleFragment : Fragment() {

    private lateinit var binding: FragmentTopsongCapsuleBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTopsongCapsuleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dummySongs = listOf(
            TopSongCapsule("01", "Blinding Lights", "The Weeknd", R.drawable.cover_blonde),
            TopSongCapsule("02", "Come Together", "Beatles", R.drawable.cover_blonde),
            TopSongCapsule("03", "Stronger", "Kanye West", R.drawable.cover_blonde),
            TopSongCapsule("04", "Persuasive", "Doechii", R.drawable.cover_blonde),
            TopSongCapsule("05", "Persuasive", "Doechii", R.drawable.cover_blonde)
        )
        val top5 = dummySongs.take(5)

        binding.recyclerTopSongs.adapter = TopSongCapsuleAdapter(top5)
        binding.titleCapsule.text = "You listened to ${top5.size} songs this month."


        binding.recyclerTopSongs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TopSongCapsuleAdapter(dummySongs)
        }
    }
}
