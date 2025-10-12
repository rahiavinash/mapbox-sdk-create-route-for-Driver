package com.solvay.UI.chat.model

data class ChatListRequest (
    val offset: Int,
    val limit: Int,
    val search: String? = null,
    val sort: String? = null,
    val order: Int? = null
)
data class ChatHistoryRequest (
    val partner: String,
    val offset: Int,
    val limit: Int? = null,

)

data class UpdateLastLocation (
    val index: Int,
    )
