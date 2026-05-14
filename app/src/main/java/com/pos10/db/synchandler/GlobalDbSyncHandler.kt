package com.pos10.db.synchandler

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pos10.db.dao.AgentTrackHistoryDao
import com.pos10.db.dao.CancelReasonListDao
import com.pos10.db.dao.DownloadedFileDao
import com.pos10.db.dao.FileDao
import com.pos10.db.dao.PendingRequestDao
import com.pos10.db.dao.WorkMetaDataDao
import com.pos10.db.dao.WorkOrderDao
import com.pos10.db.entity.DownloadedFileEntity
import com.pos10.helper.CommonUtils
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.CustomLoader
import com.pos10.helper.GlobalSnackbar
import com.pos10.helper.NetworkService
import com.pos10.helper.NetworkUtils.isOnline
import com.pos10.helper.SharedPreference
import com.pos10.helper.dbupdate.DbUpdate
import com.pos10.helper.mapper.CancelRequestMapper
import com.pos10.helper.mapper.DownloadFileMapper
import com.pos10.helper.mapper.WorkMetaDataMapper
import com.pos10.helper.mapper.WorkOrderMapper
import com.pos10.model.local.DeleteUploadFileRequest
import com.pos10.model.local.GetWorkRequestLocal
import com.pos10.model.local.QRCodeScanerRequest
import com.pos10.model.local.SaveApppointmentRequest
import com.pos10.model.local.UpdateRequest
import com.pos10.model.local.UpdateWorkOrderCompletedRequest
import com.pos10.model.remote.GetWorkListResponse
import com.pos10.network.ApiServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalDbSyncHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workOrderDao: WorkOrderDao,
    private val workMetaDataDao: WorkMetaDataDao,
    private val pendingRequestDao: PendingRequestDao,
    private val downloadedFileDao: DownloadedFileDao,
    private val fileDao: FileDao,
    private val agentTrackHistoryDao: AgentTrackHistoryDao,
    private val dbUpdate: DbUpdate,
    private val gson: Gson,
    private val apiService: ApiServices,
    private val cancelReasonListDao: CancelReasonListDao) {

    private val networkService = NetworkService.getInstance(context)
    private var isSyncing = false

    /**
     * Observes network changes and syncs work orders automatically.
     * Must be called from a LifecycleOwner (Activity/Fragment).
     */
    fun observeNetworkAndSync(lifecycleOwner: LifecycleOwner) {
        networkService.isOnline.observe(lifecycleOwner, Observer { online ->
            if (online) {
                GlobalSnackbar.info("User is Online")
                manualSync(lifecycleOwner)
            }else{
                GlobalSnackbar.info("User is Offline")
            }
        })
    }

    /**
     * Manually triggers sync with lifecycle-aware coroutine
     */
    fun manualSync(lifecycleOwner: LifecycleOwner? = null, activity: Activity? = null,
                   onInvalidToken: (() -> Unit)? = null) {
        if (!networkService.getCurrentStatus()) {
            GlobalSnackbar.error("No network connection. Cannot sync.")
            return
        }

        if (isSyncing) {
            Log.d("GlobalDbSyncHandler", "Sync already in progress, skipping new request.")
            return
        }
        isSyncing = true

        val scope = lifecycleOwner?.lifecycleScope ?: CoroutineScope(Dispatchers.IO)

        scope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                   // activity?.let { CustomLoader.showLoader(it) }
                    GlobalSnackbar.success("Sync process started…")
                }

                Log.d("GlobalDbSyncHandler", "---- Starting sync process ----")

                // 1. Sync pending requests
                syncPendingRequests()

                // 2. Fetch fresh work orders
                val model = GetWorkRequestLocal(0, 0, SharedPreference.get(context).name)
                val response = try {
                    apiService.getWorkRequestList(
                        model = model)
                } catch (networkException: Exception) {
                    Log.e("GlobalDbSyncHandler", "Network error during sync", networkException)
                    val message = networkException.message.orEmpty()
                    if (message.contains("HTTP 401", ignoreCase = true)) {
                        Log.d("TAG", "setupSync: ------newwGlobal")
                        withContext(Dispatchers.Main) {
                            CustomLoader.hideLoader()
                            onInvalidToken?.invoke()
                        }
                        return@launch
                    }
                    withContext(Dispatchers.Main) {
                        CustomLoader.hideLoader()
                        GlobalSnackbar.error("Network error during sync")
                    }
                    return@launch
                }

                // ---- ADD Cancel List API Call ----
                try {
                    val cancelListResponse = apiService.getCancelList(
//                        authorization = returnAccessToken(context),
                        typeId = "49")

                    if(cancelListResponse.info.isSuccess){
                        val cancelList = cancelListResponse.data ?: emptyList()
                        val cancelEntities = CancelRequestMapper.mapCancelListToEntityList(cancelList)
                        cancelReasonListDao.clearAll()
                        cancelReasonListDao.insertAll(cancelEntities)
                    } else {
                        Log.w("GlobalDbSyncHandler", "CancelList API error: ${cancelListResponse.info.message}")
                    }
                } catch (networkException: Exception) {
                    Log.e("GlobalDbSyncHandler", "Network error during CancelList sync", networkException)
                    val message = networkException.message.orEmpty()
                    if (message.contains("HTTP 401", ignoreCase = true)) {
                        withContext(Dispatchers.Main) {
                            CustomLoader.hideLoader()
                            onInvalidToken?.invoke()
                        }
                        return@launch
                    }
                }

                if (response.info.isSuccess) {
                    val workList = response.data.woList
                    val entities = WorkOrderMapper.mapWoListToEntityList(workList)
                    val metaEntities = WorkMetaDataMapper.mapToEntity(response.data)

                    Log.d("TAG", "manualSync:WorkList ---$workList ")
                    try {

                        /**
                         * This is used to delete because after update
                         * if wo doesn't exist then it will delete
                         */
                        val newIds = entities.map { it.workid }
                        if (newIds.isNotEmpty()) {
                            workOrderDao.deleteNotIn(newIds)
                        }

                        // Insert WorkOrders + MetaData
                        workOrderDao.insertWorkOrders(entities)
                        workMetaDataDao.insertWorkMetaData(metaEntities)
                        Log.d("TAG", "manualSync:WorkList Ent ---$entities ")

                        // 3. Sync DownloadFiles for each requestId
                        syncDownloadFiles(workList)

                        withContext(Dispatchers.Main) {
                            CustomLoader.hideLoader()
                            GlobalSnackbar.success("Sync process completed")
                            Log.d("GlobalDbSyncHandler", "---- Sync completed successfully ----")
                        }
                    } catch (dbException: Exception) {
                        Log.e("GlobalDbSyncHandler", "Database error during sync", dbException)
                        withContext(Dispatchers.Main) {
                            CustomLoader.hideLoader()
                            GlobalSnackbar.error( "Database error during sync")
                        }
                    }
                } else {
                    Log.w("GlobalDbSyncHandler", "API error: ${response.info.message}")
                    withContext(Dispatchers.Main) {
                        CustomLoader.hideLoader()
                        GlobalSnackbar.error("API Error: ${response.info.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("GlobalDbSyncHandler", "Unexpected error during sync", e)
                withContext(Dispatchers.Main) {
                    CustomLoader.hideLoader()
                    GlobalSnackbar.error("Unexpected error during sync")
                }
            } finally {
                isSyncing = false
            }
        }
    }

    private suspend fun syncPendingRequests() {
        val pendingList = pendingRequestDao.getAllPendingRequests()

        if (pendingList.isEmpty()) {
            Log.d("GlobalDbSyncHandler", "No pending requests in queue.")
            return
        }

        Log.d("GlobalDbSyncHandler", "Starting sync of ${pendingList.size} pending requests...")

        for ((index, request) in pendingList.withIndex()) {
            try {
                Log.d("GlobalDbSyncHandler", "[${index + 1}/${pendingList.size}] Executing pending request for endpoint: ${request.endpoint}")

                when (request.endpoint) {
                    "api/WorkAssignment/UpdateAppointmentDetails" -> {
                        val model = gson.fromJson(request.payload, SaveApppointmentRequest::class.java)
                        val token = returnAccessToken(context)

                        val response = apiService.saveAppointment(
                            model = model)

                        if (response.info.isSuccess) {
                            Log.d("GlobalDbSyncHandler",
                                "Success: Appointment scheduled for WorkID=${model.workId}, removing from queue")
                            pendingRequestDao.deletePendingRequest(request)

                            //  Update DB with final server-confirmed state (WorkOrder + children)
                            dbUpdate.updateWorkOrderAndRequests(
                                workId = model.workId,
                                workStatusid = CommonUtils.STATUS.INPROGRESS.type,
                                status = CommonUtils.STATUS.INPROGRESS.sttausname,
                                appointmentDate = model.appointmentDate,
                                appointmentTime = model.appointmentTime
                            )
                        } else {
                            withContext(Dispatchers.Main) {
                                GlobalSnackbar.error(" ${response.info.message}")
                            }
                            Log.e("GlobalDbSyncHandler", "Failed (server responded with error) for WorkID=${model.workId}, keeping in queue")
                        }
                    }

                    "api/Request/UpdateDeviceRequest"->{
                        val model = gson.fromJson(request.payload, QRCodeScanerRequest::class.java)
                        val token = returnAccessToken(context)

                        val response = apiService.qrcodeScanner( model = model)

                        if (response.info.isSuccess) {
                            Log.d("GlobalDbSyncHandler", "Success QR → requestId=${model.RequestId}")
                            pendingRequestDao.deletePendingRequest(request)
                            dbUpdate.updateRequestAfterQRCode(model.RequestId, "1", model.SerialNumber,model.SimNumber)
                        } else {
                            Log.e("GlobalDbSyncHandler", "Failed QR for requestId=${model.RequestId}")
                        }
                    }

                    "api/Request/UpdateRequest" -> {
                        val model = gson.fromJson(request.payload, UpdateRequest::class.java)
                        val token = returnAccessToken(context)

                        Log.d("TAG", "syncPendingRequests:GlobalModel ---${model} ")
                        try {
                            val response = apiService.updateRequest(/*authorization = token,*/ model = model)
                            if (response.info.isSuccess) {
                                pendingRequestDao.deletePendingRequest(request)
                            }
                            //  Always update DB safely (offline-safe & status-safe)
                            dbUpdate.updateRequestAfterUpdateApi(model)
                        } catch (e: Exception) {
                            // Keep in queue, but still update DB
                            Log.d("TAG", "syncPendingRequests: Exception --${e.message}--${e.localizedMessage}")
                        }
                    }

                    "api/Request/UploadFileRequest" -> {
                        try {
                            val mapType = object : TypeToken<Map<String, String>>() {}.type
                            val model: Map<String, String> = gson.fromJson(request.payload, mapType)

                            val requestId = model["RequestId"] ?: ""
                            val createdBy = model["CreatedBy"] ?: ""
                            val uploadType=model["UploadType"]?:""
                            val requestType=model["RequestType"]?:""
                            val filePath = model["File"] ?: ""
                            val file = File(filePath)

                            if (file.exists()) {
                                val token = returnAccessToken(context)

                                val requestIdBody = requestId.toRequestBody("text/plain".toMediaTypeOrNull())
                                val createdByBody = createdBy.toRequestBody("text/plain".toMediaTypeOrNull())
                                val uploadTypeBody = uploadType.toRequestBody("text/plain".toMediaTypeOrNull())
                                val requestTypeBody = requestType.toRequestBody("text/plain".toMediaTypeOrNull())
                                val multipart = MultipartBody.Part.createFormData(
                                    "File",
                                    file.name,
                                    file.asRequestBody("image/jpeg".toMediaTypeOrNull()))

                                val response = apiService.uploadFileRequest(
//                                    authorization = token,
                                    requestId = requestIdBody,
                                    createdBy = createdByBody,
                                    UploadType = uploadTypeBody,
                                    RequestType = requestTypeBody,
                                    file = multipart)

                                if (response.info.isSuccess) {
                                    Log.d("GlobalDbSyncHandler", "File uploaded successfully for requestId=$requestId, removing from queue")
                                    pendingRequestDao.deletePendingRequest(request)

                                    //  Update DB → mark file as synced
                                    dbUpdate.updateFileInDb(requestId, filePath)
                                    insertUploadedFileAsDownloaded(requestId, file,uploadType,requestType, false, response.data.id)

                                } else {
                                    Log.e("GlobalDbSyncHandler", "File upload failed for requestId=$requestId, keeping in queue")
                                    dbUpdate.updateFileInDb(requestId, filePath)
                                    insertUploadedFileAsDownloaded(requestId, file, uploadType,requestType,false, "")

                                }
                            } else {
                                Log.e("GlobalDbSyncHandler", "File not found at $filePath → removing from queue")
                                pendingRequestDao.deletePendingRequest(request)
                            }
                        } catch (e: Exception) {
                            Log.e("GlobalDbSyncHandler", "Exception uploading file: ${e.message}", e)
                            // Keep in queue for retry
                        }
                    }

                    "api/WorkAssignment/AddOrUpdateWorkOrder" -> {
                        val model = gson.fromJson(request.payload, UpdateWorkOrderCompletedRequest::class.java)
                        val token = returnAccessToken(context)

                        Log.d("TAG", "syncPendingRequests:GlobalModel ---${model} ")
                        try {
                            val response = apiService.updateWorkOrderCompleted(/*authorization = token,*/ model = model)
                            if (response.info.isSuccess) {
                                pendingRequestDao.deletePendingRequest(request)
                            }
                            //  Always update DB safely (offline-safe & status-safe)
                            dbUpdate.updateWorkOrderCompletedAfterUpdateApi(model)
                        } catch (e: Exception) {
                            // Keep in queue, but still update DB
                            Log.d("TAG", "syncPendingRequests: Exception --${e.message}--${e.localizedMessage}")
                        }
                    }

                    "api/Request/DeleteUploadFile" -> {
                        val model = gson.fromJson(request.payload, DeleteUploadFileRequest::class.java)
                        val token = returnAccessToken(context)

                        Log.d("TAG", "syncPendingRequests: DeleteFileModel ---$model")
                        try {
                            //  Always remove from DB first
                            dbUpdate.deleteFileFromDb(model.RequestId, model.UploadType)

                            // Attempt API call if online
                            if (isOnline(context)) {
                                val response = apiService.deleteUploadFile(/*authorization = token,*/ model = model)
                                if (response.info.isSuccess) {
                                    pendingRequestDao.deletePendingRequest(request)
                                } else {
                                    // Failed → keep in queue
                                }
                            } else {
                                // ⏸ Offline → keep in queue
                            }
                        } catch (e: Exception) {
                            // Exception → keep in queue
                        }
                    }

                    else -> {
                    }
                }
            } catch (e: Exception) {
                break
            }
        }

        val remaining = pendingRequestDao.getAllPendingRequests().size
        if (remaining == 0) {
        } else {
        }
    }

    private suspend fun insertUploadedFileAsDownloaded(requestId: String, file: File,uploadType:String,requestTYpe:String, isSynced: Boolean, id: String) {
        val entity = DownloadedFileEntity(
            requestId = requestId,
            filePath = file.absolutePath,
            originalFileName = file.name,
            uploadType = uploadType,
            isSynced = isSynced,
            requestType = requestTYpe,
            id=id
            )
        downloadedFileDao.insertFiles(listOf(entity))
    }

    //this is new
    private suspend fun syncDownloadFiles(workList: List<GetWorkListResponse.Data.Wo>) {
        val workRequests = workList.flatMap { wo ->
            wo.woRequest.map { req ->
                wo.workid to req.requestid   // Pair<workid, requestid>
            }
        }

        workRequests.forEach { (workId, requestId) ->
            try {
                listOf("W", "R").forEach { type ->
                    val idToSend = if (type == "W") workId.toString() else requestId

                    val response = apiService.downloadFile(
                        requestId = idToSend,
                        requestType = type)

                    if (response.info.isSuccess) {
                        val fileEntities = DownloadFileMapper.mapListToEntityList(
                            response.data,
                            idToSend,
                            type
                        )
                        Log.d("TAG", "syncDownloadFiles:InsertFilesNew----$fileEntities")
                        downloadedFileDao.insertFiles(fileEntities)
                    } else {
                    }
                }
            } catch (fileEx: Exception) {
            }
        }
    }
}