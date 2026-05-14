package com.pos10.model.remote


data class UnbindDeviceResponse(
    val data: Any? =null,
    val info: UnbindInfo
)

data class UnbindInfo(
    val code: Int,
    val isSuccess: Boolean,
    val message: String
)



