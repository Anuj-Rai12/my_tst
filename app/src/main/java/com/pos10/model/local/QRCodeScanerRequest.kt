package com.pos10.model.local

data class QRCodeScanerRequest(
    val DeviceNo: Int,
    val RequestId: Int,
    val SerialNumber: String,
    val SimNumber: String,
    val UserId: Int,
    val Merchantid:String,
    val Timestamp:String
)
