package com.mad.besokminggu.di

import android.content.Context
import android.content.SharedPreferences
import com.mad.besokminggu.data.services.UnprotectedApiService
import com.mad.besokminggu.data.services.ProtectedApiService
import com.mad.besokminggu.network.AuthInterceptor
import com.mad.besokminggu.network.RefreshTokenInterceptor
import com.mad.besokminggu.network.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SingletonModule {

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("SecurePrefs", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideSessionManager(sharedPreferences: SharedPreferences): SessionManager {
        return SessionManager(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        refreshTokenInterceptor: RefreshTokenInterceptor,
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
//            .setLevel(HttpLoggingInterceptor.Level.HEADERS)

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(refreshTokenInterceptor) // TODO: HAPUS AJA KALO EMANG GABERHASIL BRO
            .addInterceptor(authInterceptor)
//            .authenticator(authAuthenticator) // TODO: GANTI JADI INTERCEPTOR AJA KALO EMANG MASIH GA BERHASIL JUGA
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthInterceptor(tokenManager: SessionManager): AuthInterceptor =
        AuthInterceptor(tokenManager)

    @Singleton
    @Provides
    fun provideRefreshTokenInterceptor(tokenManager: SessionManager): RefreshTokenInterceptor =
        RefreshTokenInterceptor(tokenManager)

    @Singleton
    @Provides
    fun provideRetrofitBuilder(): Retrofit.Builder =
        Retrofit.Builder()
            .baseUrl("http://34.101.226.132:3000/api/")
            .addConverterFactory(GsonConverterFactory.create())

    @Singleton
    @Provides
    fun provideUnprotectedAPIService(retrofit: Retrofit.Builder): UnprotectedApiService =
        retrofit
            .build()
            .create(UnprotectedApiService::class.java)

    @Singleton
    @Provides
    fun provideProtectedAPIService(okHttpClient: OkHttpClient, retrofit: Retrofit.Builder): ProtectedApiService =
        retrofit
            .client(okHttpClient)
            .build()
            .create(ProtectedApiService::class.java)
}