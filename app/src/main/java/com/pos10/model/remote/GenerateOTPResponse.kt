package com.pos10.model.remote

data class GenerateOTPResponse(
    val `data`: Data,
    val info: Info
) {
    data class Data(
        val generatedOTP: String,
        val responseMessage: String,
        val responsecode: Int,
    )

    data class Info(
        val code: Int,
        val isSuccess: Boolean,
        val message: String
    )
}