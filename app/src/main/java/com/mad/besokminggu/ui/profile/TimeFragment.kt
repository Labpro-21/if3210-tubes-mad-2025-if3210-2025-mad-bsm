package com.mad.besokminggu.ui.profile

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mad.besokminggu.databinding.FragmentTimeListenedBinding

class TimeListenedFragment : Fragment() {

    private var _binding: FragmentTimeListenedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeListenedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol back
        binding.buttonBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Siapkan span
        val totalMinutes = "862 minutes"
        val prefix       = "You listened to music for "
        val suffix       = " this month."

        val ssb = SpannableStringBuilder()
            .append(prefix)
        val start = ssb.length
        ssb.append(totalMinutes)
        ssb.setSpan(
            ForegroundColorSpan(Color.parseColor("#00E676")),
            start, start + totalMinutes.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssb.append(suffix)

        // Tampilkan
        binding.textMainSentence.text = ssb
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
