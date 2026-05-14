package com.pos10.domain

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pos10.data.repository.AuthRepository
import com.pos10.helper.EmpResource
import com.pos10.helper.InternetConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.pos10.MyApplication
import com.pos10.R
import com.pos10.db.dao.DownloadedFileDao
import com.pos10.db.dao.WorkOrderDao
import com.pos10.db.entity.WorkOrderEntity
import com.pos10.helper.CommonUtils
import com.pos10.helper.CommonUtils.parseDateToMillis
import com.pos10.model.local.AuthRequest
import com.pos10.model.local.CancelReasonListResponse
import com.pos10.model.local.GenerateOTPRequest
import com.pos10.model.local.UpdateRequest
import com.pos10.model.remote.AuthResponse
import com.pos10.model.remote.DownloadFileResponse
import com.pos10.model.remote.GenerateOTPResponse
import com.pos10.model.remote.MerchantDetailResponse
import com.pos10.model.remote.UpdateRequestResponse
import com.pos10.model.remote.ValidateOTPRequest
import com.pos10.view.screens.MerchantDetailsRequest
import java.io.File
import javax.inject.Inject
import com.pos10.model.local.FcmRequest
import com.pos10.model.local.GetAgenTrackHistoryRequest
import com.pos10.model.local.GetAgentFeedbackRequest
import com.pos10.model.local.NotificationToggleRequest
import com.pos10.model.local.QRCodeScanerRequest
import com.pos10.model.local.SaveApppointmentRequest
import com.pos10.model.local.UpdateWorkOrderCompletedRequest
import com.pos10.model.remote.GetFeedbackResponse
import com.pos10.model.remote.GetTerminalStatusResponse
import com.pos10.model.remote.GetTrackHistoryResponse
import com.pos10.model.remote.GetWorkListResponse
import com.pos10.model.remote.LocationTrackRequest
import com.pos10.model.remote.NotificationResponse
import com.pos10.model.remote.QrResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val dao: WorkOrderDao,
    private val downloadedFileDao: DownloadedFileDao,
    private val authRepository: AuthRepository
) : ViewModel() {

    var type by mutableStateOf(0)
    var isNetworkAvailable = MutableLiveData(true)
    var uploadType by mutableStateOf(0)
    val selectDatePicker = mutableStateOf("DD/MM/YYYY")
    val selectDatePickerStartDate = mutableStateOf("DD/MM/YYYY")
    val selectDatePickerEndDate = mutableStateOf("DD/MM/YYYY")
    val selectTimePicker = mutableStateOf("HH:MM:SS")
    var isPdf = MutableLiveData(true)
    var updatestatus by mutableStateOf(false)
    var updateQr by mutableStateOf(false)
    var rescheduleRefresh by mutableStateOf(false)
    val selectedDateMillis = mutableStateOf(System.currentTimeMillis())

    // add param
    var selectedChipListWatch by mutableStateOf("")
    var otpId by mutableStateOf(0)
    var showDialog by mutableStateOf(false)
    var showDialogActive by mutableStateOf(false)
    var terminalStatus by mutableStateOf("")
    var showDialogQr by mutableStateOf(false)
    var showDatePicker by mutableStateOf(false)
    var showTimePicker by mutableStateOf(false)
    var showDatePickerHistory by mutableStateOf(false)
    var showDatePickerEndHistory by mutableStateOf(false)
    var saveDataRequest by mutableStateOf<GetWorkListResponse.Data?>(null)
    var requestListData by mutableStateOf(ArrayList<GetWorkListResponse.Data.Wo>())
    var requestListChecklistData by mutableStateOf(ArrayList<GetWorkListResponse.Data.Checklist>())
    var feedbackData by mutableStateOf(ArrayList<GetFeedbackResponse.Data>())
    var requestsubworkListData by mutableStateOf(ArrayList<GetWorkListResponse.Data.Wo.WoRequest>())
    var downloadfilelistData by mutableStateOf(ArrayList<DownloadFileResponse.Data>())
    var selectedDateLong = mutableStateOf("")
    var selectedStartDateLong = mutableStateOf("")
    var selectedEndDateLong = mutableStateOf("")
    var selectedTimeLong = mutableStateOf("")
    var capturesignatureDialog by mutableStateOf(false)
    var selectedOptionModel = mutableStateOf("Installation")
    var uploadDocType = mutableStateOf("3904")
    var uploadRequestType = mutableStateOf("R")
    var simno = mutableStateOf("")
    var serialnumber = mutableStateOf("")

    fun checkInternetConnection(): Boolean {
        return if (InternetConnection.checkConnection(MyApplication.appContext)) {
            true
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                isNetworkAvailable.value = false
            }
            false
        }
    }

    private val _loginLiveData = MutableLiveData<EmpResource<AuthResponse>>()
    val loginLiveData: LiveData<EmpResource<AuthResponse>>
        get() = _loginLiveData

    fun hitLogin(model: AuthRequest) {
        if (checkInternetConnection()) {
            viewModelScope.launch {
                _loginLiveData.value = EmpResource.Loading
                _loginLiveData.value = authRepository.hitLogin(model)
            }
        } else {
            Toast.makeText(
                MyApplication.appContext,
                MyApplication.appContext.getString(R.string.no_network_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val _merchantDetailLiveData = MutableLiveData<EmpResource<MerchantDetailResponse>>()
    val merchantDetailLiveData: LiveData<EmpResource<MerchantDetailResponse>>
        get() = _merchantDetailLiveData

    fun hitMerchantDetail(model: MerchantDetailsRequest) {
        if (checkInternetConnection()) {
            viewModelScope.launch {
                _merchantDetailLiveData.value = EmpResource.Loading
                _merchantDetailLiveData.value = authRepository.hitMerchantDetail(model)
            }
        } else {
            Toast.makeText(
                MyApplication.appContext,
                MyApplication.appContext.getString(R.string.no_network_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val _terminalstatusLiveData = MutableLiveData<EmpResource<GetTerminalStatusResponse>>()
    val terminalstatusLiveData: LiveData<EmpResource<GetTerminalStatusResponse>>
        get() = _terminalstatusLiveData

    fun hitTerminalStatus(token: String, terminalStatus: String) {
        if (checkInternetConnection()) {
            viewModelScope.launch {
                _terminalstatusLiveData.value = EmpResource.Loading
                _terminalstatusLiveData.value =
                    authRepository.hitTerminalStatus(token, terminalStatus)
            }
        } else {
            Toast.makeText(
                MyApplication.appContext,
                MyApplication.appContext.getString(R.string.no_network_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val _updateLiveData = MutableLiveData<EmpResource<UpdateRequestResponse>>()
    val updateLiveData: LiveData<EmpResource<UpdateRequestResponse>>
        get() = _updateLiveData


    private val _getGenerateOtpLiveData = MutableLiveData<EmpResource<GenerateOTPResponse>>()
    val getGenerateOtpLiveData: LiveData<EmpResource<GenerateOTPResponse>>
        get() = _getGenerateOtpLiveData

    fun hitGetGenerateOTP(token: String, model: GenerateOTPRequest) {
        if (checkInternetConnection()) {
            viewModelScope.launch {
                _getGenerateOtpLiveData.value = EmpResource.Loading
                _getGenerateOtpLiveData.value = authRepository.hitGetGenerateOTP(token, model)
            }
        } else {
            Toast.makeText(
                MyApplication.appContext,
                MyApplication.appContext.getString(R.string.no_network_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val _validateOtpLiveData = MutableLiveData<EmpResource<GenerateOTPResponse>>()
    val validateOtpLiveData: LiveData<EmpResource<GenerateOTPResponse>>
        get() = _validateOtpLiveData

    fun hitValidateOTP(token: String, model: ValidateOTPRequest) {
        if (checkInternetConnection()) {
            viewModelScope.launch {
                _validateOtpLiveData.value = EmpResource.Loading
                _validateOtpLiveData.value = authRepository.hitValidateOTP(token, model)
            }
        } else {
            Toast.makeText(
                MyApplication.appContext,
                MyApplication.appContext.getString(R.string.no_network_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val _locationtrackLiveData = MutableLiveData<EmpResource<QrResponse>>()
    val locationtrackLiveData: LiveData<EmpResource<QrResponse>>
        get() = _locationtrackLiveData

    fun hitLocationTrack(token: String, model: LocationTrackRequest) {
        if (checkInternetConnection()) {
            viewModelScope.launch {
                _locationtrackLiveData.value = EmpResource.Loading
                _locationtrackLiveData.value = authRepository.hitLocationTrackRequest(token, model)
            }
        } else {
            Toast.makeText(
                MyApplication.appContext,
                MyApplication.appContext.getString(R.string.no_network_found), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val _historytrackLiveData = MutableLiveData<EmpResource<GetTrackHistoryResponse>>()
    val historytrackLiveData: LiveData<EmpResource<GetTrackHistoryResponse>>
        get() = _historytrackLiveData

    fun hitHistoryTrack(token: String, model: GetAgenTrackHistoryRequest) {
        if (checkInternetConnection()) {
            viewModelScope.launch {
                _historytrackLiveData.value = EmpResource.Loading
                _historytrackLiveData.value = authRepository.hitAgentTrackHistory(token, model)
            }
        } else {
            Toast.makeText(
                MyApplication.appContext,
                MyApplication.appContext.getString(R.string.no_network_found), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val _fcmTokenLiveData = MutableLiveData<EmpResource<QrResponse>>()
    val fcmTokenLiveData: LiveData<EmpResource<QrResponse>>
        get() = _fcmTokenLiveData

    fun hitFcmToken(token: String, model: FcmRequest) {
        if (checkInternetConnection()) {
            viewModelScope.launch {
                _fcmTokenLiveData.value = EmpResource.Loading
                _fcmTokenLiveData.value = authRepository.hitFieldAgentFcmtoken(token, model)
            }
        } else {
            Toast.makeText(
                MyApplication.appContext,
                MyApplication.appContext.getString(R.string.no_network_found), Toast.LENGTH_SHORT
            ).show()
        }
    }


    private val _fieldFeedbackLiveData = MutableLiveData<EmpResource<GetFeedbackResponse>>()
    val fieldFeedbackLiveData: LiveData<EmpResource<GetFeedbackResponse>>
        get() = _fieldFeedbackLiveData

    fun hitFeedbackRequest(token: String, model: GetAgentFeedbackRequest) {
        if (checkInternetConnection()) {
            viewModelScope.launch {
                _fieldFeedbackLiveData.value = EmpResource.Loading
                _fieldFeedbackLiveData.value = authRepository.hitFieldAgentFeedbck(token, model)
            }
        } else {
            Toast.makeText(
                MyApplication.appContext,
                MyApplication.appContext.getString(R.string.no_network_found), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val _notifictionStatusLiveData = MutableLiveData<EmpResource<NotificationResponse>>()
    val notifictionStatusLiveData: LiveData<EmpResource<NotificationResponse>>
        get() = _notifictionStatusLiveData

    fun hitNotificationToggle(token: String, model: NotificationToggleRequest) {
        if (checkInternetConnection()) {
            viewModelScope.launch {
                _notifictionStatusLiveData.value = EmpResource.Loading
                _notifictionStatusLiveData.value =
                    authRepository.hitNotificationStatus(token, model)
            }
        } else {
            Toast.makeText(
                MyApplication.appContext,
                MyApplication.appContext.getString(R.string.no_network_found), Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Offline Feature
    // Full list of work orders from DB
    private val _allWorkOrders: StateFlow<List<GetWorkListResponse.Data.Wo>> =
        authRepository.getWorkOrdersFromDb()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkOrders: StateFlow<List<GetWorkListResponse.Data.Wo>>
        get() = _allWorkOrders

    // Selected filters
    private val _selectedRequestType = MutableStateFlow<String?>(null)
    private val _selectedStatus = MutableStateFlow<List<String>?>(null)

    // Exposed filtered work orders
    val filteredWorkOrders: StateFlow<List<GetWorkListResponse.Data.Wo>> =
        combine(_allWorkOrders, _selectedRequestType, _selectedStatus) { all, type, status ->
            Log.d("TAG", "Tabs-----$status: ")
            all.flatMap { wo ->
                val requests = wo.woRequest ?: emptyList()

                // Step 1: apply requestType + requestStatus filter on child requests
                val matchingReqs = requests.filter { req ->
                    val typeMatch =
                        type?.let { t -> req.requesttype.contains(t, ignoreCase = true) } ?: true
                    val statusMatch =
                        status?.let { statusList ->
                            statusList.any { s ->
                                req.status.equals(s, ignoreCase = true)
                            }
                        } ?: true

                    typeMatch && statusMatch
                }

                // Step 2: group matching requests by requestType
                matchingReqs.groupBy { it.requesttype }
                    .values
                    .map { groupedReqs ->
                        // Step 3: make ONE Wo per requestType group, containing all its requestNos
                        wo.copy(woRequest = groupedReqs as ArrayList)
                    }
            }
                .sortedWith(
                    compareBy<GetWorkListResponse.Data.Wo> {
                        when (it.priority?.trim()?.lowercase()) {
                            "high", "1" -> 1
                            "medium", "2" -> 2
                            "low", "3" -> 3
                            else -> 4
                        }
                    }.thenByDescending { it.workid }
                )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Functions to update filters
    fun setRequestType(type: String?) {
        _selectedRequestType.value = type
    }

    fun setWorkStatus(status: List<String>?) {
        _selectedStatus.value = status
    }

    //status count (assigned , inprogress, completed, failed)
    //count is requestwise
    val workOrderStatusCounts: StateFlow<Map<String, Int>> =
        combine(_allWorkOrders, _selectedRequestType) { allWorkOrders, selectedType ->
            withContext(Dispatchers.Default) {
                val now = System.currentTimeMillis()

                val allRequestsWithDueDate = allWorkOrders.flatMap { wo ->
                    val dueMillis = parseDateToMillis(wo.dueDate)
                    (wo.woRequest ?: emptyList()).map { req -> req to dueMillis }
                }

                val filtered = allRequestsWithDueDate.filter { (req, _) ->
                    selectedType?.let { t -> req.requesttype.equals(t, ignoreCase = true) } ?: true
                }

                val baseCounts = filtered
                    .groupingBy { (req, _) -> req.status.trim().lowercase() }
                    .eachCount()
                    .toMutableMap()

                val newCount = filtered.count { (req, dueMillis) ->
                    val isValidStatus =
                        req.status.equals(
                            CommonUtils.STATUS.ASSIGNED.sttausname,
                            ignoreCase = true
                        ) ||
                                req.status.equals(
                                    CommonUtils.STATUS.ITEMPACKED.sttausname,
                                    ignoreCase = true
                                )

                    isValidStatus && (dueMillis == null || dueMillis >= now)
                }

                val delayedCount = filtered.count { (req, dueMillis) ->
                    val isValidStatus =
                        req.status.equals(
                            CommonUtils.STATUS.ASSIGNED.sttausname,
                            ignoreCase = true
                        ) ||
                                req.status.equals(
                                    CommonUtils.STATUS.ITEMPACKED.sttausname,
                                    ignoreCase = true
                                )

                    isValidStatus && (dueMillis != null && dueMillis < now)
                }

                baseCounts["new"] = newCount
                baseCounts["delayed"] = delayedCount
                baseCounts["in progress"] =
                    baseCounts[CommonUtils.STATUS.INPROGRESS.sttausname.lowercase()] ?: 0
                baseCounts["completed"] =
                    baseCounts[CommonUtils.STATUS.COMPLETED.sttausname.lowercase()] ?: 0
                baseCounts["failed"] =
                    baseCounts[CommonUtils.STATUS.FAILED.sttausname.lowercase()] ?: 0
                baseCounts
            }
        }.flowOn(Dispatchers.Default) // ensures flow runs on background thread
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyMap()
            )

    //this is for work order complete
    fun isWorkRequestClosed(originalWo: GetWorkListResponse.Data.Wo): Boolean {
        val allRequests = originalWo.woRequest ?: return false
        if (allRequests.isEmpty()) return false

        return allRequests.all { req ->
            req.status.equals(CommonUtils.STATUS.COMPLETED.sttausname, ignoreCase = true) ||
                    req.status.equals(CommonUtils.STATUS.FAILED.sttausname, ignoreCase = true)
        }
    }

    //metadata
    private val _workMetaData: StateFlow<GetWorkListResponse.Data?> =
        authRepository.getWorkMetaDataFromDb()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Expose publicly as immutable
    val workMetaData: StateFlow<GetWorkListResponse.Data?> = _workMetaData

    fun scheduleWorkAppointment(model: SaveApppointmentRequest, token: String) {
        viewModelScope.launch {
            Log.d("TAG", "scheduleWorkAppointment:Recshedule ----${rescheduleRefresh} ")
            authRepository.scheduleWorkAppointment(token, model)
        }
    }

    private val _requestId = MutableStateFlow<String?>(null)
    private val _requestType = MutableStateFlow<String?>(null)

    // All files for given requestId + requestType
    @OptIn(ExperimentalCoroutinesApi::class)
    val allFiles: StateFlow<List<DownloadFileResponse.Data>> =
        combine(
            _requestId.filterNotNull(),
            _requestType.filterNotNull()
        ) { requestId, requestType ->
            requestId to requestType
        }
            .flatMapLatest { (requestId, requestType) ->
                authRepository.getDownloadedFilesFromDb(requestId, requestType)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRequestId(requestId: String) {
        _requestId.value = requestId
    }

    fun setRequestTypeee(requestType: String) {
        _requestType.value = requestType
    }

    //upload file
    fun uploadFile(
        token: String,
        createdBy: String,
        uploadType: String,
        requestTye: String,
        file: File
    ) {
        viewModelScope.launch {
            _requestId.value?.let {
                authRepository.hitUploadFileRequest(
                    token,
                    it,
                    createdBy,
                    uploadType,
                    requestTye,
                    file
                )
            }
        }
    }

    //delete upload file
    fun removeImage(
        uri: Uri,
        requestId: Int,
        uploadType: String,
        token: String,
        requestTye: String
    ) {
        viewModelScope.launch {
            try {
                val filePath = when {
                    uri.scheme == "file" -> uri.path?.let { File(it).absolutePath }
                    uri.scheme?.startsWith("http") == true -> uri.toString()
                    else -> uri.toString()
                }

                filePath?.let {
                    // 1. remove locally from DB
                    downloadedFileDao.deleteFileByPath(requestId)

                    // 2. Call API/queue for delete sync
                    authRepository.hitDeleteFileRequest(
                        token = token,
                        requestId = requestId,
                        uploadType = uploadType,
                        requestType = requestTye
                    )
                } ?: Log.e("removeImage", "Skipped removal - null filepath")
            } catch (e: Exception) {
            }
        }
    }


    //qr
    val workOrders: StateFlow<List<WorkOrderEntity>> =
        dao.getAllWorkOrdersFlow()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Call QRCode hit (just updates DB)
    fun hitQRCode(model: QRCodeScanerRequest) {
        viewModelScope.launch {
            authRepository.hitQRCode(model)
        }
    }

    private val _updateSuccess = MutableSharedFlow<String>() // emit status
    val updateSuccess = _updateSuccess.asSharedFlow()

    fun hitUpdateRequest(model: UpdateRequest, status: String) {
        viewModelScope.launch {
            authRepository.hitUpdateRequest(model)
            _updateSuccess.emit(status)  // instead of true
        }
    }

    private val _updateWorkOrderCompleted = MutableSharedFlow<String>() // emit status
    val updateWorkOrderCompleted = _updateWorkOrderCompleted.asSharedFlow()

    fun hitUpdateWorkOrderCompleted(
        token: String,
        model: UpdateWorkOrderCompletedRequest,
        status: String
    ) {
        viewModelScope.launch {
            authRepository.hitWorkOrderCompleted(token, model)
            _updateWorkOrderCompleted.emit(status)
        }
    }

    fun refreshFiles(requestId: String, requestType: String) {
        viewModelScope.launch {
            authRepository.syncDownloadFilesForRequest(requestId, requestType)
        }
    }

    /***********update cancel list**********/
    private val _allCancelList: StateFlow<List<CancelReasonListResponse.Data>> =
        authRepository.getCancelListFromDb()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCancelList: StateFlow<List<CancelReasonListResponse.Data>>
        get() = _allCancelList


    //check
    val distanceMap = mutableStateMapOf<String, String>()
    var currentAddress by mutableStateOf<String?>(null)

    fun calculateDistanceFromCurrentToDestination(
        context: Context,
        currentLat: Double,
        currentLng: Double,
        destinationAddress: String?,
        workOrderNo: String,
        merchantLatLong: String?
    ) {
        if (distanceMap.containsKey(workOrderNo)) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Step 1: Reverse geocode current location
                val startAddress = CommonUtils.getAddressFromLatLng(context, currentLat, currentLng)
                if (startAddress != null) {
                    withContext(Dispatchers.Main) {
                        currentAddress = startAddress
                    }
                }
                Log.d(
                    "MerchantLatlong",
                    "merchantLatLong:-$merchantLatLong" + "destinationAddress:- $destinationAddress" + "workOrderNo: $workOrderNo"
                )
                // Step 2: Geocode destination address
                val destLatLng: Pair<Double, Double>? =
                    merchantLatLong
                        ?.split(",")
                        ?.map { it.trim() }
                        ?.takeIf { it.size == 2 }
                        ?.let {
                            val lat = it[0].toDoubleOrNull()
                            val lng = it[1].toDoubleOrNull()
                            if (lat != null && lng != null) lat to lng else null
                        }
                        ?: destinationAddress?.let {
                            CommonUtils.getLatLngFromAddress(context, it)
                        }
                Log.d("DISTANCE_DEBUG", "destLatLng = $destLatLng")
                if (destLatLng != null) {
                    val (destLat, destLng) = destLatLng
                    val km = CommonUtils.getDistanceInKm(currentLat, currentLng, destLat, destLng)
                    val formatted = "%.2f km".format(km)

                    withContext(Dispatchers.Main) {
                        distanceMap[workOrderNo] = formatted
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        distanceMap[workOrderNo] = "Unknown"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}