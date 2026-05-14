package com.pos10.helper.mapper

import com.pos10.db.entity.CancelResonListEntity
import com.pos10.model.local.CancelReasonListResponse

object CancelRequestMapper {

    fun mapWoToEntity(cancelList: CancelReasonListResponse.Data): CancelResonListEntity {
        return CancelResonListEntity(
            valueTypeId = cancelList.valueTypeId,
            name = cancelList.name,
            description = cancelList.description,
            valueTypeGroupId = cancelList.valueTypeGroupId,
            createdDate = cancelList.createdDate,
            active = cancelList.active,
            displayOrder = cancelList.displayOrder,
            parentValueTypeId = cancelList.parentValueTypeId,
            bitMapValue = cancelList.bitMapValue,
        )}

    fun mapEntityToWo(entity: CancelResonListEntity): CancelReasonListResponse.Data{
        return CancelReasonListResponse.Data(
            valueTypeId = entity.valueTypeId,
            name = entity.name?:"",
            description = entity.description?:"",
            valueTypeGroupId = entity.valueTypeGroupId?:0,
            createdDate = entity.createdDate?:"",
            active = entity.active?:false,
            displayOrder = entity.displayOrder?:0,
            parentValueTypeId = entity.parentValueTypeId?:0,
            bitMapValue = entity.bitMapValue?:0,
        )}

    fun mapCancelListToEntityList(list: List<CancelReasonListResponse.Data>): List<CancelResonListEntity> {
        return list.map { mapWoToEntity(it) }
    }

    fun mapEntityListToCancelList(list: List<CancelResonListEntity>): List<CancelReasonListResponse.Data> {
        return list.map { mapEntityToWo(it) }
    }
}