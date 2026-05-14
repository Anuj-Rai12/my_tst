package com.pos10.model.local

data class UpdateRequest(
    val Description: String,
    val DueDate: String,
    val IschecklistDone: Boolean,
    val Location: String,
    val ModifiedBy: Int,
    val Priority: Int,
    val Quantity: Int,
    val RequestId: Int,
    val Status: Int,
    val WarrantyType: Int,
    val AppointmentDate: String = "",
    val AppointmentTime: String = "",
    val ReasonId: Int = 0,
    val FinalRemarks: String = "",
    val RequestConditions: String? = null
)