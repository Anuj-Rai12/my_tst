package com.pos10.network

import com.pos10.MyApplication
import com.pos10.helper.SharedPreference
import com.pos10.model.local.CancelReasonListResponse
import com.pos10.model.local.DeleteUploadFileRequest
import com.pos10.model.local.FcmRequest
import com.pos10.model.local.GenerateOTPRequest
import com.pos10.model.local.GetAgenTrackHistoryRequest
import com.pos10.model.local.GetAgentFeedbackRequest
import com.pos10.model.local.GetRequestList
import com.pos10.model.local.GetWorkRequestLocal
import com.pos10.model.local.NotificationToggleRequest
import com.pos10.model.local.QRCodeScanerRequest
import com.pos10.model.local.SaveApppointmentRequest
import com.pos10.model.local.UnbindDeviceRequest
import com.pos10.model.local.UpdateRequest
import com.pos10.model.local.UpdateWorkOrderCompletedRequest
import com.pos10.model.remote.AuthResponse
import com.pos10.model.remote.DownloadFileResponse
import com.pos10.model.remote.GenerateOTPResponse
import com.pos10.model.remote.GetFeedbackResponse
import com.pos10.model.remote.GetTerminalStatusResponse
import com.pos10.model.remote.GetTrackHistoryResponse
import com.pos10.model.remote.GetWorkListResponse
import com.pos10.model.remote.LocationTrackRequest
import com.pos10.model.remote.MerchantDetailResponse
import com.pos10.model.remote.NotificationResponse
import com.pos10.model.remote.QrResponse
import com.pos10.model.remote.RequestLisResponse
import com.pos10.model.remote.UnbindDeviceResponse
import com.pos10.model.remote.UpdateApoointmentDetailsResponse
import com.pos10.model.remote.UpdateRequestResponse
import com.pos10.model.remote.ValidateOTPRequest
import com.pos10.view.screens.MerchantDetailsRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiServices {
    //login
    @FormUrlEncoded
    @POST("AUTH/oauth2/token")
    suspend fun login(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("password") password: String,
        @Field("username") username: String
    ): AuthResponse

    //fms details
    @POST("api/User/GetUserInfoByUsername")
    suspend fun merchantDetails(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(MyApplication.appContext).userId +"|0",
        @Body model: MerchantDetailsRequest
    ): MerchantDetailResponse

    //assign task list
    @POST("api/Request/GetRequestList")
    suspend fun getRequestList(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: GetRequestList
    ): RequestLisResponse

    @POST("api/Request/GetWorkOrderRequestList")
    suspend fun getWorkRequestList(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: GetWorkRequestLocal
    ): GetWorkListResponse

    //update api
    @POST("api/Request/UpdateRequest")
    suspend fun updateRequest(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: UpdateRequest
    ): UpdateRequestResponse

    //save appointment
    @POST("api/WorkAssignment/UpdateAppointmentDetails")
    suspend fun saveAppointment(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: SaveApppointmentRequest
    ): UpdateApoointmentDetailsResponse

    //upload file
    @Multipart
    @POST("api/Request/UploadFileRequest")
    suspend fun uploadFileRequest(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Part("RequestId") requestId: RequestBody,
        @Part("CreatedBy") createdBy: RequestBody,
        @Part("UploadType") UploadType: RequestBody,
        @Part("RequestType") RequestType: RequestBody,
        @Part file: MultipartBody.Part? = null
    ): UpdateRequestResponse

    //download file
    @GET("api/Request/DownLoadRequestFile/{requestId}/{requestType}")
    suspend fun downloadFile(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(MyApplication.appContext).userId +"|0",
        @Path("requestId") requestId: String,
        @Path("requestType") requestType: String,
    ): DownloadFileResponse

    @POST("api/Common/GenerateOtp")
    suspend fun getGenerateOTP(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: GenerateOTPRequest
    ): GenerateOTPResponse

    @POST("api/Common/ValidateOtp")
    suspend fun validateOTP(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: ValidateOTPRequest
    ): GenerateOTPResponse

    @POST("api/Request/UpdateDeviceRequest")
    suspend fun qrcodeScanner(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: QRCodeScanerRequest
    ): QrResponse

    @POST("api/FieldAgent/AddAgentWorkLocation")
    suspend fun locationTrackRequest(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: LocationTrackRequest
    ): QrResponse

    @POST("api/FieldAgent/GetAgentLocationDetails")
    suspend fun getAgentHistory(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: GetAgenTrackHistoryRequest
    ): GetTrackHistoryResponse

    //FCM Notifications
    @POST("api/FieldAgent/UpdateFieldAgentFcmToken")
    suspend fun getFieldAgentFcmToken(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: FcmRequest
    ): QrResponse

    @POST("api/FieldAgent/GetAgentFeedback")
    suspend fun getAgentFeedback(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: GetAgentFeedbackRequest
    ): GetFeedbackResponse

    @POST("api/FieldAgent/updateFieldAgentNotificationStatus")
    suspend fun notificationStatus(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: NotificationToggleRequest
    ): NotificationResponse

    @GET("api/Device/GetTerminalStatus/{terminalId}")
    suspend fun getTerminalStatus(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Path("terminalId") terminalId: String
    ): GetTerminalStatusResponse

    @POST("api/WorkAssignment/AddOrUpdateWorkOrder")
    suspend fun updateWorkOrderCompleted(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: UpdateWorkOrderCompletedRequest
    ): UpdateRequestResponse

    @POST("api/Request/DeleteUploadFile")
    suspend fun deleteUploadFile(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body model: DeleteUploadFileRequest
    ): UpdateRequestResponse

    @GET("api/Common/GetValueType/{typeId}")
    suspend fun getCancelList(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Path("typeId") typeId: String
    ): CancelReasonListResponse

    @POST("api/Device/MerchantUnBindDevice")
    suspend fun unbindDevice(
        @Header("Accept") accept: String = "application/json",
        @Header("UserName") UserName: String = SharedPreference.get(
            MyApplication.appContext
        ).userId +"|0",
        @Body request: UnbindDeviceRequest
    ): UnbindDeviceResponse
}