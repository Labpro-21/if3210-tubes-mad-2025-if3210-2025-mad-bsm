package com.mad.besokminggu.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mad.besokminggu.data.repositories.UnprotectedRepository
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.network.SessionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

@HiltWorker
class RefreshTokenWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val unprotectedRepository: UnprotectedRepository,
    private val sessionManager: SessionManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        println("Refresh Token Worker Running...")

        val refreshToken = sessionManager.getRefreshToken()
                .catch { e -> emit(null) }
                .firstOrNull()

        if (refreshToken.isNullOrEmpty()) {
            println("No refresh token found")
            sessionManager.clearToken()
            scheduleNextRun(applicationContext)
            return Result.failure()
        }

        unprotectedRepository.refreshToken(refreshToken).collect { response ->
            when(response) {
                is ApiResponse.Failure -> {
                    println("Failed to refresh token: ${response.errorMessage}")
                }
                is ApiResponse.Success -> {
                    println("Token refreshed successfully")
                    response.data.let { newToken ->
                        sessionManager.storeAccessToken(newToken.accessToken, newToken.refreshToken)
                    }
                }
                is ApiResponse.Loading -> {
                    println("Refreshing token...")
                }
            }
        }

        scheduleNextRun(applicationContext)
        return Result.success()
    }

    private fun scheduleNextRun(context: Context) {
        println("Scheduling next run for RefreshTokenWorker...")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<RefreshTokenWorker>()
            .setConstraints(constraints)
            .setInitialDelay(5, TimeUnit.MINUTES)
            .build()

        try {
            WorkManager.getInstance(context).enqueue(request)
            println("Re-scheduled RefreshTokenWorker successfully.")
        } catch (e: Exception) {
            println("Failed to re-schedule RefreshTokenWorker: ${e.message}")
            e.printStackTrace()
        }
    }
}