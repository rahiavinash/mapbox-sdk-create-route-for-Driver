package com.solvay.data.repo

import android.content.Context
import com.google.android.datatransport.runtime.EncodedPayload
import com.solvay.UI.chat.model.ChatHistoryRequest
import com.solvay.UI.chat.model.ChatHistoryResponse
import com.solvay.UI.chat.model.ChatListRequest
import com.solvay.UI.chat.model.ChatListResponse
import com.solvay.UI.newChat.dataModel.AdminListRequest
import com.solvay.UI.newChat.dataModel.AdminListResponse
import com.solvay.UI.settings.dataModels.ContactUsRequest
import com.solvay.UI.settings.dataModels.ContactusResponse
import com.solvay.UI.settings.dataModels.ProfileData
import com.solvay.UI.settings.dataModels.ProfileResponse
import com.solvay.UI.settings.dataModels.UpdateLocationRequest
import com.solvay.UI.settings.dataModels.UpdateLocationResponse
import com.solvay.authentication.models.RouteKeyRequest
import com.solvay.authentication.models.RouteKeyResponse
import com.solvay.data.response.BaseResponse
import com.solvay.data.response.LoginRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody


interface RemoteRepositary {
    fun login(
        loginRequest: LoginRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )

    fun chatList(
        chatListRequest: ChatListRequest,
        onResult: (isSuccess: Boolean, baseResponse: ChatListResponse) -> Unit
    )
    fun chatHistory(
        chatHistoryRequest: ChatHistoryRequest,
        onResult: (isSuccess: Boolean, baseResponse: ChatHistoryResponse) -> Unit
    )
    fun getRoute(
        routeKeyRequest: RouteKeyRequest,
        onResult: (isSuccess: Boolean, routeKeyResponse: RouteKeyResponse) -> Unit
    )
    fun uploadChatMedia(
    //    request: HashMap<String, RequestBody>?=null,
        media: MultipartBody.Part,
        onResult: (isSuccess: Boolean,baseResponse: BaseResponse) -> Unit
    )

    fun getProfile(
        onResult: (isSuccess: Boolean, routeKeyResponse: ProfileResponse) -> Unit
    )

    fun completeRoute(
        onResult: (isSuccess: Boolean, routeKeyResponse: ContactusResponse) -> Unit
    )

    fun updateProfile(
        routeKeyRequest: ProfileData,
        context: Context,
        onResult: (isSuccess: Boolean, routeKeyResponse: ProfileResponse) -> Unit
    )

    fun adminList(
        routeKeyRequest: AdminListRequest,
        onResult: (isSuccess: Boolean, payload: AdminListResponse) -> Unit
    )
    fun updateLastLocation(
        routeKeyRequest: UpdateLocationRequest,
        onResult: (isSuccess: Boolean, routeKeyResponse: UpdateLocationResponse) -> Unit
    )

    fun contactUs(
        routeKeyRequest: ContactUsRequest,
        onResult: (isSuccess: Boolean, routeKeyResponse: ContactusResponse) -> Unit
    )

    fun logout(
        onResult: (isSuccess: Boolean, routeKeyResponse: ContactusResponse) -> Unit
    )

    fun deleteMessage(
        messageId: String,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    )
}