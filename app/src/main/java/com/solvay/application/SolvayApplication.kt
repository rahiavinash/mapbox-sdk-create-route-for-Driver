package com.solvay.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.solvay.Localization.LocaleHelper
import com.solvay.Network.SocketIOImpl
import com.solvay.Utils.Prefs
import java.util.Locale

class SolvayApplication : Application() {

    companion object {
        lateinit var instance: SolvayApplication
        fun get(): SolvayApplication = instance
    }

    private var mCurrentActivity: Activity? = null
    val socket= SocketIOImpl()

    fun setCurrentActivity(mCurrentActivity: Activity?) {
        this.mCurrentActivity = mCurrentActivity
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        registerActivityLifecycleCallbacks(object: ActivityLifecycleCallbacks{
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
                AppStateManager.setAppForegroundState(true)
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
                if (activity.isFinishing.not()) {
                    AppStateManager.setAppForegroundState(false)
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }

        })
    }

    override fun attachBaseContext(base: Context) {
        val languageCode = Prefs.init().appLanguageCode
        val context = LocaleHelper.setLocale(base, languageCode)
        super.attachBaseContext(context)
    }
    object AppStateManager {
        private var isAppInForeground = false

        fun setAppForegroundState(isForeground: Boolean) {
            isAppInForeground = isForeground
        }

        fun isAppInForeground(): Boolean = isAppInForeground
    }
}