package com.pos10.model.remote

data class LocationTrackRequest(
    val agentUserid: String,
    val createdBy: Int,
    val latitude: String,
    val longitude: String,
    val workId: Int
)