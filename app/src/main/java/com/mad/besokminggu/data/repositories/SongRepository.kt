package com.mad.besokminggu.data.repositories

import androidx.lifecycle.LiveData
import com.mad.besokminggu.data.dao.SongDao
import com.mad.besokminggu.data.model.Song
import javax.inject.Inject

class SongRepository @Inject constructor(private val songDao: SongDao) {
    val allSongs: LiveData<List<Song>> = songDao.getAllSongs()

    suspend fun insertDummySongsIfEmpty(dummySongs: List<Song>) {
        // optional: check if table is empty before inserting
        songDao.insertAll(dummySongs)
    }

    suspend fun insert(song: Song) {
        songDao.insert(song)
    }

    suspend fun deleteAllSongs() {
        songDao.deleteAll()
    }
}
