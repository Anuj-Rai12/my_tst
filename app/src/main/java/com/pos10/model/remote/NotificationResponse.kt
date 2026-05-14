package com.pos10.model.remote

data class NotificationResponse(
    val `data`: Data,
    val info: Info
) {
    data class Data(
        val isNotificationOn: Boolean
    )

    data class Info(
        val code: Int,
        val isSuccess: Boolean,
        val message: String
    )
}