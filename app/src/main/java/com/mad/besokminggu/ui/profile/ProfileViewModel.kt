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
import com.mad.besokminggu.data.model.PlayedSongDate
import com.mad.besokminggu.data.model.StreakInfo
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

    private val _hasCurrentMonthStreak = MutableLiveData<Boolean>(false)
    val hasCurrentMonthStreak: LiveData<Boolean> = _hasCurrentMonthStreak

    private val _streakInfo = MutableLiveData<StreakInfo?>()
    val streakInfo: LiveData<StreakInfo?> get() = _streakInfo


    fun checkStreakDummy() {
        _hasCurrentMonthStreak.value = true
    }

    private fun Date.toLocalDate(): java.time.LocalDate {
        return this.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    }



    private fun getCurrentMonthKey(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
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

                val topSongObj = repository.getTopSongByMonth(ownerId, monthKey)
                val topSongTitle = topSongObj?.title ?: ""
                val topSongCover = topSongObj?.coverFileName ?: ""
                val topArtist = repository.getTopArtistByMonth(ownerId, monthKey)
                val topArtistCover = topArtist?.let {
                    repository.getTopArtistCover(ownerId, it)
                }


                MonthlySummaryCapsule(
                    month = formatMonthFromKey(monthKey),
                    totalMinutes = totalMinutes,
                    topSong = topSongTitle,
                    topArtist = topArtist,
                    topSongCover = topSongCover,
                    topArtistCover = topArtistCover
                )

            }

            withContext(Dispatchers.Main) {
                _monthlySummaries.value = summaries
                _monthlySummaryMap.value = summaries.associateBy { it.month }
            }
        }
    }

    fun loadDummyStreakInfo() {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val dummyStreak = StreakInfo(
            startDate = formatter.parse("2025-05-19")!!,
            endDate = formatter.parse("2025-05-24")!!,
            streakLength = 6,
            streakSongTitle = "Loose",
            streakSongArtist = "Daniel Caesar",
            coverFileName = "cover_streak.jpg"
        )

        _streakInfo.postValue(dummyStreak)
        _hasCurrentMonthStreak.postValue(true)
    }

    fun loadStreakInfo() {
        viewModelScope.launch {
            val profile = tokenManager.getUserProfile() ?: return@launch
            val info = repository.getStreakInfoForCurrentMonth(profile.id)
            _streakInfo.postValue(info)
            _hasCurrentMonthStreak.postValue(info != null)
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
        loadStreakInfo()
    }




}


