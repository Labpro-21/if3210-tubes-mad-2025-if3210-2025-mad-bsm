package com.mad.besokminggu.data.repositories

import android.health.connect.datatypes.units.Length
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.mad.besokminggu.R
import com.mad.besokminggu.data.dao.SongDao
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.model.StreakInfo
import com.mad.besokminggu.data.model.TopArtistCapsule
import com.mad.besokminggu.data.model.TopSongCapsule
import com.mad.besokminggu.viewModels.UserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class SongRepository @Inject constructor(private val songDao: SongDao) {

    private val _allSongs = MutableLiveData<LiveData<List<Song>>>()
    val allSongs: LiveData<List<Song>> get() = _allSongs.value ?: MutableLiveData()

    fun getAllSongs(ownerId: Int): LiveData<List<Song>> {
        return songDao.getAllSongs(ownerId)
    }

    suspend fun insert(song: Song) {
        songDao.insert(song)
    }

    suspend fun update(song: Song){
        songDao.update(song);
    }

    suspend fun deleteAllSongs() {
        songDao.deleteAll()
    }

    fun getSong(id : Int) : LiveData<Song> {
        return songDao.getSong(id);
    }

    fun getLikedSongsCount(ownerId: Int) : LiveData<Int> {
        return songDao.getLikedSongsCount(ownerId)
    }

    fun getTotalSongsCount(ownerId: Int) : LiveData<Int>{
        return songDao.getSongsCount(ownerId)
    }

    fun getListenedSongsCount(ownerId: Int) : LiveData<Int>{
        return songDao.getListenedSongsCount(ownerId)
    }

    suspend fun deleteSong(song: Song){
        songDao.delete(song)
    }

    suspend fun getNextIteratedSong(currentSong : Song, ownerId: Int) : Song{
        val nextSong = songDao.getNextIdSong(currentSong.id, ownerId)
        return nextSong ?: songDao.getFirstSong(ownerId)
    }

    suspend fun getNextRandomSong(currentSong: Song, ownerId: Int): Song {
        return songDao.getRandomSongExcluding(currentSong.id, ownerId) ?: songDao.getFirstSong(ownerId)
    }

    suspend fun getAllPlayedSongs(ownerId: Int): List<Song> {
        return songDao.getAllPlayedSongs(ownerId)
    }

    suspend fun getRecentMonthsWithPlayback(ownerId: Int): List<String> {
        return songDao.getRecentMonthsWithPlayback(ownerId)
    }

    suspend fun getTotalPlayedDurationByMonth(ownerId: Int, monthKey: String): Int? {
        return songDao.getTotalPlayedDurationByMonth(ownerId, monthKey)
    }

    suspend fun getTopArtistByMonth(ownerId: Int, monthKey: String): String? {
        return songDao.getTopArtistByMonth(ownerId, monthKey)
    }


    suspend fun incrementPlayedTime(songId: Int, seconds: Int, lastPlayedAt: Date) {
        songDao.incrementPlayedTime(songId, seconds, lastPlayedAt)
    }

    suspend fun getTopSongByMonth(ownerId: Int, month: String): Song? {
        return songDao.getTopSongByMonth(ownerId, month)
    }

    suspend fun getTopArtistCover(ownerId: Int, artist: String): String? {
        return songDao.getTopArtistCover(ownerId, artist)
    }

    suspend fun getStreakInfoForCurrentMonth(ownerId: Int): StreakInfo? {
        val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val rawSongs = songDao.getPlayedSongsByDate(ownerId, monthKey)


        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sorted = rawSongs.sortedBy { sdf.parse(it.playedDate) }

        var maxStreak = 1
        var currentStreak = 1
        var bestStart = sdf.parse(sorted[0].playedDate)!!
        var bestEnd = bestStart
        var tempStart = bestStart
        var prevDate = bestStart
        var bestSong = sorted[0]

        for (i in 1 until sorted.size) {
            val currentDate = sdf.parse(sorted[i].playedDate)!!
            val diff = ((currentDate.time - prevDate.time) / (1000 * 60 * 60 * 24)).toInt()

            if (diff == 1) {
                currentStreak++
            } else if (diff > 1) {
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                    bestStart = tempStart
                    bestEnd = prevDate
                    bestSong = sorted[i - 1]
                }
                currentStreak = 1
                tempStart = currentDate
            }

            prevDate = currentDate
        }

        // Check terakhir
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak
            bestStart = tempStart
            bestEnd = prevDate
            bestSong = sorted.last()
        }

        return if (maxStreak >= 2) {

            val coverFile = songDao.getTopArtistCover(ownerId, bestSong.artist) ?: ""
            StreakInfo(
                startDate = bestStart,
                endDate = bestEnd,
                streakLength = maxStreak,
                streakSongTitle = bestSong.title,
                streakSongArtist = bestSong.artist,
                coverFileName = coverFile
            )
        } else {
            null
        }
    }

    suspend fun getTopArtists(ownerId: Int, month: String): List<TopArtistCapsule> {
        return songDao.getTopArtistsRaw(ownerId, month).map {
            TopArtistCapsule(name = it.name, coverFileName = it.coverFileName ?: "")
        }
    }

    suspend fun getTopSongsForMonth(ownerId: Int, month: String): List<TopSongCapsule> {
        val raw = songDao.getTopSongsByMonth(ownerId, month)

        return raw.map { song ->
            TopSongCapsule(
                title = song.title,
                artist = song.artist,
                coverFileName = song.coverFileName,
                playCount = song.playCount
            )
        }
    }

    suspend fun getTotalPlayedSongCount(ownerId: Int, monthYear: String): Int {
        return songDao.getTotalPlayedSongCount(ownerId, monthYear)
    }





}
