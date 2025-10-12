package com.solvay.UI.Notification.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.solvay.Base.BaseViewModel
import com.solvay.BuildConfig
import com.solvay.UI.chat.model.ChatHistoryRequest
import com.solvay.UI.chat.model.ChatHistoryResponse
import com.solvay.UI.chat.model.ChatListRequest
import com.solvay.UI.chat.model.ChatListResponse
import com.solvay.UI.chat.model.UserData
import com.solvay.Utils.Event
import com.solvay.Utils.Prefs
import com.solvay.data.repo.RemoteRepositoryImp
import com.solvay.data.response.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class NotificationViewModel : BaseViewModel() {

    private val authenticationRepository by lazy {
        RemoteRepositoryImp()
    }
    val notificationListResponse: MutableLiveData<Event<ChatListResponse>> by lazy { MutableLiveData<Event<ChatListResponse>>() }
    val seenNotificationList: MutableLiveData<Event<ArrayList<UserData>>> by lazy { MutableLiveData<Event<ArrayList<UserData>>>() }
    val unseenNotificationList: MutableLiveData<Event<ArrayList<UserData>>> by lazy { MutableLiveData<Event<ArrayList<UserData>>>() }

    fun notificationList(chatListRequest: ChatListRequest) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.chatList(chatListRequest)
            { isSuccess: Boolean, response: ChatListResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("loginResponse", " ${response.success} ${response.data}")
                    val seenNotifications = ArrayList<UserData>()
                    val unseenNotifications = ArrayList<UserData>()
                    for (i in response.data?.list?: arrayListOf()){
                        if (i.unseenMessages == 1){
                            unseenNotifications.add(i)
                        }else{
                            seenNotifications.add(i)
                        }
                    }
                    if (response.data?.list?.isNotEmpty() == true){
                        response.data?.list?.filter { it.message.text.isNotEmpty() && it.message.type == "text" }?.forEach {data->
                            translateNotification(data.message.text){
                                data.message.text = it
                                notificationListResponse.postValue(Event(response))
                            }
                        }
                    }else{
                        notificationListResponse.postValue(Event(response))
                    }

                    seenNotificationList.postValue(Event(seenNotifications))
                    unseenNotificationList.postValue(Event(unseenNotifications))
                } else {
                    notificationListResponse.postValue(Event(response))
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun translateNotification(
        message: String,
        onTranslated: (String) -> Unit
    ){
        if (message.isEmpty()) {
            onTranslated(message)
            return
        }

        translateText(message){
            onTranslated(it)
        }
    }
    fun translateText(message: String, callback: (String) -> Unit) {
        if (message.isEmpty()) {
            callback("")
            return
        }

        val langCode = Prefs.init().appLanguageCode
        val apiKey = BuildConfig.TRANSLATION_KEY
        val url = "https://translation.googleapis.com/language/translate/v2?key=$apiKey"

        val json = JSONObject()
        json.put("q", message)
        json.put("target", langCode)

        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())
        val request = Request.Builder().url(url).post(body).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(message)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val translatedText = JSONObject(it)
                        .getJSONObject("data")
                        .getJSONArray("translations")
                        .getJSONObject(0)
                        .getString("translatedText")
                    callback(translatedText)
                } ?: callback(message)
            }
        })
    }
}