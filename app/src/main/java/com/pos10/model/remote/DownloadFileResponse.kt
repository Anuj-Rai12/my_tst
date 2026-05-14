package com.pos10.model.remote

data class DownloadFileResponse(
    val `data`: ArrayList<Data>,
    val info: Info
) {
    data class Data(
        val filePath: String,
        val originalFileName: String,
        val uploadType:String,
        val id: String
    )

    data class Info(
        val code: String,
        val isSuccess: Boolean,
        val message: String
    )
}