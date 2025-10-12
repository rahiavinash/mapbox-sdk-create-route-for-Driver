package com.solvay.Utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.solvay.Utils.Utils.dialog
import com.solvay.databinding.LayoutDialogBinding

object DialogUtils {
    fun showCustomDialog(
        context: Context,
        titleMsg: String?,
        textMsg: String?,
        imageIcon: Int?=null,
        buttonText: String? = null,
        buttonTextLeft: String? = null,
        buttonTextRight: String? = null,
        singleButton: Boolean = true,
        fullscreen: Boolean = false,
        buttonClick: (() -> Unit)? = null,
        buttonClickLeft: (() -> Unit)? = null,
        buttonClickRight: (() -> Unit)? = null
    ): Dialog {
        val binding = LayoutDialogBinding.inflate(LayoutInflater.from(context))

        val dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(binding.root)
        }

        binding.apply {
            if (singleButton) {
                llButtons.visibility = View.GONE
                btDialog.visibility = View.VISIBLE

                btDialog.text = buttonText.orEmpty()

                btDialog.setOnClickListener {
                    buttonClick?.invoke()
                    dialog.dismiss()
                }
            } else {
                llButtons.visibility = View.VISIBLE
                btDialog.visibility = View.GONE

                btDialogLeftSide.text = buttonTextLeft.orEmpty()
                btDialogRightSide.text = buttonTextRight.orEmpty()

                btDialogLeftSide.setOnClickListener {
                    buttonClickLeft?.invoke()
                    dialog.dismiss()
                }

                btDialogRightSide.setOnClickListener {
                    buttonClickRight?.invoke()
                    dialog.dismiss()
                }
            }

            tvTitle.text = titleMsg.orEmpty()
            tvText.text = textMsg.orEmpty()
            tvText.text = textMsg.orEmpty()
            if (imageIcon!=null){
                ivDialog.visibility=View.VISIBLE
                ivDialog.setImageResource(imageIcon)
            }else{
                ivDialog.visibility=View.GONE
            }

        }

        dialog.show()

        // 🔥 Set width after show()
        dialog.show()

// Get screen width and apply 90% width for horizontal margins
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val desiredWidth = (screenWidth * 0.9).toInt() // 90% of screen

        dialog.window?.setLayout(
            desiredWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return dialog
    }
}
