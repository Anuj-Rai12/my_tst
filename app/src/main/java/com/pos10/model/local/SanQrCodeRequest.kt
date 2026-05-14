package com.pos10.model.local

data class SanQrCodeRequest(
    val MID: String,
    val SIMNo: String,
    val SerialNo: String,
    val TID: String,
    val TimeStamp: String
)