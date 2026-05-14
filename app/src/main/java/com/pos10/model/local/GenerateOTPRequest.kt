package com.pos10.model.local

data class GenerateOTPRequest(
    val Username: String,
    val OtpTypeId: Int,
    val EmailAddress: String,
    val WorkId:String)

