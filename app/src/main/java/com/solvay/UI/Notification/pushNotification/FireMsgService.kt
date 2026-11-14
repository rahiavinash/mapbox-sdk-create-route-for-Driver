package com.solvay.UI.Notification.pushNotification


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.solvay.Activity.SplashActivity
import com.solvay.BuildConfig
import com.solvay.HomesActivity
import com.solvay.MainApplication

import com.solvay.R
import com.solvay.Utils.Prefs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FireMsgService : FirebaseMessagingService() {
    val TAG = "FireMsgService"
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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FcmTokenMessage", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "MessageNotification: ${message.data}")
        createNotificationChannel(message)
        Companion.createNotificationChannel(this)
    }

    private fun createNotificationChannel(remoteMessage: RemoteMessage?) {
        Companion.createNotificationChannel(this)
        val intent = if (MainApplication.AppStateManager.isAppInForeground()) {
            Log.d("ChatMessage", "App is active")
            Intent(this, HomesActivity::class.java)
        } else {
            Log.d("ChatMessage", "App is not active")
            Intent(this, SplashActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(HomesActivity::class.java)

        val data = remoteMessage?.data
        Log.d("FireMsgService", "DATA: $data")

        val rawJsonString = data?.get("data")
        val dataObject = JSONObject(rawJsonString ?: "{}")
        val notificationDetail = dataObject.optJSONObject("notificationDetail")
        var userId: String? = null

        intent.putExtra(
            getString(R.string.notification_message),
            dataObject.optString("message", "")
        )
        intent.putExtra(
            getString(R.string.notification_description),
            dataObject.optString("description", "")
        )

        notificationDetail?.let {
            val type = it.optString("type", null)
            type?.let { typeValue ->
                intent.putExtra(getString(R.string.notification_type), typeValue)
            }
        }

        notificationDetail?.optJSONObject("sender")?.let { sender ->
            userId = sender.optString("_id", "")
            intent.putExtra(getString(R.string.notification_sender_id), sender.optString("_id", ""))
            intent.putExtra(
                getString(R.string.notification_sender_name),
                sender.optString("name", "")
            )
            intent.putExtra(
                getString(R.string.notification_sender_is_online),
                sender.optBoolean("isOnline", false)
            )
            val senderUrl = sender.optString("image", null)
            senderUrl?.let {
                intent.putExtra(getString(R.string.notification_sender_image), it)
            }
        }

        stackBuilder.addNextIntent(intent)

        val notificationId = (Date().time / 1000L % Int.MAX_VALUE).toInt()
        val pIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        createNotificationLayout(dataObject, pIntent, userId)
    }

    private fun createNotificationLayout(dataObject: JSONObject, pIntent: PendingIntent, userId: String?) {
        val notificationDetail = dataObject.optJSONObject("notificationDetail")
        val sender = notificationDetail?.optJSONObject("sender")
        val notificationType = notificationDetail?.optString("type", null)
        val description = dataObject.optString("description", "New Message")
        val message = dataObject.optString("message", getString(R.string.app_name))
        val senderName = sender?.optString("name") ?: getString(R.string.app_name)
        val imageUrl = sender?.optString("image", null)
        val imageFullUrl = BuildConfig.IMAGE_URL + (imageUrl ?: "")

        // Use a coroutine to wait for translation and build notification
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val translatedDescription = translateNotification(description)
                Log.d(TAG, "Translated description: $translatedDescription")

                val remoteViews = RemoteViews(packageName, R.layout.layout_custom_push_notification).apply {
                    setTextViewText(R.id.title, getString(R.string.app_name))
                    when (notificationType) {
                        NOTIFICATION_TYPE_CHAT -> {
                            setTextViewText(R.id.message, translatedDescription)
                            setImageViewResource(R.id.large_icon, R.drawable.appicon)
                        }
                        NOTIFICATION_TYPE_POLYGON -> {
                            setTextViewText(R.id.message, translatedDescription)
                            setImageViewResource(R.id.large_icon, R.drawable.appicon)
                        }
                        else -> {
                            setTextViewText(R.id.message, translatedDescription)
                            setImageViewResource(R.id.large_icon, R.drawable.appicon)
                        }
                    }
                }

                val remoteExpandedViews = RemoteViews(packageName, R.layout.layout_custom_push_notification_expanded).apply {
                    setTextViewText(R.id.title, getString(R.string.app_name))
                    when (notificationType) {
                        NOTIFICATION_TYPE_CHAT -> {
                            setTextViewText(R.id.message, translatedDescription)
                            setImageViewResource(R.id.large_icon, R.drawable.appicon)
                        }
                        NOTIFICATION_TYPE_POLYGON -> {
                            setTextViewText(R.id.message, translatedDescription)
                            setImageViewResource(R.id.large_icon, R.drawable.appicon)
                        }
                        else -> {
                            setTextViewText(R.id.message, translatedDescription)
                            setImageViewResource(R.id.large_icon, R.drawable.appicon)
                        }
                    }
                }

                // Build the notification
                val notificationBuilder = NotificationCompat.Builder(this@FireMsgService, getString(R.string.app_name)).apply {
                    setSmallIcon(R.drawable.app_icon_new)
                    setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    setCustomContentView(remoteViews)
                    setCustomBigContentView(remoteExpandedViews)
                    setContentIntent(pIntent)
                    setAutoCancel(true)
                    setPriority(NotificationCompat.PRIORITY_HIGH)
                    setCategory(NotificationCompat.CATEGORY_MESSAGE)
                }

                // Display the notification
                if (ActivityCompat.checkSelfPermission(
                        this@FireMsgService,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val notificationId = (Date().time / 1000L % Int.MAX_VALUE).toInt()
                    if (notificationManagerr != null) {
                        if (Prefs.init().chatOtherUserIDNotification != userId) {
                            notificationManagerr?.notify(notificationId, notificationBuilder.build())
                            Log.d(TAG, "Notification displayed with translated text: $translatedDescription")
                        } else {
                            Log.d(TAG, "Notification skipped: same user ID")
                        }
                    } else {
                        Log.e(TAG, "NotificationManager is null")
                    }
                } else {
                    Log.e(TAG, "Notification permission not granted")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification: ${e.message}")
            }
        }
    }

    override fun handleIntent(intent: Intent?) {
        try {
            if (intent?.extras != null) {
                val builder = RemoteMessage.Builder("MessagingService")
                for (key in intent.extras!!.keySet()) {
                    builder.addData(key, intent.extras!!.get(key).toString())
                }
                onMessageReceived(builder.build())
            } else {
                super.handleIntent(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling intent: ${e.message}")
            super.handleIntent(intent)
        }
    }

    private suspend fun translateNotification(message: String): String {
        if (message.isEmpty()) {
            return message
        }
        return try {
            translateText(message, Prefs.init().appLanguageCode)
        } catch (e: Exception) {
            Log.e(TAG, "Translation failed: ${e.message}")
            message // Fallback to original message
        }
    }

    private suspend fun translateText(message: String, targetLanguage: String): String = suspendCancellableCoroutine { continuation ->
        if (message.isEmpty()) {
            continuation.resume("")
            return@suspendCancellableCoroutine
        }

        val langCode = languageMap[targetLanguage.lowercase()] ?: targetLanguage
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
                Log.e(TAG, "Translation request failed: ${e.message}")
                continuation.resume(message) // Fallback to original message
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Translation request failed with code: ${response.code}")
                        continuation.resume(message)
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
                            continuation.resume(translatedText)
                        } catch (e: JSONException) {
                            Log.e(TAG, "JSON parsing error: ${e.message}")
                            continuation.resume(message)
                        }
                    } ?: run {
                        Log.e(TAG, "Response body is null")
                        continuation.resume(message)
                    }
                }
            }
        })
    }

    companion object {
        const val NOTIFICATION_TYPE_CHAT = "chat"
        const val NOTIFICATION_TYPE_POLYGON = "polygon"
        lateinit var notificationManagerr: NotificationManager

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.notification_channel_name)
                val description = context.getString(R.string.notification_channel_description)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(
                    context.getString(R.string.app_name),
                    name,
                    importance
                ).apply {
                    this.description = description
                }
                notificationManagerr = context.getSystemService(NotificationManager::class.java)
                notificationManagerr.createNotificationChannel(channel)
                Log.d("FireMsgService", "Notification channel created")
            }
        }
    }
}
