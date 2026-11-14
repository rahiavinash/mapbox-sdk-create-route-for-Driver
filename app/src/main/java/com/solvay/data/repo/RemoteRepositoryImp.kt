package com.solvay.data.repo

import android.content.Context
import android.util.Log
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
import com.solvay.dataSources.RemoteDataSourceImp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class RemoteRepositoryImp(

) : RemoteRepositary {

    private val authenticationDataSource by lazy {
        RemoteDataSourceImp()
    }
    private val myCoroutineScope = CoroutineScope(Dispatchers.IO)

    @OptIn(DelicateCoroutinesApi::class)
    override fun login(
        loginRequest: LoginRequest,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.loginResponse(loginRequest)

            Log.e("API Response", response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun chatList(
        chatListRequest: ChatListRequest,
        onResult: (isSuccess: Boolean, baseResponse: ChatListResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.chatList(chatListRequest)

            Log.e("API Response", response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun chatHistory(
        chatHistoryRequest: ChatHistoryRequest,
        onResult: (isSuccess: Boolean, baseResponse: ChatHistoryResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.chatHistory(chatHistoryRequest)

            Log.e("API Response", response.toString())
            if (!response.success) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun getRoute(
        routeKeyRequest: RouteKeyRequest,
        onResult: (isSuccess: Boolean, routeKeyResponse: RouteKeyResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.getRouteResponse(routeKeyRequest)
            Log.d("API Response", response.toString())
            Log.d("API Request", routeKeyRequest.toString())
            if (response.success == false) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun uploadChatMedia(
     //   request: HashMap<String, RequestBody>?,
        media: MultipartBody.Part,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.uploadChatMedia( media)
            Log.d("API Response", response.toString())
            if (response.success == false) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun getProfile(onResult: (isSuccess: Boolean, routeKeyResponse: ProfileResponse) -> Unit) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.getProfile()
            Log.d("API Response", response.toString())
            if (response.success == false) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun completeRoute(onResult: (isSuccess: Boolean, routeKeyResponse: ContactusResponse) -> Unit) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.completeRoute()
            Log.d("API Response", response.toString())
            if (response.success == false) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun updateProfile(
        payload: ProfileData,
        context: Context,
        onResult: (isSuccess: Boolean, routeKeyResponse: ProfileResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.updateProfile(payload, context)
            Log.d("API Response", response.toString())
            if (response.success == false) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun adminList(
        payload: AdminListRequest,
        onResult: (isSuccess: Boolean, payload: AdminListResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.adminList(payload)
            Log.d("API Response", response.toString())
            if (response.success == false) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun updateLastLocation(
        payload: UpdateLocationRequest,
        onResult: (isSuccess: Boolean, routeKeyResponse: UpdateLocationResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.updateLastLocation(payload)
            Log.d("API Response", response.toString())
            if (response.success == false) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun contactUs(
        payload: ContactUsRequest,
        onResult: (isSuccess: Boolean, routeKeyResponse: ContactusResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.contactUs(payload)
            Log.d("API Response", response.toString())
            if (response.success == false) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun logout(onResult: (isSuccess: Boolean, routeKeyResponse: ContactusResponse) -> Unit) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.logout()
            Log.d("API Response", response.toString())
            if (response.success == false) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }

    override fun deleteMessage(
        messageId: String,
        onResult: (isSuccess: Boolean, baseResponse: BaseResponse) -> Unit
    ) {
        myCoroutineScope.launch {
            val response = authenticationDataSource.deleteMessage(messageId)
            Log.d("API Response", response.toString())
            if (response.success == false) {
                onResult(false, response)
            } else {
                onResult(true, response)
            }
        }
    }
}