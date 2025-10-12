package com.solvay.dataSources

import android.content.Context
import com.solvay.authentication.models.RouteKeyRequest
import com.solvay.authentication.models.RouteKeyResponse
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
import com.solvay.data.response.BaseResponse
import com.solvay.data.response.LoginRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody


interface RemoteDataSource {
    suspend fun loginResponse(payload: LoginRequest): BaseResponse
    suspend fun getRouteResponse(payload: RouteKeyRequest): RouteKeyResponse
    suspend fun chatList(payload: ChatListRequest): ChatListResponse
    suspend fun chatHistory(payload: ChatHistoryRequest): ChatHistoryResponse
    suspend fun getProfile(): ProfileResponse
    suspend fun completeRoute(): ContactusResponse
    suspend fun updateProfile(payload: ProfileData, context: Context): ProfileResponse
    suspend fun contactUs(payload: ContactUsRequest): ContactusResponse
    suspend fun adminList(payload: AdminListRequest): AdminListResponse
    suspend fun updateLastLocation(payload: UpdateLocationRequest): UpdateLocationResponse

    suspend fun logout(): ContactusResponse
    suspend fun deleteMessage(messageId:String): BaseResponse

    suspend fun uploadChatMedia(
       // request: HashMap<String, RequestBody>?=null,
        media: MultipartBody.Part
    ): BaseResponse
}