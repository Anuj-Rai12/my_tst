package com.pos10.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pos10.db.entity.FileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {

    @Query("SELECT * FROM files WHERE requestId = :requestId")
    fun getFilesForRequest(requestId: String): Flow<List<FileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<FileEntity>)
}
