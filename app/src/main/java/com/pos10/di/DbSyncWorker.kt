package com.pos10.di

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pos10.db.synchandler.GlobalDbSyncHandler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DbSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dbSyncHandler: GlobalDbSyncHandler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            dbSyncHandler.manualSync() // Run the sync
            return Result.success()
        } catch (e: Exception) {
            return Result.retry() // retry if fails
        }
    }
}
