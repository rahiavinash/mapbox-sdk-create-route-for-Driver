package com.solvay.UI.chat

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.solvay.Base.BaseFragment
import com.solvay.Localization.LocaleHelper
import com.solvay.MainApplication
import com.solvay.Network.SocketIOImpl
import com.solvay.R
import com.solvay.UI.chat.ChatViewModel.ChatViewModels
import com.solvay.Utils.EventObserver
import com.solvay.Utils.Prefs
import com.solvay.Utils.Utils.disableMultiTap
import com.solvay.databinding.FragmentSelectImageAndVideoBinding
import java.util.Objects


/**
 * A simple [Fragment] subclass.
 * Use the [SelectImageAndVideoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SelectImageAndVideoFragment : BaseFragment() {
 private lateinit var mBinding :FragmentSelectImageAndVideoBinding
    private val authViewModel by viewModels<ChatViewModels>()
    private var chatImageUri: Uri = Uri.EMPTY
    private var userChatId = ""
    private var base64Image = ""
    var dialog : Dialog?=null
    private lateinit var socket: SocketIOImpl
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
        mBinding =FragmentSelectImageAndVideoBinding.inflate(localizedInflater, container , false)
        dialog = Dialog(requireActivity())
        dialog?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        val uri=arguments?.getString("chatImageUri").toString()
        userChatId=arguments?.getString("chatUserID").toString()
        chatImageUri=uri.toUri()
        mBinding.ivChatImage.setImageURI(chatImageUri)
        socket = MainApplication.get().socket

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //imageUploadObserver()
        mBinding.IvBack.setOnClickListener {
            mBinding.ivSendButton.disableMultiTap()
            findNavController().navigateUp()
        }
        mBinding.ivSendButton.setOnClickListener {
            mBinding.IvBack.disableMultiTap()
            /** Start checking internet periodically*/
            if (isInternetAvailable(requireContext())){
                /** Convert the single image URI to base64*/
                // base64Image = ImagesVideoConverter.uriToBase64(requireContext(), chatImageUri!!)!!
//                base64Image = ImagesVideoConverter.uriToBase64(requireContext(), chatImageUri).toString()
//                preparingPopUp()
//                InternetChecker(requireContext()).startInternetCheck { isConnected ->
//                    if (isConnected) {
//                        authViewModel.chatImageUpload(
//                            ChatImageUpload(
//                                file = base64Image,
//                                ext = "jpg",
//                                localId = "1234"
//                            )
//                        )
//
//                    } else {
//                        dialog?.dismiss()
//                        Utils.showToast("Please check your internet connection.")
//                        Log.e("internet" , "internet")
//                        // Internet is not available, show a toast or perform some action
//                        // Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
//                    }
//                }

            }else{
                dialog?.dismiss()
                //     Utils.showToast("Please check your internet connection.")
            }

        }
    }
//    private fun imageUploadObserver() {
//            authViewModel.uploadMediaResponseData.observe(viewLifecycleOwner, EventObserver { response ->
//            if (response != null) {
//                if (response.success) {
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        org.greenrobot.eventbus.EventBus.getDefault()
//                            .post(ImageUploaded(response.data?.url.toString()))
//                    },500)
//                    dialog?.dismiss()
//                    findNavController().navigateUp()
//                }
//            }
//        })
//                    authViewModel.showLoading.observe(requireActivity()) {
//        }
//    }
    private fun preparingPopUp() {
        if (context!=null){
            if (!requireActivity().isFinishing) {
                dialog?.setCancelable(false)
                Objects.requireNonNull(dialog?.window)?.setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT
                    )
                )
         //       dialog?.setContentView(R.layout.preparing_please_wait)
                dialog?.show()
            }
        }

    }

    override fun onAttach(context: Context) {
        val newContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        super.onAttach(newContext)
    }
}