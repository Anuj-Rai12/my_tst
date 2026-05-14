package com.pos10.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pos10.db.converter.WoCheckListConverter
import com.pos10.db.converter.WoRequestListConverter
import com.pos10.db.dao.AgentTrackHistoryDao
import com.pos10.db.dao.CancelReasonListDao
import com.pos10.db.dao.DownloadedFileDao
import com.pos10.db.dao.FileDao
import com.pos10.db.dao.PendingRequestDao
import com.pos10.db.dao.WorkMetaDataDao
import com.pos10.db.dao.WorkOrderDao
import com.pos10.db.entity.AgentTrackHistoryEntity
import com.pos10.db.entity.CancelResonListEntity
import com.pos10.db.entity.DownloadedFileEntity
import com.pos10.db.entity.FileEntity
import com.pos10.db.entity.WorkOrderEntity
//import com.pay10.db.entity.WorkOrderRequestEntity
//import com.pay10.db.entity.ChecklistEntity
//import com.pay10.db.entity.InfoEntity
import com.pos10.db.entity.PendingRequestEntity
import com.pos10.db.entity.WorkMetaDataEntity

@Database(
    entities = [WorkOrderEntity::class, WorkMetaDataEntity::class, PendingRequestEntity::class, DownloadedFileEntity::class, FileEntity::class, AgentTrackHistoryEntity::class, CancelResonListEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(WoRequestListConverter::class, WoCheckListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workOrderDao(): WorkOrderDao
    abstract fun workMetaDataDao(): WorkMetaDataDao
    abstract fun pendingRequestDao(): PendingRequestDao
    abstract fun downloadedFileDao(): DownloadedFileDao
    abstract fun fileDao(): FileDao
    abstract fun agentTrackHistoryDao(): AgentTrackHistoryDao
    abstract fun cancelReasonListDao(): CancelReasonListDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "app_database"
                ).fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

                // Add new column with default value
                database.execSQL(
                    """
                            ALTER TABLE work_metadata 
                            ADD COLUMN return_value INTEGER NOT NULL DEFAULT 0
                         """.trimIndent()
                )

                database.execSQL(
                    """
                            ALTER TABLE work_metadata 
                            ADD COLUMN replacement INTEGER NOT NULL DEFAULT 0
                         """.trimIndent()
                )

            }
        }
    }


}


