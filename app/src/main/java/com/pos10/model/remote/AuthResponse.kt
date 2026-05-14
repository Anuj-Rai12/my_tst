package com.pos10.model.remote

data class AuthResponse(
    val IsRedirect: Boolean,
    val RedirectUrl: String,
    val access_token: String,
    val expires_in: Int,
    val token_type: String
)