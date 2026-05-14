package com.pos10.model.remote

data class ValidateOTPRequest(
    val Username:String,
    val OTP:Int,
    val OtpTypeId:Int)