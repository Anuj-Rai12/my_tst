package com.pos10.model.local

data class SaveApppointmentRequest(
    val appointmentDate: String,
    val appointmentTime: String,
    val userId: Int,
    val workId: Int
)