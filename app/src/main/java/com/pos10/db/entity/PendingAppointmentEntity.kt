package com.pos10.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pos10.helper.CommonUtils

@Entity(tableName = "pending_appointments")
data class PendingAppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workId: Int,
    val appointmentDate: String,
    val appointmentTime: String,
    val userId: Int,
    val status: String = CommonUtils.STATUS.ASSIGNED.sttausname, // default
    val isSynced: Boolean = false
)