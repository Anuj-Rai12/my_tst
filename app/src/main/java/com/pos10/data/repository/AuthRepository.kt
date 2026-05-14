package com.pos10.data.repository


import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.pos10.MyApplication
import com.pos10.db.dao.AgentTrackHistoryDao
import com.pos10.db.dao.CancelReasonListDao
import com.pos10.db.dao.DownloadedFileDao
import com.pos10.db.dao.FileDao
import com.pos10.db.dao.PendingRequestDao
import com.pos10.db.dao.WorkMetaDataDao
import com.pos10.db.dao.WorkOrderDao
import com.pos10.db.entity.DownloadedFileEntity
import com.pos10.db.entity.PendingRequestEntity
import com.pos10.helper.CommonUtils
import com.pos10.helper.EmpBaseRepository
import com.pos10.helper.EmpResource
import com.pos10.helper.GlobalSnackbar
import com.pos10.helper.NetworkUtils
import com.pos10.helper.dbupdate.DbUpdate
import com.pos10.helper.mapper.CancelRequestMapper
import com.pos10.helper.mapper.DownloadFileMapper
import com.pos10.helper.mapper.WorkMetaDataMapper
import com.pos10.helper.mapper.WorkOrderMapper
import com.pos10.model.local.AuthRequest
import com.pos10.model.local.CancelReasonListResponse
import com.pos10.model.local.DeleteUploadFileRequest
import com.pos10.model.local.FcmRequest
import com.pos10.model.local.GenerateOTPRequest
import com.pos10.model.local.GetAgenTrackHistoryRequest
import com.pos10.model.local.GetAgentFeedbackRequest
import com.pos10.model.local.GetRequestList
import com.pos10.model.local.NotificationToggleRequest
import com.pos10.model.local.QRCodeScanerRequest
import com.pos10.model.local.SaveApppointmentRequest
import com.pos10.model.local.UnbindDeviceRequest
import com.pos10.model.local.UpdateRequest
import com.pos10.model.local.UpdateWorkOrderCompletedRequest
import com.pos10.model.local.UploadFilePendingModel
import com.pos10.model.remote.DownloadFileResponse
import com.pos10.model.remote.GetWorkListResponse
import com.pos10.model.remote.LocationTrackRequest
import com.pos10.model.remote.UnbindDeviceResponse
import com.pos10.model.remote.ValidateOTPRequest
import com.pos10.network.ApiServices
import com.pos10.view.screens.MerchantDetailsRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class AuthRepository @Inject constructor(
    var apiService: ApiServices,
    private val dao: WorkOrderDao,
    private val workmetadao: WorkMetaDataDao,
    private val pendingRequestDao: PendingRequestDao,
    private val downloadedFileDao: DownloadedFileDao,
    private val fileDao: FileDao,
    private val agentTrackHistoryDao: AgentTrackHistoryDao,
    private val dbUpdate: DbUpdate,
    private val gson: Gson,
    @ApplicationContext context: Context,
    private val cancelReasonListDao: CancelReasonListDao,
) :
    EmpBaseRepository() {

    suspend fun hitLogin(model: AuthRequest) = safeApiCall {
        apiService.login(
            grantType = model.grant_type,
            clientId = model.client_id,
            password = model.password,
            username = model.username
        )
    }

    suspend fun hitMerchantDetail(/*token: String,*/ model: MerchantDetailsRequest) = safeApiCall {
        apiService.merchantDetails(model = model)
    }

    suspend fun hitRequestList(token: String, model: GetRequestList) = safeApiCall {
        apiService.getRequestList(model = model)
    }


    suspend fun hitUpdate(token: String, model: UpdateRequest) = safeApiCall {
        apiService.updateRequest(model = model)
    }

    suspend fun hitTerminalStatus(token: String, terminalId: String) = safeApiCall {
        apiService.getTerminalStatus(terminalId = terminalId)
    }

    suspend fun hitDownloadFile(token: String, requestId: String, requestType: String) =
        safeApiCall {
            apiService.downloadFile(requestId = requestId, requestType = requestType)
        }

    suspend fun hitGetGenerateOTP(token: String, model: GenerateOTPRequest) = safeApiCall {
        apiService.getGenerateOTP(model = model)
    }

    suspend fun hitValidateOTP(token: String, model: ValidateOTPRequest) = safeApiCall {
        apiService.validateOTP(model = model)
    }

    suspend fun hitLocationTrackRequest(token: String, model: LocationTrackRequest) = safeApiCall {
        apiService.locationTrackRequest(model = model)
    }

    suspend fun hitAgentTrackHistory(token: String, model: GetAgenTrackHistoryRequest) =
        safeApiCall {
            apiService.getAgentHistory(model = model)
        }

    suspend fun hitFieldAgentFcmtoken(token: String, model: FcmRequest) = safeApiCall {
        apiService.getFieldAgentFcmToken(model = model)
    }

    suspend fun hitFieldAgentFeedbck(token: String, model: GetAgentFeedbackRequest) = safeApiCall {
        apiService.getAgentFeedback(model = model)
    }

    suspend fun hitNotificationStatus(token: String, model: NotificationToggleRequest) =
        safeApiCall {
            apiService.notificationStatus(model = model)
        }

    //checking offline
    // Expose domain model flow
    fun getWorkOrdersFromDb(): Flow<List<GetWorkListResponse.Data.Wo>> {
        return dao.getAllWorkOrders()
            .map { entities ->
                WorkOrderMapper.mapEntityListToWoList(entities)
            }
    }

    fun getWorkMetaDataFromDb(): Flow<GetWorkListResponse.Data?> {
        return workmetadao.getWorkMetaData()
            .map { entity ->
                entity?.let { WorkMetaDataMapper.mapEntityToRemote(it) }
            }
    }


    fun getDownloadedFilesFromDb(
        requestId: String,
        requestType: String
    ): Flow<List<DownloadFileResponse.Data>> {
        return downloadedFileDao.getFilesForRequest(requestId, requestType)
            .map { entities ->
                DownloadFileMapper.mapEntityListToModelList(entities)
            }
    }

    //  Generic queue helper (can handle different models/endpoints)
    private suspend fun queuePendingRequest(
        endpoint: String,
        model: Any,
        workOrderId: Int? = null
    ) {
        val payload = gson.toJson(model)
        val pending = PendingRequestEntity(
            endpoint = endpoint,
            payload = payload,
            workOrderId = workOrderId
        )
        pendingRequestDao.insertPendingRequest(pending)
    }

    /* -------------------- Work Appointment -------------------- */

    suspend fun scheduleWorkAppointment(token: String, model: SaveApppointmentRequest) {
        val allOrders = dao.getAllWorkOrders().first()

        val anyInProgress = allOrders.any { order ->
            order.workStatus == CommonUtils.STATUS.INPROGRESS.sttausname
        }

        val sameOrderInProgress = allOrders.any { order ->
            order.workStatus == CommonUtils.STATUS.INPROGRESS.sttausname &&
                    order.workid == model.workId
        }
        when {
            anyInProgress && !sameOrderInProgress -> {
                //  Block if some other order is in progress
                GlobalSnackbar.info("Agent already has a work order InProgress.")
                return
            }

            else -> {
            }
        }

        val isOnline = NetworkUtils.isOnline(MyApplication.appContext)
        try {
            if (isOnline) {
                val response = apiService.saveAppointment(
                    model = model
                )

                if (response.info.isSuccess) {
                    dbUpdate.updateWorkOrderAndRequests(
                        workId = model.workId,
                        workStatusid = CommonUtils.STATUS.INPROGRESS.type,
                        status = CommonUtils.STATUS.INPROGRESS.sttausname,
                        appointmentDate = model.appointmentDate,
                        appointmentTime = model.appointmentTime
                    )
                    return //  success → nothing else
                }
            }
            // failure (API error or offline) → queue + optimistic update
            queuePendingRequest(
                endpoint = "api/WorkAssignment/UpdateAppointmentDetails",
                model = model,
                workOrderId = model.workId
            )

            dbUpdate.updateWorkOrderAndRequests(
                workId = model.workId,
                workStatusid = CommonUtils.STATUS.INPROGRESS.type,
                status = CommonUtils.STATUS.INPROGRESS.sttausname,
                appointmentDate = model.appointmentDate,
                appointmentTime = model.appointmentTime
            )
            Log.d("scheduleWorkAppointment", "Api added to pending work request queue")

        } catch (e: Exception) {
            //  exception → queue + optimistic update
            queuePendingRequest(
                endpoint = "api/WorkAssignment/UpdateAppointmentDetails",
                model = model,
                workOrderId = model.workId
            )

            dbUpdate.updateWorkOrderAndRequests(
                workId = model.workId,
                workStatusid = CommonUtils.STATUS.INPROGRESS.type,
                status = CommonUtils.STATUS.INPROGRESS.sttausname,
                appointmentDate = model.appointmentDate,
                appointmentTime = model.appointmentTime
            )
        }
    }

    /* -------------------- QR Code -------------------- */
    suspend fun hitQRCode(model: QRCodeScanerRequest) {
        val isOnline = NetworkUtils.isOnline(MyApplication.appContext)

        try {
            if (isOnline) {
                val response = apiService.qrcodeScanner(
                    model = model
                )

                if (response.info.isSuccess) {
                    dbUpdate.updateRequestAfterQRCode(
                        model.RequestId, response.data.installationStatus,
                        model.SerialNumber, model.SimNumber
                    )
                    return
                } else {
                    Log.d("TAG", "hitQRCode: ----")
                    Toast.makeText(
                        MyApplication.appContext,
                        response.info.message,
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
            }
            // ⏸ offline → queue + optimistic update
            queuePendingRequest(
                endpoint = "api/Request/UpdateDeviceRequest",
                model = model
            )
            dbUpdate.updateRequestAfterQRCode(
                model.RequestId,
                "1",
                model.SerialNumber,
                model.SimNumber
            )

        } catch (e: Exception) {
            //  exception → queue + optimistic update
            queuePendingRequest(
                endpoint = "api/Request/UpdateDeviceRequest",
                model = model
            )
            dbUpdate.updateRequestAfterQRCode(
                model.RequestId,
                "1",
                model.SerialNumber,
                model.SimNumber
            )
        }
    }

    /*-------------------------UPDATE REQUEST STATUS-------------------------*/
    suspend fun hitUpdateRequest( model: UpdateRequest) {
        val isOnline = NetworkUtils.isOnline(MyApplication.appContext)

        try {
            if (isOnline) {
                val response =
                    apiService.updateRequest(/*authorization = returnAccessToken(MyApplication.appContext),*/
                        model = model
                    )

                if (response.info.isSuccess) {
                    dbUpdate.updateRequestAfterUpdateApi(model)
                    return
                } else {
//                    queuePendingRequest(endpoint = "api/Request/UpdateRequest", model = model)
//                    dbUpdate.updateRequestAfterUpdateApi(model)
                    return
                }
            }
            // ⏸ offline → queue + optimistic update
            queuePendingRequest(
                endpoint = "api/Request/UpdateRequest",
                model = model
            )
            dbUpdate.updateRequestAfterUpdateApi(model)

        } catch (e: Exception) {
            //  exception → queue + optimistic update
            queuePendingRequest(endpoint = "api/Request/UpdateRequest", model = model)
            dbUpdate.updateRequestAfterUpdateApi(model)
        }
    }

    /*---------------------UPDATE WORK ORDER Completed-------------------------*/
    suspend fun hitWorkOrderCompleted(token: String, model: UpdateWorkOrderCompletedRequest) {
        val allOrders = dao.getAllWorkOrders().first() // one-time snapshot of the list

        val anyInProgress = allOrders.any { order ->
            order.workStatus == CommonUtils.STATUS.INPROGRESS.sttausname
        }

        val sameOrderInProgress = allOrders.any { order ->
            order.workStatus == CommonUtils.STATUS.INPROGRESS.sttausname &&
                    order.workid == model.WorkId.toInt()
        }
        when {
            anyInProgress && !sameOrderInProgress -> {
                //  Block if some other order is in progress
                GlobalSnackbar.info("Agent already has a work order InProgress.")
                return
            }

            else -> {
                // No InProgress OR same order is InProgress → continue booking
            }
        }

        val isOnline = NetworkUtils.isOnline(MyApplication.appContext)

        try {
            if (isOnline) {
                val response =
                    apiService.updateWorkOrderCompleted(/*authorization = returnAccessToken(MyApplication.appContext),*/
                        model = model
                    )

                if (response.info.isSuccess) {
                    var statusType = 0
                    var status = ""
                    if (model.StatusId == CommonUtils.STATUS.INPROGRESS.type) {
                        status = CommonUtils.STATUS.INPROGRESS.sttausname
                        statusType = CommonUtils.STATUS.INPROGRESS.type
                    } else if (model.StatusId == (CommonUtils.STATUS.COMPLETED.type)) {
                        status = CommonUtils.STATUS.COMPLETED.sttausname
                        statusType = CommonUtils.STATUS.COMPLETED.type
                    }

                    dbUpdate.updateWorkOrderAndRequests1(
                        workId = model.WorkId.toInt(),
                        workStatusid = statusType,
                        status = status
                    )

                    queuePendingRequest(endpoint = "api/Request/UpdateRequest", model = model)
                    dbUpdate.updateWorkOrderCompletedAfterUpdateApi(model)
                    return
                } else {
                    dbUpdate.updateWorkOrderCompletedAfterUpdateApi(model)
                    return
                }
            }
            // ⏸ offline → queue + optimistic update
            Log.d("TAG", "hitUpdateRequest:Repos Model ---${model} ")
            queuePendingRequest(
                endpoint = "api/Request/UpdateRequest",
                model = model
            )
            dbUpdate.updateWorkOrderCompletedAfterUpdateApi(model)

        } catch (e: Exception) {
            //  exception → queue + optimistic update
            queuePendingRequest(endpoint = "api/Request/UpdateRequest", model = model)
            dbUpdate.updateWorkOrderCompletedAfterUpdateApi(model)
        }
    }

    /**********************hitupload file**********************/
    suspend fun hitUploadFileRequest(
        token: String,
        requestId: String,
        createdBy: String,
        uploadType: String,
        requestType: String,
        file: File?
    ) {
        if (file == null) return
        val isOnline = NetworkUtils.isOnline(MyApplication.appContext)
        val filePath = file.absolutePath


        val pendingModel = UploadFilePendingModel(
            RequestId = requestId,
            CreatedBy = createdBy,
            UploadType = uploadType,
            RequestType = requestType,
            File = filePath
        )

        try {
            if (isOnline) {
                // Prepare multipart
                val requestIdBody = requestId.toRequestBody("text/plain".toMediaTypeOrNull())
                val createdByBody = createdBy.toRequestBody("text/plain".toMediaTypeOrNull())
                val uploadTypeBody = uploadType.toRequestBody("text/plain".toMediaTypeOrNull())
                val requestTypeBody = requestType.toRequestBody("text/plain".toMediaTypeOrNull())
                val multipart = MultipartBody.Part.createFormData(
                    "File",
                    file.name,
                    file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )


                val response = apiService.uploadFileRequest(
                    requestId = requestIdBody,
                    createdBy = createdByBody,
                    UploadType = uploadTypeBody,
                    RequestType = requestTypeBody,
                    file = multipart
                )
                if (response.info.isSuccess) {
                    // mark as synced in DB
                    dbUpdate.updateFileInDb(requestId, filePath)
                    insertUploadedFileAsDownloaded(
                        requestId,
                        file,
                        uploadType,
                        requestType,
                        isSynced = true,
                        response.data.id
                    )
                    return
                } else {
                    //  API failed → queue for retry
                    queuePendingRequest(
                        endpoint = "api/Request/UploadFileRequest",
                        model = pendingModel
                    )
                    dbUpdate.updateFileInDb(requestId, filePath)
                    insertUploadedFileAsDownloaded(
                        requestId,
                        file,
                        uploadType,
                        requestType,
                        isSynced = false,
                        response.data.id
                    )
                    return
                }
            } else {

                // ⏸ offline → queue + optimistic insert
                queuePendingRequest(
                    endpoint = "api/Request/UploadFileRequest",
                    model = pendingModel
                )
                dbUpdate.updateFileInDb(requestId, filePath)
                insertUploadedFileAsDownloaded(
                    requestId,
                    file,
                    uploadType,
                    requestType,
                    isSynced = false,
                    ""
                )
            }
        } catch (e: Exception) {
            //  exception → queue + optimistic insert

            queuePendingRequest(endpoint = "api/Request/UploadFileRequest", model = pendingModel)
            dbUpdate.updateFileInDb(requestId, filePath)
            insertUploadedFileAsDownloaded(
                requestId,
                file,
                uploadType,
                requestType,
                isSynced = false,
                ""
            )
            Log.e("UploadFileRequest", "Error uploading file: ${e.message}", e)
        }
    }

    /**********download files**********/
    // Save the uploaded file locally (download table)
    private suspend fun insertUploadedFileAsDownloaded(
        requestId: String,
        file: File,
        uploadType: String,
        requestType: String,
        isSynced: Boolean,
        id: String
    ) {
        val entity = DownloadedFileEntity(
            requestId = requestId,
            filePath = file.absolutePath,
            originalFileName = file.name,
            uploadType = uploadType,
            isSynced = isSynced,
            requestType = requestType,
            id = id
        )
        downloadedFileDao.insertFiles(listOf(entity))
    }

    /*********************remove delete file********************/
    suspend fun hitDeleteFileRequest(
        token: String,
        requestId: Int,
        uploadType: String,
        requestType: String
    ) {
        val isOnline = NetworkUtils.isOnline(MyApplication.appContext)

        val pendingModel = DeleteUploadFileRequest(
            RequestType = requestType,
            RequestId = requestId,
            UploadType = uploadType
        )

        // Always remove immediately from DB
        dbUpdate.deleteFileFromDb(requestId, uploadType)

        try {
            if (isOnline) {
                val response = apiService.deleteUploadFile(
                    model = pendingModel
                )
                if (!response.info.isSuccess) {
                    //  Failed → queue retry
                    queuePendingRequest(
                        endpoint = "api/Request/DeleteUploadFile",
                        model = pendingModel
                    )
                }
            } else {
                // ⏸ Offline → queue retry
                queuePendingRequest(
                    endpoint = "api/Request/DeleteUploadFile",
                    model = pendingModel
                )
            }
        } catch (e: Exception) {
            // Exception → queue retry
            queuePendingRequest(
                endpoint = "api/Request/DeleteUploadFile",
                model = pendingModel
            )
        }
    }

    //this I am doing to refresh at that time
    suspend fun syncDownloadFilesForRequest(
        requestId: String,
        requestType: String
    ) {
        try {
            val response = apiService.downloadFile(
                requestId = requestId,
                requestType = requestType
            )

            if (response.info.isSuccess) {
                val fileEntities = DownloadFileMapper.mapListToEntityList(
                    response.data,
                    requestId,
                    requestType
                )
                downloadedFileDao.insertFiles(fileEntities)
            } else {
            }
        } catch (e: Exception) {
        }
    }

    /******************update cancellist****************/
    fun getCancelListFromDb(): Flow<List<CancelReasonListResponse.Data>> {
        return cancelReasonListDao.getAllValueTypes()
            .map { entities ->
                CancelRequestMapper.mapEntityListToCancelList(entities)
            }
    }

    suspend fun deviceUnbind(request: UnbindDeviceRequest): EmpResource<UnbindDeviceResponse> =
        safeApiCall { apiService.unbindDevice(request = request) }

}

