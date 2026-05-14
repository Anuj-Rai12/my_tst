package com.pos10.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pos10.db.entity.AgentTrackHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentTrackHistoryDao {

    @Query("SELECT * FROM agent_track_history ORDER BY id ASC")
    fun getAllTrackHistory(): Flow<List<AgentTrackHistoryEntity>>

    @Query("SELECT * FROM agent_track_history WHERE workId = :workId ORDER BY id ASC")
    fun getTrackHistoryByWorkId(workId: Int): Flow<List<AgentTrackHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<AgentTrackHistoryEntity>)

    @Query("DELETE FROM agent_track_history WHERE workId = :workId")
    suspend fun deleteByWorkId(workId: Int)

    @Query("DELETE FROM agent_track_history")
    suspend fun clearAll()
}
