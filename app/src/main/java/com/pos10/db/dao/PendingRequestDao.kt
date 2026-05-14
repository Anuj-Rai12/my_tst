package com.pos10.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pos10.db.entity.PendingRequestEntity

@Dao
interface PendingRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingRequest(request: PendingRequestEntity)

    @Query("SELECT * FROM pending_requests ORDER BY createdAt ASC")
    suspend fun getAllPendingRequests(): List<PendingRequestEntity>

    @Delete
    suspend fun deletePendingRequest(request: PendingRequestEntity)
}
