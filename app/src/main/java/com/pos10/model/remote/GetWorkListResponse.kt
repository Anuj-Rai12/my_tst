package com.pos10.model.remote

import com.google.gson.annotations.SerializedName

data class GetWorkListResponse(
    val data: Data,
    val info: Info
) {
    data class Data(
        val checklist: ArrayList<Checklist>,
        val complains: Int,
        val completed: Int,
        val highPriority: Int,
        val inProgress: Int,
        val installation: Int,
        val paperRoll: Int,
        @SerializedName("return")
        val returnValue: Int,
        val replacement: Int,
        val totalCount: Int,
        val totalOrders: Int,
        val woList: ArrayList<Wo>
    ) {
        data class Checklist(
            val id: Int,
            val value: String,
            val type: String,
            val isSelected: Boolean = false
        )

        data class Wo(
            val appointmentDate: String,
            val appointmentTime: String,
            val dueDate: String? = null,
            val location: String? = null,
            val merchantCode: String,
            val merchantName: String,
            val woRequest: ArrayList<WoRequest>,
            val workOrderNo: String,
            val workStatus: String,
            val workStatusid: Int,
            val workid: Int,
            val requestCount: Int = 0,
            val mobile: String?,
            val email: String?,
            val priority: String? = "3",
            val breachMessage: String,
            val latLong: String? = null
        ) {
            data class WoRequest(
                val appointmentDate: String,
                val appointmentTime: String,
                val count: Int,
                val description: String,
                val deviceNo: String,
                val deviceType: String,
                val deviceTypeId: Int,
                var installationStatus: String,
                var ischecklistDone: Int,
                val location: String? = null,
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
                val mobile: String?,
                val email: String?,
                val requestConditions: String? =null
            )
        }
    }

    data class Info(
        val code: Int,
        val isSuccess: Boolean,
        val message: String
    )
}