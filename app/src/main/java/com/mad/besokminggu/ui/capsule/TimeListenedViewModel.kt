package com.mad.besokminggu.ui.capsule

import java.util.Date
import java.util.Locale
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.besokminggu.data.repositories.SongRepository
import com.mad.besokminggu.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject



@HiltViewModel
class TimeListenedViewModel @Inject constructor(
    private val repository: SongRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val totalMinutes = MutableLiveData<Int>()

    val minutesPerWeek = MutableLiveData<List<Int>>()

    fun loadDataForCurrentMonth() {
        val monthLabel = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            val profile = sessionManager.getUserProfile() ?: return@launch
            val ownerId = profile.id

            val total = repository.getTotalPlayedSongCount(ownerId, monthLabel)
            totalMinutes.postValue(total)

            val raw = repository.getPlayedMinutesPerDay(ownerId, monthLabel)
            val weeklyBuckets = MutableList(5) { 0 }

            raw?.forEach { day ->
                val dayInt = day.day.toInt()
                val weekIndex = (dayInt - 1) / 7
                if (weekIndex in 0..4) {
                    weeklyBuckets[weekIndex] += day.minutes
                }
            }

            minutesPerWeek.postValue(weeklyBuckets)
        }
    }


}



