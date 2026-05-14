package com.pos10.domain.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pos10.helper.EmpResource
import com.pos10.helper.dbupdate.DbUpdate
import com.pos10.model.local.UnbindDeviceRequest
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReturnDeviceViewModel @Inject constructor(
    private val returnDeviceUserCase: Lazy<ReturnDeviceUserCase>,
    private val dbUpdate: Lazy<DbUpdate>
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<ReturnDeviceIntent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun unbindDevice(
        unbindDeviceRequest: UnbindDeviceRequest,
        requestId: String?,
        serialNumber: String,
        simnumber: String?
    ) {
        viewModelScope.launch {

            when (val result = returnDeviceUserCase.get().invoke(unbindDeviceRequest)) {

                is EmpResource.Loading -> {
                    _eventFlow.emit(ReturnDeviceIntent.ShowLoader)
                }

                is EmpResource.Success -> {
                    _eventFlow.emit(ReturnDeviceIntent.HideLoader)
                    if (result.value.info.isSuccess) {
                        dbUpdate.get().updateRequestAfterQRCode(
                            requestId?.toInt() ?: 0, "0", serialNumber, simnumber ?: ""
                        )
                        _eventFlow.emit(
                            ReturnDeviceIntent.ShowToast(
                                result.value.info.message ?: "Successfully"
                            )
                        )
                    } else {
                        _eventFlow.emit(
                            ReturnDeviceIntent.ShowToast(
                                result.value.info.message ?: "Successfully"
                            )
                        )
                    }
                }

                is EmpResource.Failure -> {
                    _eventFlow.emit(ReturnDeviceIntent.HideLoader)
                    _eventFlow.emit(
                        ReturnDeviceIntent.ErrorToast(
                            result.throwable?.message ?: "Something went wrong"
                        )
                    )
                }
            }
        }
    }
}