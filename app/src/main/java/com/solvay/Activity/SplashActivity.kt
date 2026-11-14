package com.solvay.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.solvay.HomesActivity
import com.solvay.R
import com.solvay.UI.Notification.pushNotification.FireMsgService
import com.solvay.Utils.Prefs
import com.solvay.authentication.AuthenticationActivity
import com.solvay.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var mBinding : ActivitySplashBinding
    private var splashBundle = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        splashBundle = createBundleToSendViaIntent(intent)

        Handler().postDelayed({
            if (Prefs.init().accessToken == ""){
                val intent = Intent(this, AuthenticationActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                if (Prefs.init().isRouteCompleted){
                    val intent = Intent(this, AuthenticationActivity::class.java)
                    startActivity(intent)
                }else{
                    val intent = Intent(this, HomesActivity::class.java)
                    if (splashBundle.getString(getString(R.string.notification_type)) != null){
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.putExtras(splashBundle)
                    }
                    startActivity(intent)
                    finish()
                }

            }
        }, 1500)
    }

    private fun createBundleToSendViaIntent(intent: Intent?): Bundle {

        val bundle = Bundle()
        val type = intent?.extras?.getString(getString(R.string.notification_type))
        when (type) {
            FireMsgService.NOTIFICATION_TYPE_CHAT -> {
                val userId = intent.extras?.getString(getString(R.string.notification_sender_id))
                val userImage = intent.extras?.getString(getString(R.string.notification_sender_image))
                val userName = intent.extras?.getString(getString(R.string.notification_sender_name))
                val isOnline = intent.extras?.getBoolean(getString(R.string.notification_sender_is_online))

                bundle.putString(getString(R.string.notification_type), type)
                bundle.putString(getString(R.string.notification_sender_id), userId)
                bundle.putString(getString(R.string.notification_sender_image), userImage)
                bundle.putString(getString(R.string.notification_sender_name), userName)
                bundle.putBoolean(getString(R.string.notification_sender_is_online), isOnline?:false)
                bundle.putBoolean("fromNotification", true)

                Log.d("ChatMessage", "userId: $userId\nuserImage : $userImage\nuserName : ${userName}\nisOnline : ${isOnline}")
            }

            FireMsgService.NOTIFICATION_TYPE_POLYGON -> {

            }
        }
        return bundle
    }
}