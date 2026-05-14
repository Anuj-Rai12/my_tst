package com.pos10

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.assetinfinity.app.gpsclient.TrackingService
import com.pos10.di.DbSyncWorker
import com.pos10.helper.GlobalSnackbar
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class MyApplication : Application() {
    val TAG = "Pay10"

    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        GlobalSnackbar.init(this)
        // Start service safely
//        val intent = Intent(appContext, TrackingService::class.java)
//        ContextCompat.startForegroundService(appContext, intent)

        // scheduleDbSyncWorker()
    }

    private fun scheduleDbSyncWorker() {
        val workRequest = PeriodicWorkRequestBuilder<DbSyncWorker>(
            15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DbSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
