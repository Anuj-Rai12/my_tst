package com.pos10.db.entity

import androidx.room.*
import com.pos10.db.converter.WoRequestListConverter

@Entity(tableName = "work_orders")
data class WorkOrderEntity(
    @PrimaryKey
    val workid: Int,
    val appointmentDate: String,
    val appointmentTime: String,
    val dueDate: String?,
    val location: String,
    val merchantCode: String,
    val merchantName: String,
    val workOrderNo: String,
    val workStatus: String,
    val workStatusid: Int,
    val mobile: String?,
    val email: String?,
    val priority: String?,
    val breachMessage: String?,
    @TypeConverters(WoRequestListConverter::class)
    val woRequest: List<WoRequestEntity>,
    val latLong: String?
)

data class WoRequestEntity(
    val appointmentDate: String,
    val appointmentTime: String,
    val count: Int,
    val description: String,
    val deviceNo: String,
    val deviceType: String,
    val deviceTypeId: Int,
    var installationStatus: String,
    val ischecklistDone: Int,
    val location: String,
    val merchantCode: String,
    val merchantName: String,
    val quantity: String,
    val requestNo: String,
    val requestid: String,
    val requesttype: String,
    val requesttypeId: Int,
    val serialNo: String,
    val simnumber: String,
    var status: String,
    val workid: Int,
    val mobile: String,
    val email: String,
    val requestConditions: String
)
