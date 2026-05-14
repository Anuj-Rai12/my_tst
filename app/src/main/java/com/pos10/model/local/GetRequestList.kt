package com.pos10.model.local

data class GetRequestList(
    val DeviceNo: String,
    val DeviceType: Int,
    val MerchantCode: Int,
    val MerchantName: String,
    val RequestType: Int,
    val Status: Int,
    val Username: String
)