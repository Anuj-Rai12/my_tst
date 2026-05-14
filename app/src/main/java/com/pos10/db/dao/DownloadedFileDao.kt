package com.pos10.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pos10.db.entity.DownloadedFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedFileDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertFiles(files: List<DownloadedFileEntity>)

        @Query("DELETE FROM downloaded_files WHERE id = :id")
        suspend fun deleteFileByPath(id: Int)

        @Query("SELECT * FROM downloaded_files WHERE requestId = :requestId")
        fun getFilesForRequestId(requestId: String): Flow<List<DownloadedFileEntity>>

        @Query("SELECT * FROM downloaded_files WHERE requestId = :requestId AND requestType = :requestType")
        fun getFilesForRequest(
                requestId: String,
                requestType: String
        ): Flow<List<DownloadedFileEntity>>

        @Query("DELETE FROM downloaded_files WHERE requestId = :requestId")
        suspend fun deleteFilesByRequestId(requestId: String)

        @Query("DELETE FROM downloaded_files")
        suspend fun clearAll()

        @Query("DELETE FROM downloaded_files WHERE requestId = :requestId AND uploadType = :uploadType")
        suspend fun deleteFile(requestId: Int, uploadType: String)
}
