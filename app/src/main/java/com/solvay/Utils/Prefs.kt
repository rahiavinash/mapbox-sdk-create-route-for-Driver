package com.solvay.Utils

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.solvay.MainApplication


class Prefs {

    private var sharedPreferences: SharedPreferences =
        MainApplication.get().getSharedPreferences(APP_NAME, MODE_PRIVATE)

    init {
        instance = this
    }

    companion object {
        private const val APP_NAME = "WEDO80)"
        private const val ISPROGRESSUPLOAD = "ISPROGRESSUPLOAD)"
        private const val IS_ROUTE_COMPLETED = "IS_ROUTE_COMPLETED"
        private const val IS_SWITCH = "IS_SWITCH"
        private const val IS_TEST = "IS_TEST"
        private const val ACCESS_TOKEN = "ACCESS_TOKEN"
        private const val REFRESH_TOKEN = "REFRESH_TOKEN"
        private const val SORT = "SORT"
        private const val DEVICE_TOKEN = "DEVICE_TOKEN"
        private const val APP_LANGUAGE = "APP_LANGUAGE"
        private const val APP_LANGUAGE_CODE = "APP_LANGUAGE_CODE"
        private const val LATITUDE = "LATITUDE"
        private const val LONGITUDE = "LONGITUDE"
        private const val APP_LANGUAGE_VALUE = "APP_LANGUAGE_VALUE"
        private const val APP_LANGUAGE_CODE_TEMP = "APP_LANGUAGE_CODE_TEMP"
        private const val FINAL_TAG_LISt = "FINAL_TAG_LISt"
        private const val PERMISSION_CLICK_GALLERY = "PERMISSION_CLICK_GALLERY"
        private const val USER_ID = "USER_ID"
        private const val NAME = "NAME"
        private const val USER_PROFILE = "USER_PROFILE"
        private const val USERNAME = "USERNAME"
        private const val OTHER_USER_ID = "OTHER_USER_ID"
        private const val USER_NAME = "USER_NAME"
        private const val PLATE_NUMBER = "PLATE_NUMBER"
        private const val CONTRACT_NUMBER = "CONTRACT_NUMBER"
        private const val PROFILE_FILE = "PROFILE_FILE"
        private const val CHAT_ID_NOT = "CHAT_ID_NOT"
        private const val REMOVE_POST_TYPE = "REMOVE_POST_TYPE"
        private const val REMOVE_SAVE_POST_ID = "REMOVE_SAVE_POST_ID"
        private const val REMOVE_ARCHIVE_POST_ID = "REMOVE_ARCHIVE_POST_ID"
        private const val AR_COUNT = "AR_COUNT"
        private const val IS_LH_POST = "IS_LH_POST"
        private const val IS_GHOST_MODE = "IS_GHOST_MODE"
        private const val ROUTE_KEY = "ROUTE_KEY"
        private const val PAYMENT_SUCCESS = "PAYMENT_SUCESS"
        private const val IS_APP_BACKGROUND = "IS_APP_BACKGROUND"
        private const val FIRST_TIME_OPENED = "FIRST_TIME_OPENED"
        private const val POP_UP_SHOULD_SHOW = "POP_UP_SHOULD_SHOW"
        private const val HIDE_UPLOAD_POPUP = "HIDE_UPLOAD_POPUP"
        private const val MESSAGE_SENDER_ID = "MESSAGE_SENDER_ID"
        private const val BADGE_COUNT = "BADGE_COUNT"
        private const val SELECTED_CONTENT_TYPE = "SELECTED_CONTENT_TYPE"
        private const val CONTENT_TYPE_LIST = "CONTENT_TYPE_LIST"
        private const val USER_CONTENT_TYPE_LIST = "USER_CONTENT_TYPE_LIST"
        private const val NOTIFICATION_POINT = "NOTIFICATION_POINT"
        private const val AI_ENABLE = "AI_ENABLE"
        private const val NO_ADS = "AI_ENABLE"
        private const val BLUE_TICK = "BLUE_TICK"
        private const val DAILY_AR_POP_UP = "DAILY_AR_POP_UP"
        private const val WEEKLY_AR_POP_UP = "WEEKLY_AR_POP_UP"
        private const val IS_USER_LOGGED_IN = "IS_USER_LOGGED_IN"
        private const val IS_MESSAGE_USER_LIST = "IS_MESSAGE_USER_LIST"
        private const val IS_NOTIFICATION_USER_LIST = "IS_NOTIFICATION_USER_LIST"
        private const val CONTENT_TYPE_LIST_ONE = "CONTENT_TYPE_LIST_ONE"
        private const val AR_COUNTER_ON_INTERNET_DISCONNECT = "AR_COUNTER_ON_INTERNET_DISCONNECT"
        private const val CONTENT_TYPE_WHOLE_USER_LIST = "CONTENT_TYPE_WHOLE_USER_LIST"

        private var instance: Prefs? = null
        fun init(): Prefs {
            if (instance == null) {
                instance = Prefs()
            }
            return instance!!
        }
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    var isRouteCompleted: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(IS_ROUTE_COMPLETED, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(IS_ROUTE_COMPLETED, value).apply()
        }
    var ar: Float
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getFloat(AR_COUNT, 0.0f)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putFloat(AR_COUNT, value).apply()
        }
    var isTest: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(IS_TEST, true)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(IS_TEST, value).apply()
        }

    var accessToken: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(ACCESS_TOKEN, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(ACCESS_TOKEN, value).apply()
        }

    var user_name: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(USER_NAME, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(USER_NAME, value).apply()
        }

    var plateNumber: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(PLATE_NUMBER, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(PLATE_NUMBER, value).apply()
        }

    var contractNumber: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(CONTRACT_NUMBER, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(CONTRACT_NUMBER, value).apply()
        }
    var refreshToken: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(REFRESH_TOKEN, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(REFRESH_TOKEN, value).apply()
        }
    var sort: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(SORT, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(SORT, value).apply()
        }
    var appLanguage: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(APP_LANGUAGE, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(APP_LANGUAGE, value).apply()
        }

    var appLanguageCode: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(APP_LANGUAGE_CODE, "") ?: "de"
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(APP_LANGUAGE_CODE, value).apply()
        }

    var latitude: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(LATITUDE, "") ?: "de"
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(LATITUDE, value).apply()
        }



    var appLanguageValue: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(APP_LANGUAGE_VALUE, "") ?: "german"
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(APP_LANGUAGE_VALUE, value).apply()
        }

    var appLanguageCodeTemp: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(APP_LANGUAGE_CODE_TEMP, "") ?: "de"
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(APP_LANGUAGE_CODE_TEMP, value).apply()
        }
    var deviceToken: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(DEVICE_TOKEN, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(DEVICE_TOKEN, value).apply()
        }
    var isLHPost: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(IS_LH_POST, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(IS_LH_POST, value).apply()
        }
    var isGhostMode: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(IS_GHOST_MODE, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(IS_GHOST_MODE, value).apply()
        }
    var SortByEnd: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(SELECTED_CONTENT_TYPE, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(SELECTED_CONTENT_TYPE, value).apply()
        }
    var paymentSuccess: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(PAYMENT_SUCCESS, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(PAYMENT_SUCCESS, value).apply()
        }

    var isRouteKey: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(ROUTE_KEY, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(ROUTE_KEY, value).apply()
        }

    var permissionClickGallery: Int
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getInt(PERMISSION_CLICK_GALLERY, 0) ?: 0
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putInt(PERMISSION_CLICK_GALLERY, value).apply()
        }
    var userId: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(USER_ID, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(USER_ID, value).apply()
        }

    var name: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(NAME, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(NAME, value).apply()
        }

    var chatOtherUserID: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(OTHER_USER_ID, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(OTHER_USER_ID, value).apply()
        }

    var userProfile: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(USER_PROFILE, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(USER_PROFILE, value).apply()
        }


    var profileId: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(PROFILE_FILE, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(PROFILE_FILE, value).apply()
        }

    var chatOtherUserIDNotification: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(CHAT_ID_NOT, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(CHAT_ID_NOT, value).apply()
        }
    var removePostType: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(REMOVE_POST_TYPE, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(REMOVE_POST_TYPE, value).apply()
        }

    var removeSavePostId: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(REMOVE_SAVE_POST_ID, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(REMOVE_SAVE_POST_ID, value).apply()
        }
    var removeArchivePostId: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(REMOVE_ARCHIVE_POST_ID, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(REMOVE_ARCHIVE_POST_ID, value).apply()
        }
    var isAppBackground: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(IS_APP_BACKGROUND, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(IS_APP_BACKGROUND, value).apply()
        }

    var firstTimeOpened: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(FIRST_TIME_OPENED, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(FIRST_TIME_OPENED, value).apply()
        }
    var hideUploadProgress: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(HIDE_UPLOAD_POPUP, true)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(HIDE_UPLOAD_POPUP, value).apply()
        }
    var popUpShown: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(POP_UP_SHOULD_SHOW, true)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(POP_UP_SHOULD_SHOW, value).apply()
        }
    var badgeCount: Int
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getInt(BADGE_COUNT, 0) ?: 0
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putInt(BADGE_COUNT, value).apply()
        }
    var messageSenderId: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(MESSAGE_SENDER_ID, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(MESSAGE_SENDER_ID, value).apply()
        }


    var contentTypeList: String
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getString(CONTENT_TYPE_LIST, "") ?: ""
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putString(CONTENT_TYPE_LIST, value).apply()
        }
    var aiEnable: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(AI_ENABLE, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(AI_ENABLE, value).apply()
        }
    var noAds: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(NO_ADS, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(NO_ADS, value).apply()
        }
    var isUploadProgress: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(ISPROGRESSUPLOAD, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(ISPROGRESSUPLOAD, value).apply()
        }
    var blueTickEnabled: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(BLUE_TICK, false)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(BLUE_TICK, value).apply()
        }

    var dailyArPopupShown: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(DAILY_AR_POP_UP, true)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(DAILY_AR_POP_UP, value).apply()
        }

    var weeklyArPopUpShown: Boolean
        get() {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            return sF.getBoolean(WEEKLY_AR_POP_UP, true)
        }
        set(value) {
            val sF = MainApplication.get().applicationContext
                .getSharedPreferences(APP_NAME, MODE_PRIVATE)
            sF.edit().putBoolean(WEEKLY_AR_POP_UP, value).apply()
        }


    inline fun <reified T> String.fromJson(): List<T> {
        val gson = Gson()
        return gson.fromJson(this, object : TypeToken<List<T>>() {}.type)
    }
}