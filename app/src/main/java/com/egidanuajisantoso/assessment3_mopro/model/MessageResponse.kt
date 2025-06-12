package com.egidanuajisantoso.assessment3_mopro.model


data class MessageResponse(
    val status: String,
    val data: String,
    val message: String? = null
)

