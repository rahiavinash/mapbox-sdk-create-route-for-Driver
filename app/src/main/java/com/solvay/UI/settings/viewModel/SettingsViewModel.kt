package com.solvay.UI.settings.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.solvay.Base.BaseViewModel
import com.solvay.UI.newChat.dataModel.AdminListRequest
import com.solvay.UI.newChat.dataModel.AdminListResponse
import com.solvay.UI.settings.dataModels.ContactUsRequest
import com.solvay.UI.settings.dataModels.ContactusResponse
import com.solvay.UI.settings.dataModels.ProfileData
import com.solvay.UI.settings.dataModels.ProfileResponse
import com.solvay.Utils.Event
import com.solvay.Utils.Prefs
import com.solvay.data.repo.RemoteRepositoryImp
import com.solvay.data.response.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel : BaseViewModel() {
    private val authenticationRepository by lazy {
        RemoteRepositoryImp()
    }
    val userProfile: MutableLiveData<Event<ProfileResponse>> by lazy { MutableLiveData<Event<ProfileResponse>>() }
    val updateProfileResponse: MutableLiveData<Event<ProfileResponse>> by lazy { MutableLiveData<Event<ProfileResponse>>() }
    val contactusResponse: MutableLiveData<Event<ContactusResponse>> by lazy { MutableLiveData<Event<ContactusResponse>>() }
    val logoutResponse: MutableLiveData<Event<ContactusResponse>> by lazy { MutableLiveData<Event<ContactusResponse>>() }


    fun getProfile() {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.getProfile()
            { isSuccess: Boolean, response: ProfileResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("userProfile", " ${response}")
                    userProfile.postValue(Event(response))
                } else {
                    userProfile.postValue(Event(response))
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun updateProfile(request: ProfileData, context: Context) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.updateProfile(request, context)
            { isSuccess: Boolean, response: ProfileResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("updateProfile", " ${response}")
                    updateProfileResponse.postValue(Event(response))
                } else {
                    updateProfileResponse.postValue(Event(response))
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun contactUs(request: ContactUsRequest) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.contactUs(request)
            { isSuccess: Boolean, response: ContactusResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("userProfile", " ${response}")
                    contactusResponse.postValue(Event(response))
                } else {
                    contactusResponse.postValue(Event(response))
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun logout(){
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.logout()
            { isSuccess: Boolean, response: ContactusResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("logout", " ${response}")
                    logoutResponse.postValue(Event(response))
                } else {
                    logoutResponse.postValue(Event(response))
                    showMessage.postValue(response.message)
                }
            }
        }
    }


    fun saveUserProfileLocally(userProfile: ProfileData){
        Prefs.init().userProfile
    }


}