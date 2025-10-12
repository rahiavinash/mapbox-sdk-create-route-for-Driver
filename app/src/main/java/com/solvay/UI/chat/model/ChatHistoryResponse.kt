package com.solvay.UI.chat.model

data class ChatHistoryResponse(
    var data: Data?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false
)

data class Data(
    val `data`: DataXUser,
    val self: Self,
    val user: Users
)

data class DataXUser(
    var list: ArrayList<ChatHistory>,
    val pagination: HistoryPagination
)

data class Self(
    val image: Any,
    val isOnline: Boolean,
    val name: String
)

data class Users(
    val _id: String,
    val image: String,
    val isOnline: Boolean,
    val name: String,
    val socketId: String
)

data class ChatHistory(
    val __v: Int,
    val _id: String,
    var createdAt: String,
    val deletedBy: ArrayList<Any>?=null,
    val `file`: String,
    val `receiver`: String,
    val replyId: String,
    val self: Boolean,
    val sender: String,
    val status: String,
    var text: String,
    val thumbnail: Any,
    var type: String,
    var unsend: Boolean,
    val updatedAt: String,
    val isReplyVisible: Boolean,
    var isMenuOpen: Boolean? = false,
    val replyMessage: ReplyMessage? = null,
    var translatedText: String?=null,
)

data class ReplyMessage(
    val _id: String,
    val sender: String,
    val receiver: String,
    val replyId: String?,
    val text: String,
    val file: String?,
    val thumbnail: String?,
    val deletedBy: List<String>,
    val unsend: Boolean,
    val type: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    val self: Boolean
)
data class HistoryPagination(
    val currentPage: Int,
    val nextPage: Int,
    val prevPage: Int,
    val totalCount: Int
)