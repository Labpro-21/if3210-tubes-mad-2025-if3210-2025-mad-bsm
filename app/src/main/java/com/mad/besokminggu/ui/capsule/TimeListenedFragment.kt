package com.mad.besokminggu.ui.capsule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mad.besokminggu.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import android.graphics.Color
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimeListenedFragment : Fragment() {

    private val viewModel: TimeListenedViewModel by viewModels()

    private lateinit var chart: LineChart
    private lateinit var totalText: TextView
    private lateinit var avgText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_time_listened, container, false)
        chart = view.findViewById(R.id.line_chart)
        totalText = view.findViewById(R.id.text_main_sentence)
        avgText = view.findViewById(R.id.text_daily_average)

        viewModel.loadDataForCurrentMonth()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.totalMinutes.observe(viewLifecycleOwner) { total ->
            totalText.text = "You listened to music for $total minutes this month."
        }

        view.findViewById<ImageButton>(R.id.button_back).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewModel.minutesPerWeek.observe(viewLifecycleOwner) { list ->
            val avg = if (list.isNotEmpty()) list.sum() / list.size else 0
            avgText.text = "Weekly average: $avg min"
            setupChart(list)
        }
    }

    private fun setupChart(dailyData: List<Int>) {
        val entries = dailyData.mapIndexed { index, minutes ->
            Entry((index + 1).toFloat(), minutes.toFloat())
        }

        val dataSet = LineDataSet(entries, "Minutes per Day").apply {
            lineWidth = 2f
            color = Color.GREEN
            valueTextColor = Color.WHITE
            setCircleColor(Color.WHITE)
            circleRadius = 3f
            valueTextSize = 10f
        }

        chart.apply {
            data = LineData(dataSet)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.WHITE
                granularity = 1f
                labelRotationAngle = -45f
            }
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            legend.textColor = Color.WHITE
            description.isEnabled = false
            invalidate()
        }
    }
}


