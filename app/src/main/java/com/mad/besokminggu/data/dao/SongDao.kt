package com.mad.besokminggu.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mad.besokminggu.data.model.DailyPlay
import com.mad.besokminggu.data.model.PlayedSongDate
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.data.model.StreakInfo
import com.mad.besokminggu.data.model.TopArtistCapsule
import com.mad.besokminggu.data.model.TopSongCapsule
import java.util.Date

@Dao
interface SongDao {

    @Query("SELECT * FROM songs WHERE ownerId=:ownerId")
    fun getAllSongs(ownerId: Int): LiveData<List<Song>>

    @Query("SELECT * FROM songs WHERE isLiked = 1 and ownerId=:ownerId")
    fun getLikedSongs(ownerId: Int): LiveData<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<Song>)

    @Update
    suspend fun update(song: Song)

    @Delete
    suspend fun delete(song: Song)

    @Query("SELECT * FROM songs WHERE id = :id")
    fun getSong(id: Int): LiveData<Song>

    @Query("SELECT * FROM songs WHERE id = :id AND isLiked= 1")
    suspend fun getLikedSong(id: Int): Song

    @Query("DELETE FROM songs")
    suspend fun deleteAll()

    @Query("SELECT * FROM songs WHERE `id` > :currentIndex AND ownerId = :ownerId ORDER BY `id` ASC LIMIT 1")
    suspend fun getNextIdSong(currentIndex : Int, ownerId: Int): Song?

    @Query("SELECT * FROM songs WHERE ownerId = :ownerId ORDER BY `id` ASC LIMIT 1")
    suspend fun getFirstSong(ownerId: Int): Song

    @Query("SELECT COUNT(*) FROM songs WHERE ownerId = :ownerId")
    fun getSongsCount(ownerId: Int): LiveData<Int>

    @Query("SELECT COUNT(*) FROM songs WHERE isLiked = 1 AND ownerId = :ownerId")
    fun getLikedSongsCount(ownerId: Int): LiveData<Int>

    @Query("SELECT COUNT(*) FROM songs WHERE lastPlayedAt IS NOT NULL AND ownerId = :ownerId")
    fun getListenedSongsCount(ownerId: Int): LiveData<Int>

    @Query("SELECT * FROM songs WHERE id != :excludeId AND ownerId = :ownerId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomSongExcluding(excludeId: Int, ownerId: Int): Song?

    @Query("""
        SELECT SUM(totalPlayedSeconds)
        FROM songs
        WHERE ownerId = :ownerId
          AND strftime('%Y-%m', lastPlayedAt / 1000, 'unixepoch') = :monthYear
    """)
    suspend fun getTotalPlayedDurationByMonth(ownerId: Int, monthYear: String): Int?


    @Query("""
        SELECT * FROM songs
        WHERE ownerId = :ownerId
          AND strftime('%Y-%m', lastPlayedAt / 1000, 'unixepoch') = :monthYear
        GROUP BY title
        ORDER BY COUNT(*) DESC
        LIMIT 1
    """)
    suspend fun getTopSongByMonth(ownerId: Int, monthYear: String): Song?

    @Query("""
        SELECT artist
        FROM songs
        WHERE ownerId = :ownerId
          AND strftime('%Y-%m', lastPlayedAt / 1000, 'unixepoch') = :monthYear
        GROUP BY artist
        ORDER BY COUNT(*) DESC
        LIMIT 1
    """)
    suspend fun getTopArtistByMonth(ownerId: Int, monthYear: String): String?


    @Query("SELECT * FROM songs WHERE lastPlayedAt IS NOT NULL AND ownerId = :ownerId")
    suspend fun getAllPlayedSongs(ownerId: Int): List<Song>

    @Query("""
        SELECT DISTINCT strftime('%Y-%m', lastPlayedAt / 1000, 'unixepoch') AS monthYear
        FROM songs
        WHERE lastPlayedAt IS NOT NULL AND ownerId = :ownerId
        ORDER BY monthYear DESC
        LIMIT 3
    """)
    suspend fun getRecentMonthsWithPlayback(ownerId: Int): List<String>

    @Query("""
        UPDATE songs
        SET totalPlayedSeconds = totalPlayedSeconds + :seconds,
            lastPlayedAt = :lastPlayedAt
        WHERE id = :songId
    """)
    suspend fun incrementPlayedTime(songId: Int, seconds: Int, lastPlayedAt: Date)

    @Query("""
        SELECT coverFileName
        FROM songs
        WHERE ownerId = :ownerId
          AND artist = :artist
        ORDER BY totalPlayedSeconds DESC
        LIMIT 1
    """)
    suspend fun getTopArtistCover(ownerId: Int, artist: String): String?

    @Query("""
        SELECT title, artist, DATE(lastPlayedAt / 1000, 'unixepoch') as playedDate
        FROM songs
        WHERE ownerId = :ownerId
          AND strftime('%Y-%m', lastPlayedAt / 1000, 'unixepoch') = :monthKey
        ORDER BY playedDate ASC
    """)
    suspend fun getPlayedSongsByDate(ownerId: Int, monthKey: String): List<PlayedSongDate>

    @Query("""
        SELECT artist AS name, MAX(coverFileName) AS coverFileName
        FROM songs
        WHERE ownerId = :ownerId
          AND strftime('%Y-%m', lastPlayedAt / 1000, 'unixepoch') = :month
        GROUP BY artist
        ORDER BY COUNT(*) DESC
        LIMIT 5
    """)
    suspend fun getTopArtistsRaw(ownerId: Int, month: String): List<TopArtistCapsule>

    @Query("""
        SELECT 
            title, 
            artist, 
            MAX(coverFileName) AS coverFileName, 
            COUNT(*) as playCount 
        FROM songs
        WHERE ownerId = :ownerId
          AND strftime('%Y-%m', lastPlayedAt / 1000, 'unixepoch') = :monthYear
        GROUP BY title, artist, coverFileName
        ORDER BY playCount DESC
        LIMIT 5
    """)
    suspend fun getTopSongsByMonth(ownerId: Int, monthYear: String): List<TopSongCapsule>

    @Query("""
        SELECT COUNT(id)
        FROM songs
        WHERE ownerId = :ownerId
          AND strftime('%Y-%m', CAST(lastPlayedAt / 1000 AS INTEGER), 'unixepoch') = :monthYear
    """)
    suspend fun getTotalPlayedSongCount(ownerId: Int, monthYear: String): Int


    @Query("""
        SELECT strftime('%d', datetime(lastPlayedAt / 1000, 'unixepoch')) AS day,
               SUM(totalPlayedSeconds) / 60 AS minutes
        FROM songs
        WHERE ownerId = :ownerId
          AND strftime('%Y-%m', datetime(lastPlayedAt / 1000, 'unixepoch')) = :monthYear
        GROUP BY day
        ORDER BY day
    """)
    suspend fun getPlayedMinutesPerDay(ownerId: Int, monthYear: String): List<DailyPlay>





}