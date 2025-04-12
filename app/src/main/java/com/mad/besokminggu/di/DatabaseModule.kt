package com.mad.besokminggu.di

import android.content.Context
import androidx.room.Room
import com.mad.besokminggu.data.SongDatabase
import com.mad.besokminggu.data.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): SongDatabase {
        return Room.databaseBuilder(
            appContext,
            SongDatabase::class.java,
            "songs.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideSongDao(db: SongDatabase): SongDao {
        return db.songDao()
    }
}
