package com.pos10.di

sealed class AuthEvent {
    object Unauthorized : AuthEvent()
}