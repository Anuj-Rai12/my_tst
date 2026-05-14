package com.pos10.model.local

data class NotificationToggleRequest(
    val isNotificationOn: Boolean,
    val userid: Int
)