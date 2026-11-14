package com.solvay.UI.newChat.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.solvay.Base.BaseViewModel
import com.solvay.UI.chat.model.ChatHistoryRequest
import com.solvay.UI.chat.model.ChatHistoryResponse
import com.solvay.UI.chat.model.ChatListRequest
import com.solvay.UI.chat.model.ChatListResponse
import com.solvay.UI.newChat.dataModel.AdminListRequest
import com.solvay.UI.newChat.dataModel.AdminListResponse
import com.solvay.Utils.Event
import com.solvay.data.repo.RemoteRepositoryImp
import com.solvay.data.response.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class AdminListViewModel : BaseViewModel() {
    private val authenticationRepository by lazy {
        RemoteRepositoryImp()
    }
    val adminListResponse: MutableLiveData<Event<AdminListResponse>> by lazy { MutableLiveData<Event<AdminListResponse>>() }

    fun adminList(request: AdminListRequest) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.adminList(request)
            { isSuccess: Boolean, response: AdminListResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("loginResponse", " ${response.success} ${response.data}")
                    adminListResponse.postValue(Event(response))
                } else {
                    adminListResponse.postValue(Event(response))
                    showMessage.postValue(response.message)
                }
            }
        }
    }


}