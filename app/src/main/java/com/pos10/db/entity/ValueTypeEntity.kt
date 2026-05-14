package com.pos10.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cancel_list_table")
data class CancelResonListEntity(
    @PrimaryKey val valueTypeId: Int,
    val name: String?,
    val description: String?,
    val valueTypeGroupId: Int?,
    val createdDate: String?,
    val active: Boolean?,
    val displayOrder: Int?,
    val parentValueTypeId: Int?,
    val bitMapValue: Int?
)