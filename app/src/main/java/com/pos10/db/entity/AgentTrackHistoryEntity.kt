package com.pos10.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agent_track_history")
data class AgentTrackHistoryEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,  // For Room
    val id: Int,                                            // From API
    val agentUserid: Int,
    val agentName: String,
    val latitude: Double,
    val longitude: Double,
    val workOrderNo: String,
    val workId: Int,
    val status: String,
    val startDate: String?,   // optional (store for filtering)
    val endDate: String?      // optional
)
