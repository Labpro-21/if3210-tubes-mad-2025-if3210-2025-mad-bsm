package com.mad.besokminggu.data.repositories

import android.health.connect.datatypes.units.Length
import androidx.lifecycle.LiveData
import com.mad.besokminggu.data.dao.SongDao
import com.mad.besokminggu.data.model.Song
import javax.inject.Inject

class SongRepository @Inject constructor(private val songDao: SongDao) {
    val allSongs: LiveData<List<Song>> = songDao.getAllSongs()

    suspend fun insertDummySongsIfEmpty(dummySongs: List<Song>) {
        if(isEmpty()) {
            songDao.insertAll(dummySongs)
        }
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

    suspend fun getSong(id : Int) : Song{
        return songDao.getSong(id);
    }

    fun getLikedSongsCount() : LiveData<Int> {
        return songDao.getLikedSongsCount()
    }

    fun getTotalSongsCount() : LiveData<Int>{
        return songDao.getSongsCount()
    }

    fun getListenedSongsCount() : LiveData<Int>{
        return songDao.getListenedSongsCount()
    }

    suspend fun getNextIteratedSong(currentSong : Song) : Song{
        val nextSong = songDao.getNextIdSong(currentSong.id)
        return nextSong ?: songDao.getFirstSong()
    }

    suspend fun isEmpty(): Boolean {
        return songDao.getSongsCount().value == 0
    }
}
