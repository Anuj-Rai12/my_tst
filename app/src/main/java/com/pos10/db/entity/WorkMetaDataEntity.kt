package com.pos10.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.pos10.db.converter.WoCheckListConverter
@Entity(tableName = "work_metadata")
data class WorkMetaDataEntity(
    @PrimaryKey
    val id: Int = 0,
    val complains: Int,
    val completed: Int,
    val highPriority: Int,
    val inProgress: Int,
    val installation: Int,
    val paperRoll: Int,
    val totalCount: Int,
    val totalOrders: Int,
    val returnValue: Int,
    val replacement: Int,
    @TypeConverters(WoCheckListConverter::class)
    val checklist: List<Checklist>
)

data class Checklist(
    val id: Int,
    val value: String,
    val type: String,
    val isSelected: Boolean = false
)
