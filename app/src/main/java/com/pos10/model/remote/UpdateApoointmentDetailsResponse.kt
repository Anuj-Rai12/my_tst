package com.pos10.model.remote

data class UpdateApoointmentDetailsResponse(
    val `data`: Data?=null,
    val info: Info
) {
    data class Data(
        val agentId: Int,
        val appointmentDate: String,
        val appointmentTime: String,
        val workId: Int
    )

    data class Info(
        val code: Int,
        val isSuccess: Boolean,
        val message: String
    )
}