package com.pos10.di

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthEventManager @Inject constructor() {

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    suspend fun send(event: AuthEvent) {
        _events.emit(event)
    }
}