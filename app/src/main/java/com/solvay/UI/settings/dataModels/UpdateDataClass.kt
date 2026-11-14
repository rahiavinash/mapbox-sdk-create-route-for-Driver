package com.solvay.UI.settings.dataModels

data class UpdateLocationRequest(
    val index: Int?=null,
    val lastLocation: String? = null,
    val cords: ArrayList<Double>? = null
)
data class UpdateLocationResponse(
    val success: Boolean? = null,
    val error: String? = null,
    var message: String? = null
)


data class deleteRequest(
    val success: Boolean? = null,
    var message: String? = null
)