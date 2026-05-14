package com.pos10.model.local

import com.google.gson.annotations.SerializedName

data class UnbindDeviceRequest(
    @SerializedName("DeviceNo")
    val deviceNo: Int,
    @SerializedName("ModifiedBy")
    val modifiedBy: Int
)