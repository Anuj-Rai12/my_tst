package com.pos10.model.local

data class CancelReasonListResponse(
    val `data`: ArrayList<Data>?=null,
    val info: Info
) {
    data class Data(
        val active: Boolean,
        val bitMapValue: Int,
        val createdDate: String,
        val description: String,
        val displayOrder: Int,
        val name: String,
        val parentValueTypeId: Int,
        val valueTypeGroupId: Int,
        val valueTypeId: Int
    )

    data class Info(
        val code: Int,
        val isSuccess: Boolean,
        val message: Any
    )
}