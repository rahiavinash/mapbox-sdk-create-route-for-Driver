package com.solvay.data.response
data class BaseResponse(
    var data: Data?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false
)

data class CommentBaseResponse(
    var data: DataX?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false
)
data class DataX(
    var isChecked: Boolean = false,
    val _id: String,
    val creator: String,
    val totalComments: Int,
    val noOfComments: Int
)
data class Data(
    val userId: String?=null,
    val accessToken: String="",
    val refreshToken: String="",
    val file: String=""
)
