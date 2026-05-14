package com.pos10.model.local

data class GetAgenTrackHistoryRequest(
    val agentUserid: String,
    val endDate: String?,
    val startDate: String?,
    val workId: String)