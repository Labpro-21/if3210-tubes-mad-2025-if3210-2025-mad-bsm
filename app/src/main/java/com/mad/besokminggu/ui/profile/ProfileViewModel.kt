package com.mad.besokminggu.ui.profile

import MonthlySummaryCapsule
import android.se.omapi.Session
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.MonthlyTopSongsData
import com.mad.besokminggu.data.model.TopSongCapsule
import com.mad.besokminggu.data.repositories.SongRepository
import com.mad.besokminggu.network.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: SongRepository,
    private val tokenManager: SessionManager
) : ViewModel() {

    private val _likedSongsCount = MutableLiveData<Int>()
    val likedSongsCount: LiveData<Int> = _likedSongsCount

    private val _songsCount = MutableLiveData<Int>()
    val songsCount: LiveData<Int> = _songsCount

    private val _listenedSongsCount = MutableLiveData<Int>()
    val listenedSongsCount: LiveData<Int> = _listenedSongsCount

    private val _monthlyTopSongs = MutableLiveData<Map<String, MonthlyTopSongsData>>()
    val monthlyTopSongs: LiveData<Map<String, MonthlyTopSongsData>> = _monthlyTopSongs

    private val _currentListeningSeconds = MutableLiveData<Int>(0)
    val currentListeningSeconds: LiveData<Int> get() = _currentListeningSeconds

    private val _listenedSecondsThisMonth = MutableLiveData(0)
    val listenedSecondsThisMonth: LiveData<Int> get() = _listenedSecondsThisMonth

    private var previousMonthKey: String = getCurrentMonthKey()

    fun refreshCurrentMonthSummary() {
        val list = _monthlySummaries.value?.toMutableList() ?: return
        val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        val index = list.indexOfFirst { it.month == currentMonth }

        if (index != -1) {
            val old = list[index]
            val newMinutes = (_listenedSecondsThisMonth.value ?: 0) / 60

            if (old.totalMinutes != newMinutes) {
                val updated = old.copy(totalMinutes = newMinutes)
                list[index] = updated
                _monthlySummaries.value = list
            }
        }
    }


    private fun getCurrentMonthKey(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    fun ensureMonthKeyFresh() {
        val currentKey = getCurrentMonthKey()
        if (currentKey != previousMonthKey) {
            _listenedSecondsThisMonth.value = 0
            previousMonthKey = currentKey
        }
    }

    fun incrementListeningTime() {
        _currentListeningSeconds.value = (_currentListeningSeconds.value ?: 0) + 1
        _listenedSecondsThisMonth.value = (_listenedSecondsThisMonth.value ?: 0) + 1
        refreshCurrentMonthSummary()
    }


    fun updateMonthlySummaries(newList: List<MonthlySummaryCapsule>) {
        _monthlySummaries.value = newList
    }


    fun loadCounts() {
        viewModelScope.launch {
            val profile = tokenManager.getUserProfile()

            // Observe each count and update the corresponding MutableLiveData
            repository.getLikedSongsCount(profile?.id ?: -1).observeForever { count ->
                _likedSongsCount.postValue(count)
            }

            repository.getTotalSongsCount(profile?.id ?: -1).observeForever { count ->
                _songsCount.postValue(count)
            }

            repository.getListenedSongsCount(profile?.id ?: -1).observeForever { count ->
                _listenedSongsCount.postValue(count)
            }
        }
    }

    fun setMonthlyTopSongs(data: Map<String, MonthlyTopSongsData>) {
        _monthlyTopSongs.value = data
    }

    private fun loadDummyMonthlyTopSongs() {
        val dummy = mapOf(
            "April 2025" to MonthlyTopSongsData(
                totalPlayed = 203,
                topSongs = listOf(
                    TopSongCapsule("01", "Starboy", "The Weeknd, Daft Punk", R.drawable.cover_starboy, 15),
                    TopSongCapsule("02", "Blinding Lights", "The Weeknd", R.drawable.cover_starboy, 12),
                    TopSongCapsule("03", "Save Your Tears", "The Weeknd", R.drawable.cover_starboy, 8)
                )
            )
        )
        _monthlyTopSongs.value = dummy
    }

    fun formatMonthFromKey(key: String): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val output = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return try {
            val date = sdf.parse(key)
            output.format(date!!)
        } catch (e: Exception) {
            key
        }
    }



    fun loadCapsuleSummary() {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = tokenManager.getUserProfile() ?: return@launch
            val ownerId = profile.id

            val months = repository.getRecentMonthsWithPlayback(ownerId).toMutableList()
            val currentMonthKey = getCurrentMonthKey()
            if (!months.contains(currentMonthKey)) {
                months.add(currentMonthKey)
            }

            val summaries = months.map { monthKey ->
                val totalMinutes = repository.getTotalPlayedDurationByMonth(ownerId, monthKey)?.div(60) ?: 0

                val topSong = repository.getTopSongByMonth(ownerId, monthKey)
                val topArtist = repository.getTopArtistByMonth(ownerId, monthKey)

                MonthlySummaryCapsule(
                    month = formatMonthFromKey(monthKey),
                    totalMinutes = totalMinutes,
                    topSong = topSong,
                    topArtist = topArtist,
                    topSongImageRes = R.drawable.cover_starboy,
                    topArtistImageRes = R.drawable.cover_blonde
                )
            }

            withContext(Dispatchers.Main) {
                _monthlySummaries.value = summaries
                _monthlySummaryMap.value = summaries.associateBy { it.month }
            }
        }
    }



    // For RecyclerView
    private val _monthlySummaries = MutableLiveData<List<MonthlySummaryCapsule>>()
    val monthlySummaries: LiveData<List<MonthlySummaryCapsule>> = _monthlySummaries

    // For individual lookup by monthLabel
    private val _monthlySummaryMap = MutableLiveData<Map<String, MonthlySummaryCapsule>>()
    val monthlySummaryMap: LiveData<Map<String, MonthlySummaryCapsule>> = _monthlySummaryMap

    init {
        loadCapsuleSummary()

    }




}


