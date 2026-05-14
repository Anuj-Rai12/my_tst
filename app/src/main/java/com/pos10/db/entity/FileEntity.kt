package com.pos10.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: String,
    val filePath: String,
    val originalFileName: String,
)
