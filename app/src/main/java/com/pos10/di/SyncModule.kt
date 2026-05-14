package com.pos10.di

import android.content.Context
import com.google.gson.Gson
import com.pos10.db.dao.AgentTrackHistoryDao
import com.pos10.db.dao.CancelReasonListDao
import com.pos10.db.dao.DownloadedFileDao
import com.pos10.db.dao.FileDao
import com.pos10.db.dao.PendingRequestDao
import com.pos10.db.dao.WorkMetaDataDao
import com.pos10.db.dao.WorkOrderDao
import com.pos10.db.synchandler.GlobalDbSyncHandler
import com.pos10.helper.dbupdate.DbUpdate
import com.pos10.network.ApiServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideGlobalDbSyncHandler(
        @ApplicationContext context: Context,
        workOrderDao: WorkOrderDao,
        workMetaDataDao: WorkMetaDataDao,
        pendingRequestDao: PendingRequestDao,
        downloadedFileDao: DownloadedFileDao,
        fileDao: FileDao,
        agentTrackHistoryDao: AgentTrackHistoryDao,
        dbUpdate: DbUpdate,
        gson: Gson,
        apiService: ApiServices,
        cancelReasonListDao: CancelReasonListDao
    ): GlobalDbSyncHandler {
        return GlobalDbSyncHandler(context, workOrderDao,workMetaDataDao,pendingRequestDao,downloadedFileDao,fileDao,agentTrackHistoryDao,dbUpdate,gson, apiService,cancelReasonListDao)
    }
}
