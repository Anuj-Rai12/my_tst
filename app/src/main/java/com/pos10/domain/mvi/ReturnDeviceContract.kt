package com.pos10.domain.mvi

import com.pos10.model.remote.GetWorkListResponse

/**
 * State represents the UI state at any given point in time.
 */
data class ReturnDeviceState(
    val isLoading: Boolean = false,
    val workOrders: List<GetWorkListResponse.Data.Wo> = emptyList(),
    val counts: Map<String, Int> = emptyMap(),
    val selectedOption: String = "Installation",
    val selectedTabIndex: Int = 0,
    val searchQuery: String = "",
    val error: String? = null
)

/**
 * Intent represents the user's intention or action.
 */
sealed class ReturnDeviceIntent {
    object NavigateToUpload : ReturnDeviceIntent()
    object NavigateToDashboard : ReturnDeviceIntent()
    object ShowLoader : ReturnDeviceIntent()
    object HideLoader : ReturnDeviceIntent()
    data class ShowToast(val message: String) : ReturnDeviceIntent()
    data class ErrorToast(val message: String) : ReturnDeviceIntent()

}

/**
 * Effect represents side effects that should happen only once (e.g., navigation, toasts).
 */
sealed class ReturnDeviceEffect {
    data class ShowError(val message: String) : ReturnDeviceEffect()
    data class NavigateToDetail(val route: String) : ReturnDeviceEffect()
}
