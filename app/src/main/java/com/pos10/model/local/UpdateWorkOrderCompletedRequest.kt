package com.pos10.model.local

data class UpdateWorkOrderCompletedRequest(
    val WorkId:String,
    val StatusId:Int,
    val CreatedBy:Int,
    val dueDate: String,
    val Remarks:String,
    val RequesId:String
    )