package com.pos10.model.remote

data class QrResponse(
    val `data`: Data,
    val info: Info
) {
    data class Data(
        val installationStatus:String
    )
    data class Info(
        val code: Int,
        val isSuccess: Boolean,
        val message: String
    )
}