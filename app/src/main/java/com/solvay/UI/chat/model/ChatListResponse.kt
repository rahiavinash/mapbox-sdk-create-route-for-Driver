package com.solvay.UI.chat.model

import com.solvay.data.response.Data

data class ChatListResponse(
    var data: DataX?=null,
    var message: String?=null,
    var error: Int?=null,
    var success: Boolean=false
)

data class DataX(
    val list: ArrayList<UserData>,
    val pagination: Pagination
)

data class UserData(
    val _id: String,
    val message: Message,
    val self: Boolean,
    val unseenMessages: Int,
    val updatedAt: String,
    val user: User
)

data class Pagination(
    val currentPage: Int,
    val nextPage: Int,
    val prevPage: Int,
    val totalCount: Int
)

data class Message(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val deletedBy: ArrayList<Any>,
    val `file`: Any,
    val `receiver`: String,
    val replyId: Any,
    val sender: String,
    val status: String,
    var text: String,
    val thumbnail: Any,
    val type: String,
    val unsend: Boolean,
    val updatedAt: String
)

data class User(
    val _id: String,
    val image: String,
    val isOnline: Boolean,
    val name: String
)