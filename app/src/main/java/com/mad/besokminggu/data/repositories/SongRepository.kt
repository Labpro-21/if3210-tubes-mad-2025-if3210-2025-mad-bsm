package com.mad.besokminggu.data.repositories

import android.health.connect.datatypes.units.Length
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.mad.besokminggu.data.dao.SongDao
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.viewModels.UserViewModel
import javax.inject.Inject

class SongRepository @Inject constructor(private val songDao: SongDao) {

    private val _allSongs = MutableLiveData<LiveData<List<Song>>>()
    val allSongs: LiveData<List<Song>> get() = _allSongs.value ?: MutableLiveData()

    fun getAllSongs(ownerId: Int): LiveData<List<Song>> {
        Log.d("VALEN GET ALL SONG", "ownerId: $ownerId")
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

    suspend fun getSong(id : Int) : Song{
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

}
