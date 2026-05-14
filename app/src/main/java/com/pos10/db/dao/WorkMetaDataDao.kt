package com.pos10.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pos10.db.entity.WorkMetaDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkMetaDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkMetaData(metaData: WorkMetaDataEntity)

    @Query("SELECT * FROM work_metadata WHERE id = 0 LIMIT 1")
    fun getWorkMetaData(): Flow<WorkMetaDataEntity?>
}
