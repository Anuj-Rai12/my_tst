package com.pos10.model.local

data class UploadFilePendingModel(
    val RequestId: String,
    val CreatedBy: String,
    val UploadType:String,
    val RequestType:String,
    val File: String
)
