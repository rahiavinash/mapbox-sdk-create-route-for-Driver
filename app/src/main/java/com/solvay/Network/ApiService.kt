package com.solvay.Network

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.solvay.BuildConfig
import com.solvay.MainApplication
import com.solvay.R
import com.solvay.Utils.Prefs
import com.solvay.Utils.Utils
import com.solvay.Utils.Utils.toJsonString
import com.solvay.authentication.AuthenticationActivity
import com.solvay.authentication.models.RouteKeyRequest
import com.solvay.authentication.models.RouteKeyResponse
import com.solvay.UI.chat.model.ChatHistoryRequest
import com.solvay.UI.chat.model.ChatHistoryResponse
import com.solvay.UI.chat.model.ChatListRequest
import com.solvay.UI.chat.model.ChatListResponse
import com.solvay.UI.chat.model.UpdateLastLocation
import com.solvay.UI.newChat.dataModel.AdminListRequest
import com.solvay.UI.newChat.dataModel.AdminListResponse
import com.solvay.UI.newChat.dataModel.TranslationRequest
import com.solvay.UI.newChat.dataModel.TranslationResponse
import com.solvay.UI.settings.dataModels.ContactUsRequest
import com.solvay.UI.settings.dataModels.ContactusResponse
import com.solvay.UI.settings.dataModels.ProfileData
import com.solvay.UI.settings.dataModels.ProfileResponse
import com.solvay.UI.settings.dataModels.UpdateLocationRequest
import com.solvay.UI.settings.dataModels.UpdateLocationResponse
import com.solvay.data.response.BaseResponse
import com.solvay.data.response.LoginRequest
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.HashMap
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface ApiService {
    @POST("api/v1/user/sign-up")
    suspend fun login(
        @Body body: LoginRequest
    ): BaseResponse

    @POST("api/v1/user/get-route")
    suspend fun getRoute(
        @Body body: RouteKeyRequest
    ): RouteKeyResponse

    @POST("api/v1/chat/recent")
    suspend fun chatList(
        @Body body: ChatListRequest
    ): ChatListResponse

    @POST("api/v1/chat/history")
    suspend fun chatHistory(
        @Body body: ChatHistoryRequest
    ): ChatHistoryResponse




    @POST("api/v1/user/admin-list")
    suspend fun adminList(
        @Body body: AdminListRequest
    ): AdminListResponse

    @POST("api/v1/user/contact-us")
    suspend fun contactUs(
        @Body body: ContactUsRequest
    ): ContactusResponse


    @POST("api/v1/user/updateLastLocation")
    suspend fun updateLastLocation(
        @Body body: UpdateLocationRequest
    ): UpdateLocationResponse

    @Multipart
    @PUT("api/v1/user/profile")
    suspend fun updateProfile(
        @Part("_id") id: RequestBody?,
        @Part("language") language: RequestBody?,
        @Part("name") name: RequestBody?,
        @Part("plateNumber") plateNumber: RequestBody?,
        @Part("contractNumber") contractNumber: RequestBody?,
        @Part image: MultipartBody.Part?
    ): ProfileResponse

    @GET("api/v1/user/profile")
    suspend fun getProfile(): ProfileResponse

    @POST("api/v1/user/complete-route")
    suspend fun completeRoute(): ContactusResponse

    @GET("api/v1/user/logout")
    suspend fun logout(): ContactusResponse

    @DELETE("api/v1/chat/delete-message/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: String
    ): BaseResponse

    @Multipart
    @POST("api/v1/chat/media")
    suspend fun uploadChatMedia(
        //  @PartMap request: HashMap<String, RequestBody>?=null,
        @Part media: MultipartBody.Part,
    ): BaseResponse


    @POST("language/translate/v2")
    suspend fun translateText(
        @Query("key") apiKey: String,
        @Body request: TranslationRequest
    ): TranslationResponse
    companion object {
        val TAG = ApiService::class.java.name
        var BASE_URL_LOCAL = "http://192.168.3.223:9999/"

        @SuppressLint("SuspiciousIndentation")
        private fun getAPIService(): ApiService {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                        return arrayOf()
                    }
                }
            )

            val clientBuilder = OkHttpClient.Builder()
                .addInterceptor(provideHeaderInterceptor()!!)
                .addInterceptor(ForbiddenInterceptor())
                .connectTimeout(3000, TimeUnit.SECONDS)
                .readTimeout(3000, TimeUnit.SECONDS)
                .writeTimeout(3000, TimeUnit.SECONDS)
                .sslSocketFactory(getSslSocketFactory()!!, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .addInterceptor(provideHttpLoggingInterceptor()!!)
                .addInterceptor(ExceptionInterceptorWithoutEncryption())
//               .addInterceptor(EncryptionInterceptor())

            val client = clientBuilder.build()
            val gson = GsonBuilder()
                .setLenient()
                .create()
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build().create(ApiService::class.java)
        }

        private fun getSslSocketFactory(): SSLSocketFactory? {
            try {
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate?>?,
                            authType: String?
                        ) {
                        }

                        override fun checkServerTrusted(
                            chain: Array<X509Certificate?>?,
                            authType: String?
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate?>? {
                            return arrayOf()
                        }
                    }
                )
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                return sslContext.socketFactory


            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                return null
            }
        }

        fun getErrorMessageFromGenericResponse(
            exception: Exception,
        ): String? {
            var error = ""
            try {
                when (exception) {
                    is HttpException -> {
                        val body = exception.response()?.errorBody()
                        val adapter = Gson().getAdapter(BaseResponse::class.java)
                        val errorParser = adapter.fromJson(body?.string())
                        val msg = errorParser.message ?: exception.message()
                        error = if (msg == "Unauthenticated.") {
                            MainApplication.instance.applicationContext.getString(R.string.auth_error)
                        } else {
                            msg
                        }
                    }

                    is ConnectException -> {
                        error =
                            MainApplication.instance.applicationContext.getString(R.string.no_internet_message)
                    }

                    is SocketTimeoutException -> {
                        error =
                            MainApplication.instance.applicationContext.getString(R.string.no_internet_message)
                    }

                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                return error
            }
        }

        fun getErrorDataFromGenericResponse(
            exception: Exception,
        ): String? {
            var error = ""
            try {
                when (exception) {
                    is HttpException -> {
                        val body = exception.response()?.errorBody()
                        val adapter = Gson().getAdapter(BaseResponse::class.java)
                        val errorParser = adapter.fromJson(body?.string())
                        val msg = errorParser.toJsonString()
                        error = if (msg == "Unauthenticated.") {
                            MainApplication.instance.applicationContext.getString(R.string.language)
                        } else {
                            msg
                        }
                    }

                    is ConnectException -> {
                        error =
                            MainApplication.instance.applicationContext.getString(R.string.no_internet_message)
                    }

                    is SocketTimeoutException -> {
                        error =
                            MainApplication.instance.applicationContext.getString(R.string.no_internet_message)
                    }

                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                return error
            }
        }

        private fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor? {
            val httpLoggingInterceptor =
                HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) {
                        Log.d("API Logging", "response => $message")
                    }
                })
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            return httpLoggingInterceptor
        }

        private fun provideHeaderInterceptor(): Interceptor? {
            return Interceptor { chain -> //*Code for request with auth token
                val accessToken = if (Prefs.init().accessToken.isNullOrEmpty()) {
                    ""
                } else if (!Utils.isTokenExpired()) {
                    Prefs.init().accessToken
                } else {
                    ""
                }


                Log.e("accessToken: ", accessToken ?: "null")

                if (!TextUtils.isEmpty(accessToken)) {
                    val request: Request = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("x-access-token", "Bearer $accessToken")
                        .addHeader("language", Locale.getDefault().language).build()
                    chain.proceed(request)

                } else {
                    val request: Request = chain.request().newBuilder()
                        .addHeader("Accept", "application/json").build()
                    chain.proceed(request)
                }
            }
        }


        class ForbiddenInterceptor : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val request: Request = chain.request()
                return chain.proceed(request)
            }
        }

        class ExceptionInterceptorWithoutEncryption : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                val response = chain.proceed(request)
                when (response.code) {
                    500 -> {
                        return response
                    }

                    402 -> {
                        return response
                    }

                    401 -> {
                        MainApplication.get().applicationContext.startActivity(
                            Intent(
                                MainApplication.get().applicationContext,
                                AuthenticationActivity::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                . putExtra("EXTRA_IS_LOGOUT", true)
                        )
                        return response
                    }

                    else -> return response
                }
            }
        }

        operator fun invoke() = getAPIService()
    }
}