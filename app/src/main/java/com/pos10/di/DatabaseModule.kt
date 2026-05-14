// DatabaseModule.kt
package com.pos10.di

import android.content.Context
import androidx.room.Room
import com.pos10.db.dao.AgentTrackHistoryDao
import com.pos10.db.dao.CancelReasonListDao
import com.pos10.db.dao.DownloadedFileDao
import com.pos10.db.dao.FileDao
import com.pos10.db.dao.PendingRequestDao
import com.pos10.db.dao.WorkMetaDataDao
import com.pos10.db.dao.WorkOrderDao
import com.pos10.db.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideWorkOrderDao(db: AppDatabase): WorkOrderDao = db.workOrderDao()

    @Provides
    @Singleton
    fun provideWorkMetaDataDao(db: AppDatabase): WorkMetaDataDao = db.workMetaDataDao()

    @Provides
    @Singleton
    fun providePendingRequestDao(db: AppDatabase): PendingRequestDao = db.pendingRequestDao()

    @Provides
    @Singleton
    fun provideDownloadFilesDao(db: AppDatabase): DownloadedFileDao = db.downloadedFileDao()

    @Provides
    @Singleton
    fun provideUploadFilesDao(db: AppDatabase): FileDao = db.fileDao()

    @Provides
    @Singleton
    fun provideAgentTrackHistoryDao(db: AppDatabase): AgentTrackHistoryDao = db.agentTrackHistoryDao()

    @Provides
    @Singleton
    fun provideCancelReasonListDao(db: AppDatabase): CancelReasonListDao = db.cancelReasonListDao()
}
