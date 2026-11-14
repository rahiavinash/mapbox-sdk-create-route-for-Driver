package com.solvay.data.response


data class LoginRequest(
    var deviceToken: String?=null,
    var fcmToken: String?=null,
    var deviceType: String="android",
    var contractNumber: String="",
    var plateNumber: String="",
    var name:String?=null,
    var language:String?= null,
    var location: List<Double?>? = null,
)