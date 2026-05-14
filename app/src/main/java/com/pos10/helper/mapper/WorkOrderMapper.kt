package com.pos10.helper.mapper

import com.pos10.db.entity.WorkOrderEntity
import com.pos10.db.entity.WoRequestEntity
import com.pos10.model.remote.GetWorkListResponse

object WorkOrderMapper {

    fun mapWoToEntity(wo: GetWorkListResponse.Data.Wo): WorkOrderEntity {
        return WorkOrderEntity(
            workid = wo.workid,
            appointmentDate = wo.appointmentDate,
            appointmentTime = wo.appointmentTime,
            dueDate = wo.dueDate,
            location = wo.location?:"",
            merchantCode = wo.merchantCode,
            merchantName = wo.merchantName,
            workOrderNo = wo.workOrderNo,
            workStatus = wo.workStatus,
            workStatusid = wo.workStatusid,
            mobile = wo.mobile,
            email = wo.email,
            priority = wo.priority,
            breachMessage = wo.breachMessage,
            woRequest = wo.woRequest.map { mapWoRequestToEntity(it) },
            latLong = wo.latLong?:"")
    }

    private fun mapWoRequestToEntity(req: GetWorkListResponse.Data.Wo.WoRequest): WoRequestEntity {
        return WoRequestEntity(
            appointmentDate = req.appointmentDate,
            appointmentTime = req.appointmentTime,
            count = req.count,
            description = req.description,
            deviceNo = req.deviceNo,
            deviceType = req.deviceType?:"-",
            deviceTypeId = req.deviceTypeId,
            installationStatus = req.installationStatus,
            ischecklistDone = req.ischecklistDone,
            location = req.location?:"",
            merchantCode = req.merchantCode,
            merchantName = req.merchantName,
            quantity = req.quantity,
            requestNo = req.requestNo,
            requestid = req.requestid,
            requesttype = req.requesttype,
            requesttypeId = req.requesttypeId,
            serialNo = req.serialNo,
            simnumber = req.simnumber,
            status = req.status,
            workid = req.workid,
            mobile = req.mobile?:"",
            email = req.email?:"",
            requestConditions = req.requestConditions?:""
        )
    }

    fun mapWoListToEntityList(list: List<GetWorkListResponse.Data.Wo>): List<WorkOrderEntity> {
        return list.map { mapWoToEntity(it) }
    }

    // Reverse mapping: Entity -> Domain Model
    fun mapEntityToWo(entity: WorkOrderEntity): GetWorkListResponse.Data.Wo {
        return GetWorkListResponse.Data.Wo(
            workid = entity.workid,
            appointmentDate = entity.appointmentDate,
            appointmentTime = entity.appointmentTime,
            dueDate = entity.dueDate,
            location = entity.location,
            merchantCode = entity.merchantCode,
            merchantName = entity.merchantName,
            workOrderNo = entity.workOrderNo,
            workStatus = entity.workStatus,
            workStatusid = entity.workStatusid,
            mobile = entity.mobile,
            email = entity.email,
            priority = entity.priority,
            breachMessage = entity.breachMessage?:"",
            woRequest = ArrayList(entity.woRequest.map { mapEntityRequestToWoRequest(it) }
            ),
            latLong = entity.latLong
        )
    }

    private fun mapEntityRequestToWoRequest(req: WoRequestEntity): GetWorkListResponse.Data.Wo.WoRequest {
        return GetWorkListResponse.Data.Wo.WoRequest(
            appointmentDate = req.appointmentDate,
            appointmentTime = req.appointmentTime,
            count = req.count,
            description = req.description,
            deviceNo = req.deviceNo,
            deviceType = req.deviceType,
            deviceTypeId = req.deviceTypeId,
            installationStatus = req.installationStatus,
            ischecklistDone = req.ischecklistDone,
            location = req.location,
            merchantCode = req.merchantCode,
            merchantName = req.merchantName,
            quantity = req.quantity,
            requestNo = req.requestNo,
            requestid = req.requestid,
            requesttype = req.requesttype,
            requesttypeId = req.requesttypeId,
            serialNo = req.serialNo,
            simnumber = req.simnumber,
            status = req.status,
            workid = req.workid,
            mobile = req.mobile,
            email = req.email,
            requestConditions = req.requestConditions

        )
    }

    fun mapEntityListToWoList(list: List<WorkOrderEntity>): List<GetWorkListResponse.Data.Wo> {
        return list.map { mapEntityToWo(it) }
    }
}
