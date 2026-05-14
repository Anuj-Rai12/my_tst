package com.pos10.domain.mvi

import com.pos10.data.repository.AuthRepository
import com.pos10.helper.EmpResource
import com.pos10.model.local.UnbindDeviceRequest
import com.pos10.model.remote.UnbindDeviceResponse
import dagger.Lazy
import javax.inject.Inject

class ReturnDeviceUserCase @Inject constructor(
    private val repository: Lazy<AuthRepository>
) {
    suspend operator fun invoke(unbindDeviceRequest: UnbindDeviceRequest): EmpResource<UnbindDeviceResponse> {
        return repository.get().deviceUnbind(unbindDeviceRequest)
    }
}