package com.solvay.authentication.authViewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.solvay.Base.BaseViewModel
import com.solvay.UI.chat.model.UpdateLastLocation
import com.solvay.UI.settings.dataModels.ContactusResponse
import com.solvay.UI.settings.dataModels.UpdateLocationRequest
import com.solvay.UI.settings.dataModels.UpdateLocationResponse
import com.solvay.Utils.Event
import com.solvay.authentication.models.RouteKeyRequest
import com.solvay.authentication.models.RouteKeyResponse
import com.solvay.data.repo.RemoteRepositoryImp
import com.solvay.data.response.BaseResponse
import com.solvay.data.response.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AuthViewModel : BaseViewModel() {

    var isCheckBoxChecked: Boolean = false
    var username: String? = null
    private val authenticationRepository by lazy {
        RemoteRepositoryImp()
    }
    var loginData = MutableLiveData<BaseResponse>()
    var routeData = MutableLiveData<RouteKeyResponse>()
    var completeRouteResponse = MutableLiveData<ContactusResponse>()
    val updateLocationResponseData: MutableLiveData<Event<UpdateLocationResponse>> by lazy { MutableLiveData<Event<UpdateLocationResponse>>() }

    fun login(loginRequest: LoginRequest) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.login(loginRequest)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("loginResponse", " ${response.success} ${response.data}")
                    loginData.postValue(response)
                } else {
                    loginData.postValue(response)
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun getRouteKey(routeKeyRequest: RouteKeyRequest) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getRoute(routeKeyRequest)
            { isSuccess: Boolean, response: RouteKeyResponse ->
                showLoading.postValue(false)
                Log.d("RouteResponse", " ${response}")
                if (isSuccess) {
                    Log.d("RouteResponse", " ${response.success}")
                    Log.d("RouteResponse", " ${response}")
                    routeData.postValue(response)
                } else {
                    Log.d("RouteResponse", " ${response.error}")
                    routeData.postValue(response)
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun updateLastLocation(payload:UpdateLocationRequest) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.updateLastLocation(payload)
            { isSuccess: Boolean, response: UpdateLocationResponse ->
                showLoading.postValue(false)
                Log.d("RouteResponse", " ${response}")
                if (isSuccess) {
                    Log.d("RouteResponse", " ${response.success}")
                    Log.d("RouteResponse", " ${response}")
                    updateLocationResponseData.postValue(Event(response))
                } else {
                    Log.d("RouteResponse", " ${response.error}")
                    updateLocationResponseData.postValue(Event(response))
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun completeRoute() {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.completeRoute()
            { isSuccess: Boolean, response: ContactusResponse ->
                showLoading.postValue(false)
                Log.d("RouteResponse", " ${response}")
                if (isSuccess) {
                    Log.d("RouteResponse", " ${response.success}")
                    Log.d("RouteResponse", " ${response}")
                    completeRouteResponse.postValue(response)
                } else {
                    completeRouteResponse.postValue(response)
                    showMessage.postValue(response.message)
                }
            }
        }
    }


}