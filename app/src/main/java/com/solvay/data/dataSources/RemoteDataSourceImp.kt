package com.solvay.dataSources

import android.content.Context
import android.net.Uri
import android.util.Log
import com.solvay.Network.ApiService
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class RemoteDataSourceImp() : RemoteDataSource {

    private val apiService by lazy {
        ApiService()
    }
    override suspend fun loginResponse(payload: LoginRequest): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.login(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun getRouteResponse(payload: RouteKeyRequest): RouteKeyResponse {
        var response = RouteKeyResponse()
        try {
            response = apiService.getRoute(payload)
        }catch (e: java.lang.Exception){
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }


    override suspend fun chatList(payload: ChatListRequest): ChatListResponse {
        var response = ChatListResponse()
        try {
            response = apiService.chatList(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun chatHistory(payload: ChatHistoryRequest): ChatHistoryResponse {
        var response = ChatHistoryResponse()
        try {
            response = apiService.chatHistory(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    override suspend fun getProfile(): ProfileResponse {
        var response = ProfileResponse()
        try {
            response = apiService.getProfile()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun completeRoute(): ContactusResponse {
        var response = ContactusResponse()
        try {
            response = apiService.completeRoute()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun updateProfile(payload: ProfileData, context: Context): ProfileResponse {
        var response = ProfileResponse()
        try {
            response = apiService.updateProfile(
                id = createTextPart(payload.id),
                language = createTextPart(payload.language),
                name = createTextPart(payload.name),
                plateNumber = createTextPart(payload.plateNumber),
                contractNumber = createTextPart(payload.contractNumber),
                image = createImagePart(context, payload.imageUri)
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun contactUs(payload: ContactUsRequest): ContactusResponse {
        var response = ContactusResponse()
        try {
            response = apiService.contactUs(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun adminList(payload: AdminListRequest): AdminListResponse {
        var response = AdminListResponse()
        try {
            response = apiService.adminList(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun updateLastLocation(payload: UpdateLocationRequest): UpdateLocationResponse {
        var response = UpdateLocationResponse()
        try {
            response = apiService.updateLastLocation(payload)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun logout(): ContactusResponse {
        var response = ContactusResponse()
        try {
            response = apiService.logout()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }

    override suspend fun deleteMessage(messageId: String): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.deleteMessage(messageId)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
        }
        return response
    }



    override suspend fun uploadChatMedia(
     //   request: HashMap<String, RequestBody>?,
        media: MultipartBody.Part
    ): BaseResponse {
        var response = BaseResponse()
        try {
            response = apiService.uploadChatMedia( media)
        } catch (e: Exception) {
            e.printStackTrace()
            response.message = ApiService.getErrorMessageFromGenericResponse(e).toString()
            response.error = 1
        }
        return response
    }

    private fun createTextPart(value: String?): RequestBody? {
        return value?.let {
            RequestBody.create("text/plain".toMediaTypeOrNull(), it)
        }
    }

    private fun createImagePart(context: Context, uri: Uri?): MultipartBody.Part? {
        return uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
            tempFile.outputStream().use { output ->
                inputStream?.copyTo(output)
            }
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), tempFile)
            MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
        }
    }

}



