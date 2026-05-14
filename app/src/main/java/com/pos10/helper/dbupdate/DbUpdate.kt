package com.pos10.helper.dbupdate

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.pos10.db.dao.DownloadedFileDao
import com.pos10.db.dao.FileDao
import com.pos10.db.dao.PendingRequestDao
import com.pos10.db.dao.WorkMetaDataDao
import com.pos10.db.dao.WorkOrderDao
import com.pos10.db.entity.FileEntity
import com.pos10.helper.CommonUtils
import com.pos10.model.local.UpdateRequest
import com.pos10.model.local.UpdateWorkOrderCompletedRequest
import com.pos10.network.ApiServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import javax.inject.Inject

class DbUpdate @Inject constructor(var apiService: ApiServices,
                                   private val dao: WorkOrderDao,
                                   private val workmetadao: WorkMetaDataDao,
                                   private val pendingRequestDao: PendingRequestDao,
                                   private val downloadedFileDao: DownloadedFileDao,
                                   private val fileDao: FileDao,
                                   private val gson: Gson,
                                   @ApplicationContext context: Context) {

    /**
     * ✅ Shared helper for Sync + Repository
     */

     suspend fun updateWorkOrderAndRequests(
        workId: Int,
        status: String,
        workStatusid: Int,
        appointmentDate: String? = null,
        appointmentTime: String? = null) {
        dao.getWorkOrderById(workId)?.let {
            val updatedRequests = it.woRequest.map { req ->
                req.copy(status = status)
            }
            val updatedEntity = it.copy(
                workStatus = status,
                workStatusid = workStatusid,
                appointmentDate = appointmentDate ?: it.appointmentDate,
                appointmentTime = appointmentTime ?: it.appointmentTime,
                woRequest = updatedRequests
            )
            dao.updateWorkOrder(updatedEntity)
        }
    }


    suspend fun updateRequestAfterQRCode(
        requestId: Int,
        installationStatus: String,
        serialnumber: String,
        simnumber: String,
    ) {
        // Collect the latest snapshot once
        dao.getAllWorkOrdersFlow()
            .firstOrNull()
            ?.firstOrNull { order ->
                order.woRequest.any { it.requestid.toInt() == requestId }
            }?.let { workOrder ->

                // update only that request
                val updatedRequests = workOrder.woRequest.map { req ->
                    if (req.requestid.toInt() == requestId) {
                        req.copy(installationStatus = installationStatus, serialNo =serialnumber, simnumber = simnumber )
                    } else req
                }

                // update parent status if all completed
                val parentStatus = workOrder.workStatus

                // save back in DB
                val updatedEntity = workOrder.copy(
                    workStatus = parentStatus,
                    woRequest = updatedRequests
                )
                dao.updateWorkOrder(updatedEntity)
            }
    }

     suspend fun updateRequestAfterUpdateApi(model: UpdateRequest) {
        dao.getAllWorkOrdersFlow()
            .firstOrNull()
            ?.firstOrNull { order ->
                order.woRequest.any { it.requestid.toInt() == model.RequestId }
            }?.let { workOrder ->

                val updatedRequests = workOrder.woRequest.map { req ->
                    if (req.requestid.toInt() == model.RequestId) {

                        // Checklist: only set to 1 if model says true, else keep old
                        val newChecklist = if (model.IschecklistDone) 1 else req.ischecklistDone

                        // Status: only update if model explicitly changes it, else keep old
                        val newStatus = if (model.Status != 0) {
                            val mappedStatus = CommonUtils.STATUS.getByType(model.Status)?.sttausname
                            if (mappedStatus != null) mappedStatus else req.status
                        } else req.status

                        req.copy(
                            ischecklistDone = newChecklist,
                            status = newStatus,
                            appointmentDate = if (model.AppointmentDate.isNotEmpty()) model.AppointmentDate else req.appointmentDate,
                            appointmentTime = if (model.AppointmentTime.isNotEmpty()) model.AppointmentTime else req.appointmentTime,
                            description = if (model.Description.isNotEmpty()) model.Description else req.description
                        )
                    } else req
                }



                val updatedEntity = workOrder.copy(
                 /*   workStatus = newWorkStatus,
                    workStatusid = newWorkStatusId,*/
                    woRequest = updatedRequests
                )

                Log.d("TAG", "updateRequestAfterUpdateApi:Updated--${model.Status} ")

                dao.updateWorkOrder(updatedEntity)
            }
    }

     suspend fun updateFileInDb(
        requestId: String,
        filePath: String,
    ) {
        val entity = FileEntity(
            requestId = requestId,
            filePath = filePath,
            originalFileName = File(filePath).name,
        )
        fileDao.insertFile(entity)
    }

    suspend fun updateWorkOrderCompletedAfterUpdateApi(model: UpdateWorkOrderCompletedRequest) {
        Log.d("TAG", "updateWorkOrderCompletedAfterUpdateApi: yyyy---$model")
        dao.getAllWorkOrdersFlow()
            .firstOrNull()
            ?.firstOrNull { order ->
                Log.d("TAG", "updateWorkOrderCompletedAfterUpdateApiOrders:${order.workid}--${model.WorkId.toInt()} ")
                order.workid == model.WorkId.toInt()
            }
            ?.let { workOrder ->
                var statusType =0
                var status =""
                if(model.StatusId==(CommonUtils.STATUS.INPROGRESS.type)){
                    status=CommonUtils.STATUS.INPROGRESS.sttausname
                    statusType=CommonUtils.STATUS.INPROGRESS.type
                }else if(model.StatusId==(CommonUtils.STATUS.COMPLETED.type)){
                    status=CommonUtils.STATUS.COMPLETED.sttausname
                    statusType=CommonUtils.STATUS.COMPLETED.type
                }

                Log.d("TAG", "updateWorkOrderCompletedAfterUpdateApi: Status---$status---$statusType")
                val updatedEntity = workOrder.copy(
                    workStatus = status,
                    workStatusid = statusType)

                Log.d("TAG",
                    "updateWorkOrderCompletedAfterUpdateApi: Updated WorkId=${model.WorkId} → " +
                            "workStatus=${updatedEntity.workStatus}, workStatusid=${updatedEntity.workStatusid}")

                dao.updateWorkOrder(updatedEntity)
            }
    }


    suspend fun deleteFileFromDb(
        requestId: Int,
        uploadType: String) {
        downloadedFileDao.deleteFile(requestId, uploadType)
    }

    suspend fun updateWorkOrderAndRequests1(
        workId: Int,
        status: String,
        workStatusid:Int) {
        dao.getWorkOrderById(workId)?.let {
            val updatedRequests = it.woRequest.map { req ->
                req.copy(status = status)
            }
            val updatedEntity = it.copy(
                workStatus = status,
                workStatusid = workStatusid,
                woRequest = updatedRequests
            )
            dao.updateWorkOrder(updatedEntity)
        }
    }
}