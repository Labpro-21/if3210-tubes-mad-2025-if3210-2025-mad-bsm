package com.mad.besokminggu.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mad.besokminggu.data.model.Song

@Dao
interface SongDao {

    @Query("SELECT * FROM songs")
    fun getAllSongs(): LiveData<List<Song>>

    @Query("SELECT * FROM songs WHERE isLiked = 1")
    fun getLikedSongs(): LiveData<List<Song>>

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

    @Query("SELECT * FROM songs WHERE `id` > :currentIndex ORDER BY `id` ASC LIMIT 1")
    suspend fun getNextIdSong(currentIndex : Int): Song?

    @Query("SELECT * FROM songs ORDER BY `id` ASC LIMIT 1")
    suspend fun getFirstSong(): Song

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongsCount(): Int

    @Query("SELECT * FROM songs WHERE id != :excludeId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomSongExcluding(excludeId: Int): Song?


}