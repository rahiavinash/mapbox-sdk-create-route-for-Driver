package com.solvay.UI.settings.dataModels

data class ContactUsRequest(
    val message: String? = null,
    val email: String? = null
)

data class ContactusResponse(
    val success: Boolean? = null,
    var message: String? = null
)
