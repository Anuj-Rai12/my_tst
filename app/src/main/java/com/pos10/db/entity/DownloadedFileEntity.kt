package com.pos10.db.entity

import androidx.room.Entity

@Entity(
    tableName = "downloaded_files",
    primaryKeys = ["id"]
)
data class DownloadedFileEntity(
    val requestId: String,
    val filePath: String,
    val originalFileName: String,
    val isSynced:Boolean=false,
    val uploadType:String,
    val requestType:String,
    val id: String

)
