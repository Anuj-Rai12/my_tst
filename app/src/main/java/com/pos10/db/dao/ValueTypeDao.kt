package com.pos10.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pos10.db.entity.CancelResonListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CancelReasonListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(valueTypes: List<CancelResonListEntity>)

    @Query("SELECT * FROM cancel_list_table")
    fun getAllValueTypes(): Flow<List<CancelResonListEntity>>

    @Query("DELETE FROM cancel_list_table")
    suspend fun clearAll()
}
