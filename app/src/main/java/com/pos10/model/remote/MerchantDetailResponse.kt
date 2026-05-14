package com.pos10.model.remote

data class MerchantDetailResponse(
    val `data`: Data?=null,
    val info: Info)

data class Data(
    val email: String,
    val entityTypeId: Int,
    val password: Any,
    val roleId: String,
    val uid: String,
    val userId: String,
    val userName: String,
    val averageRating:String)

data class Info(
    val code: String,
    var isSuccess: Boolean,
    val message: String)