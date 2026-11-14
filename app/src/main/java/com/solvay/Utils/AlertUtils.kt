package com.solvay.Utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import com.solvay.R
import androidx.core.view.isVisible

class AlertManager(
    private val container: ViewGroup,
    private val localizedContext: Context // Localized context passed from fragment
) {
    private var successAlertView: View? = null
    private var errorAlertView: View? = null
    private val DISPLAY_TIME = 3000L

    init {
        initializeViews()
    }

    private fun initializeViews() {
        // Inflate with localized context
        successAlertView = LayoutInflater.from(localizedContext)
            .inflate(R.layout.layout_succes_alert, container, false).apply {
                visibility = View.GONE
                findViewById<View>(R.id.alert_close)?.setOnClickListener {
                    hideSuccessAlert()
                }
            }
        container.addView(successAlertView)

        errorAlertView = LayoutInflater.from(localizedContext)
            .inflate(R.layout.layout_error_alert, container, false).apply {
                visibility = View.GONE
                findViewById<View>(R.id.alert_close)?.setOnClickListener {
                    hideErrorAlert()
                }
            }
        container.addView(errorAlertView)
    }

    fun showError(message: String) {
        showAlert(message, isError = true)
    }

    fun showSuccess(message: String) {
        showAlert(message, isError = false)
    }

    private fun showAlert(message: String, isError: Boolean) {
        val targetView = if (isError) errorAlertView else successAlertView
        targetView?.let { view ->
            view.findViewById<TextView>(R.id.alert_message).text = message

            // Slide down
            view.translationY = -view.height.toFloat()
            view.isVisible = true

            view.animate()
                .translationY(0f)
                .setDuration(300)
                .start()

            view.postDelayed(
                { if (isError) hideErrorAlert() else hideSuccessAlert() },
                DISPLAY_TIME
            )
        }
    }

    fun hideErrorAlert() {
        errorAlertView?.hideWithAnimation()
    }

    fun hideSuccessAlert() {
        successAlertView?.hideWithAnimation()
    }

    private fun View.hideWithAnimation() {
        animate()
            .translationY(-height.toFloat())
            .setDuration(300)
            .withEndAction { isVisible = false }
            .start()
    }

    fun cleanup() {
        container.removeView(successAlertView)
        container.removeView(errorAlertView)
        successAlertView = null
        errorAlertView = null
    }
}
