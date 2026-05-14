package com.pos10.helper.mapper

import com.pos10.db.entity.Checklist
import com.pos10.db.entity.WorkMetaDataEntity
import com.pos10.model.remote.GetWorkListResponse

object WorkMetaDataMapper {

    // Remote -> Entity
    fun mapToEntity(data: GetWorkListResponse.Data): WorkMetaDataEntity {
        return WorkMetaDataEntity(
            id = 0, // fixed row for metadata
            complains = data.complains,
            completed = data.completed,
            highPriority = data.highPriority,
            inProgress = data.inProgress,
            installation = data.installation,
            paperRoll = data.paperRoll,
            totalCount = data.totalCount,
            totalOrders = data.totalOrders,
            returnValue =  data.returnValue,
            replacement = data.replacement,
            checklist = data.checklist.map { mapChecklistToEntity(it) }
        )
    }

    private fun mapChecklistToEntity(item: GetWorkListResponse.Data.Checklist): Checklist {
        return Checklist(
            id = item.id,
            value = item.value,
            type = item.type,
            isSelected = item.isSelected
        )
    }

    // Entity -> Remote
    fun mapEntityToRemote(entity: WorkMetaDataEntity): GetWorkListResponse.Data {
        return GetWorkListResponse.Data(
            complains = entity.complains,
            completed = entity.completed,
            highPriority = entity.highPriority,
            inProgress = entity.inProgress,
            installation = entity.installation,
            paperRoll = entity.paperRoll,
            totalCount = entity.totalCount,
            totalOrders = entity.totalOrders,
            returnValue =  entity.returnValue,
            replacement = entity.replacement,
            checklist = ArrayList(entity.checklist.map { mapEntityChecklistToRemote(it) }),
            woList = arrayListOf()
        )
    }

    private fun mapEntityChecklistToRemote(item: Checklist): GetWorkListResponse.Data.Checklist {
        return GetWorkListResponse.Data.Checklist(
            id = item.id,
            value = item.value,
            type = item.type,
            isSelected = item.isSelected
        )
    }
}
