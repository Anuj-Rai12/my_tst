package com.pos10.model.remote

data class GetTerminalStatusResponse(
    val `data`: Data,
    val info: Info
) {
    data class Data(
        val terminalStatus: String
    )

    data class Info(
        val code: Int,
        val isSuccess: Boolean,
        val message: String
    )
}