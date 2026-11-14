package com.solvay.UI.chat.ChatViewModel


import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.solvay.Base.BaseViewModel
import com.solvay.BuildConfig
import com.solvay.UI.chat.model.ChatHistory
import com.solvay.Utils.Event
import com.solvay.UI.chat.model.ChatHistoryRequest
import com.solvay.UI.chat.model.ChatHistoryResponse
import com.solvay.UI.chat.model.ChatListRequest
import com.solvay.UI.chat.model.ChatListResponse
import com.solvay.Utils.Prefs
import com.solvay.data.repo.RemoteRepositoryImp
import com.solvay.data.response.BaseResponse
import com.solvay.data.response.LoginRequest
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
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class ChatViewModels : BaseViewModel() {

    var isCheckBoxChecked: Boolean = false
    var username: String? = null
    private val authenticationRepository by lazy {
        RemoteRepositoryImp()
    }
    val languageMap = mapOf(
        "german" to "de",
        "english" to "en",
        "polish" to "pl",
        "russian" to "ru",
        "ukrainian" to "uk",
        "czech" to "cs",
        "hungarian" to "hu",
        "slovak" to "sk",
        "slovenian" to "sl",
        "croatian" to "hr",
        "bulgarian" to "bg",
        "romanian" to "ro",
        "lithuanian" to "lt",
        "french" to "fr",
        "spanish" to "es",
        "italian" to "it"
    )
    val uploadMediaResponseData : MutableLiveData<Event<BaseResponse>> by lazy { MutableLiveData<Event<BaseResponse>>() }
    val chatListResponse: MutableLiveData<Event<ChatListResponse>> by lazy { MutableLiveData<Event<ChatListResponse>>() }
    val chatHistoryResponseData: MutableLiveData<Event<ChatHistoryResponse>> by lazy { MutableLiveData<Event<ChatHistoryResponse>>() }
    val baseResponse: MutableLiveData<Event<BaseResponse>> by lazy { MutableLiveData<Event<BaseResponse>>() }
    fun chatList(chatListRequest: ChatListRequest) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.chatList(chatListRequest)
            { isSuccess: Boolean, response: ChatListResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("loginResponse", " ${response.success} ${response.data}")
                    if (response.data?.list?.isNotEmpty() == true) {
                        val textMessages = response.data?.list
                            ?.filter { it.message.type == "text" && !it.message.text.isNullOrEmpty() }

                        if (!textMessages.isNullOrEmpty()) {
                            var remainingTranslations = textMessages.size

                            textMessages.forEach { data ->
                                translateMessage(data.message.text.orEmpty()) { translatedText ->
                                    data.message.text = translatedText

                                    remainingTranslations--

                                    if (remainingTranslations == 0) {
                                        // Post updated response once all translations are done
                                        chatListResponse.postValue(Event(response))
                                        Log.d("loginResponse", "All translations done: ${response.success} ${response.data}")
                                    }
                                }
                            }
                        } else {
                            // No valid text messages to translate
                            chatListResponse.postValue(Event(response))
                        }
                    } else {
                        chatListResponse.postValue(Event(response))
                    }



                } else {
                    chatListResponse.postValue(Event(response))
                    showMessage.postValue(response.message)
                }
            }
        }
    }


    fun chatHistory(chatHistoryRequest: ChatHistoryRequest) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.chatHistory(chatHistoryRequest)
            { isSuccess: Boolean, response: ChatHistoryResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("loginResponse", " ${response.success} ${response.data}")
                    translateChat(response.data?.data?.list?: arrayListOf()) { translatedChat ->
                        response.data?.data?.list = translatedChat
                        Log.d("MessageTranslation", "FormattedResponse  ${translatedChat}\n${response.data?.data}")

                        // response.data?.data?.list?.addAll(translatedChat)
                        chatHistoryResponseData.postValue(Event(response))
                    }
                } else {
                    chatHistoryResponseData.postValue(Event(response))
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun uploadChatMedia(
  //      request: HashMap<String, RequestBody>?=null,
        media: MultipartBody.Part
    ) {
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.uploadChatMedia( media)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("uploadChatMedia", "Upload success: ${response.message}")
                    uploadMediaResponseData.postValue(Event(response))
                } else {
                    Log.d("uploadChatMedia", "Upload failed: ${response.message}")
                    uploadMediaResponseData.postValue(Event(response))
                    showMessage.postValue(response.message)
                }
            }
        }
    }


    fun deleteMessage(messageId:String){
        showLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            authenticationRepository.deleteMessage(messageId)
            { isSuccess: Boolean, response: BaseResponse ->
                showLoading.postValue(false)
                if (isSuccess) {
                    Log.d("logout", " ${response}")
                    baseResponse.postValue(Event(response))
                } else {
                    baseResponse.postValue(Event(response))
                    showMessage.postValue(response.message)
                }
            }
        }
    }

    fun translateChat(
        chatList: ArrayList<ChatHistory>,
        onTranslated: (ArrayList<ChatHistory>) -> Unit
    ) {
        // Filter the chat messages that need translation and wrap each message in quotes
        val messagesToTranslate = chatList.filter { it.self != true && (it.type == "text" || it.type.isNullOrEmpty()) }
            .map { "\"${it.text}\"" }  // Wrap messages in quotes

        if (messagesToTranslate.isEmpty()) {
            onTranslated(chatList)
            return
        }

        translateTextBatch(messagesToTranslate, Prefs.init().appLanguageCode) { translatedList ->
            Log.d("sdsdfsddsf", "TranslatedList: ${translatedList[0]}\nTranslatedList Size: ${translatedList.size}")

            // Decode the HTML and replace &quot; with regular quotes
            val decodedString = Html.fromHtml(translatedList[0]).toString().replace("&quot;", "\"")
            Log.d("sdsdfsddsf", "Decoded String: $decodedString")

            // Check if the decoded string is a list or a single string
            val splitMessages = decodedString.trim('[').trim(']').split(",").map { it.trim(' ', '"') }

            Log.d("sdsdfsddsf", "Decoded and Split Messages: $splitMessages")

            // Assign the translated messages to the respective chat items
            var i = 0
            chatList.forEachIndexed { index, chat ->
                if (chat.self != true && (chat.type == "text" || chat.type.isNullOrEmpty())) {
                    // If the message is a list, update each item individually
                    if (splitMessages.isNotEmpty()) {
                        chat.text = splitMessages.getOrNull(i) ?: chat.text
                        i++
                    } else {
                        chat.text = decodedString.trim()  // If only one message, directly assign the decoded string
                    }
                }
            }

            onTranslated(chatList)
        }
    }

    fun translateMessage(
        message: String,
        onTranslated: (String) -> Unit
    ){
        if (message.isEmpty()) {
            onTranslated(message)
            return
        }

        translateText(message, Prefs.init().appLanguageCode){
            onTranslated(it)
        }
    }



    fun translateTextBatch(messages: List<String>, targetLanguage: String, callback: (List<String>) -> Unit) {
        if (messages.isEmpty()) {
            callback(emptyList())
            return
        }

        val apiKey = BuildConfig.TRANSLATION_KEY
        val url = "https://translation.googleapis.com/language/translate/v2?key=$apiKey"

        val json = JSONObject()
        json.put("q", messages)
        json.put("target", targetLanguage)
        Log.d("MessageTranslation","Requested messages: ${messages}")

        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())
        val request = Request.Builder().url(url).post(body).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(messages) // Return original messages on failure
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("MessageTranslationResponse", "Full Response: $responseBody")

                responseBody?.let {
                    try {
                        val translations = JSONObject(it)
                            .getJSONObject("data")
                            .getJSONArray("translations")
                        val translatedTexts = mutableListOf<String>()
                        for (i in 0 until translations.length()) {
                            translatedTexts.add(translations.getJSONObject(i).getString("translatedText"))
                        }
                        Log.d("MessageTranslation", "Output messages: ${translatedTexts}")
                        callback(translatedTexts)
                    } catch (e: Exception) {
                        Log.d("MessageTranslationError", "Error message: ${e.message}")
                        callback(messages) // Return original messages on failure
                    }
                } ?: callback(messages)
            }

        })
    }

    fun translateText(message: String, targetLanguage: String, callback: (String) -> Unit) {
        if (message.isEmpty()) {
            callback("")
            return
        }
        val apiKey = BuildConfig.TRANSLATION_KEY
        val url = "https://translation.googleapis.com/language/translate/v2?key=$apiKey"

        val json = JSONObject()
        json.put("q", message)
        json.put("target", targetLanguage)

        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json.toString())
        val request = Request.Builder().url(url).post(body).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Translation", "Translation request failed: ${e.message}")
                callback(message) // fallback to original message
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("Translation", "Translation request failed with code: ${response.code}")
                        callback(message)
                        return
                    }

                    val responseBody = response.body?.string()
                    responseBody?.let {
                        try {
                            val translatedText = JSONObject(it)
                                .getJSONObject("data")
                                .getJSONArray("translations")
                                .getJSONObject(0)
                                .getString("translatedText")
                            callback(translatedText)
                        } catch (e: JSONException) {
                            Log.e("Translation", "JSON parsing error: ${e.message}")
                            callback(message)
                        }
                    } ?: run {
                        Log.e("Translation", "Response body is null")
                        callback(message)
                    }
                }
            }
        })
    }


}