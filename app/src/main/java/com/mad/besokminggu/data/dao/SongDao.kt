package com.mad.besokminggu.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mad.besokminggu.data.model.Song

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
    suspend fun getSong(id: Int): Song

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


}