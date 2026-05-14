package com.pos10.model.remote

data class GetTrackHistoryResponse(
    val `data`: ArrayList<Data>,
    val info: Info
) {
    data class Data(
        val agentName: String,
        val agentUserid: Int,
        val id: Int,
        val latitude: String,
        val longitude: String,
        val status: String,
        val workId: Int,
        val workOrderNo: String
    )

    data class Info(
        val code: Int,
        val isSuccess: Boolean,
        val message: String
    )
}

data class RouteCoordinate(
    val latitude: Double,
    val longitude: Double
)