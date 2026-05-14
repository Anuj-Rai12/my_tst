package com.pos10.model.remote

data class UpdateRequestResponse(
    val `data`: Data,
    val info: Info
) {
    data class Data(
        val isChecklistDone: Boolean,
        val status: Int,
        val id: String
    )

    data class Info(
        val code: String,
        val isSuccess: Boolean,
        val message: String
    )
}