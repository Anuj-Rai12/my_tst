package com.pos10.model.remote

import java.util.ArrayList

data class GetFeedbackResponse(
    val `data`: ArrayList<Data>,
    val info: Info
) {
    data class Data(
        val merchantId: Int,
        val merchantName: String,
        val createdDate:String,
        val rating: String,
        val remarks: String
    )

    data class Info(
        val code: Int,
        val isSuccess: Boolean,
        val message: String
    )
}