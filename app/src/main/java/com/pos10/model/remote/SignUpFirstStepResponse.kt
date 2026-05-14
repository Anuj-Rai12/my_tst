package com.pos10.model.remote

data class SignUpFirstStepResponse(
    val `data`: Data,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val commercial_registration_number: String,
        val country_address: String,
        val mobile_number: String,
        val permanent_establishment_number: String,
        val street_line_1: String,
        val street_line_2: String,
        val taxpayer_id_number: String,
        val vat_id_number: String,
        val website: String,
        val zip_code: String
    )
}