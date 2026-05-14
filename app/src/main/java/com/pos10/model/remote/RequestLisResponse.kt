package com.pos10.model.remote

data class RequestLisResponse(
    val `data`: Data,
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
        val requestList: ArrayList<Request>,
        val totalCount: Int,
        val totalOrders: Int
    ) {
        data class Checklist(
            val id: Int,
            val value: String,
            val isSelected:Boolean=false
        )

        data class Request(
            val count: Int,
            val createdBy: String,
            val createdDate: String,
            val description: String,
            val deviceType: String,
            val deviceNo: String,
            val dueDate: String,
            val location: String,
            val merchantCode: String,
            val merchantName: String,
            val priority: String,
            val quantity: String,
            val requestNo: String,
            val requestid: String,
            val requesttype: String,
            val serialNo: String,
            var status: String,
            val warrentyType: String,
            val ischecklistDone:Int,
            val appointmentDate:String,
            val appointmentTime:String,
            val workOrderNo:String,
            val installationStatus:String
        )
    }

    data class Info(
        val code: String,
        val isSuccess: Boolean,
        val message: String
    )
}