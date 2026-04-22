package com.shayanhaque.politicstracker.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shayanhaque.politicstracker.PoliticsTrackerApp
import com.shayanhaque.politicstracker.data.repository.RefreshOutcome
import java.util.concurrent.TimeUnit

/**
 * Periodic background refresh so the user's cached data doesn't go stale
 * between app opens. Failures retry with backoff — we never want WorkManager
 * to give up just because the user's wifi dropped for a minute.
 */
class MarketRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as PoliticsTrackerApp).container.repository
        return when (repository.refreshMarkets()) {
            RefreshOutcome.Success -> Result.success()
            is RefreshOutcome.Failure -> Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_NAME = "markets-periodic-refresh"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<MarketRefreshWorker>(
                repeatInterval = 30,
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                // KEEP: don't reset the schedule every time the app cold-starts,
                // otherwise the first tick is pushed out to 30min every launch.
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
