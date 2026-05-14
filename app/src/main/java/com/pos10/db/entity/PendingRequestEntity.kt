package com.pos10.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_requests")
data class PendingRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val endpoint: String,              // e.g., "/saveAppointment"
    val payload: String,               // JSON payload
    val workOrderId: Int?=0,           // which WO it belongs to
    val createdAt: Long = System.currentTimeMillis()
)
