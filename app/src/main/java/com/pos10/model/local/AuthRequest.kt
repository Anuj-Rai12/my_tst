package com.pos10.model.local

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("grant_type")
    val grant_type: String,
    @SerializedName("client_id")
    val client_id: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("username")
    val username: String
)
