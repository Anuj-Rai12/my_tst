package com.pos10.model.local

data class DeleteUploadFileRequest(
    val RequestId: Int,
    val RequestType: String,
    val UploadType: String
)