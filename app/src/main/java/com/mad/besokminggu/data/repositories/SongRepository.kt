package com.mad.besokminggu.data.repositories


import androidx.lifecycle.LiveData
import com.mad.besokminggu.data.dao.SongDao
import com.mad.besokminggu.data.model.Song
import javax.inject.Inject

class SongRepository @Inject constructor(private val songDao: SongDao) {
    val allSongs: LiveData<List<Song>> = songDao.getAllSongs()

    suspend fun insert(song: Song) {
        songDao.insert(song)
    }

    suspend fun update(song: Song){
        songDao.update(song)
    }

    suspend fun deleteAllSongs() {
        songDao.deleteAll()
    }

    suspend fun getSong(id : Int) : Song{
        return songDao.getSong(id);
    }

    suspend fun deleteSong(song: Song){
        songDao.delete(song)
    }

    suspend fun getNextIteratedSong(currentSong : Song) : Song{
        val nextSong = songDao.getNextIdSong(currentSong.id)
        return nextSong ?: songDao.getFirstSong()
    }

    suspend fun isEmpty(): Boolean {
        return songDao.getSongsCount() == 0
    }

    suspend fun getNextRandomSong(currentSong: Song): Song {
        return songDao.getRandomSongExcluding(currentSong.id) ?: songDao.getFirstSong()
    }

}
