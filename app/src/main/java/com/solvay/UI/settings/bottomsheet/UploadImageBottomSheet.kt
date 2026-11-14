package com.solvay.UI.settings.bottomsheet

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.solvay.Localization.LocaleHelper
import com.solvay.R
import com.solvay.UI.settings.dataModels.ProfileData
import com.solvay.UI.settings.dataModels.ProfileImageUpdation
import com.solvay.UI.settings.dataModels.ProfileResponse
import com.solvay.UI.settings.viewModel.SettingsViewModel
import com.solvay.Utils.AlertManager
import com.solvay.Utils.EventObserver
import com.solvay.Utils.Prefs
import com.solvay.databinding.FragmentUploadImageBottomSheetBinding
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

class UploadImageBottomSheet : BottomSheetDialogFragment() {
    private lateinit var mBinding: FragmentUploadImageBottomSheetBinding
    private val viewModel by viewModels<SettingsViewModel>()
    private lateinit var alertManager: AlertManager
    var dialogs: Dialog? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val GALLERY_PERMISSION_REQUEST_CODE = 101
    private val CAMERA_REQUEST_CODE = 102
    private val GALLERY_REQUEST_CODE = 103
    private var cameraImageUri: Uri? = null
    private var tempCameraFile: File? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.2f)

            findViewById<View>(com.google.android.material.R.id.container)?.fitsSystemWindows = false
            findViewById<View>(com.google.android.material.R.id.coordinator)?.fitsSystemWindows = false
        }

        dialog.behavior.isFitToContents = false
        dialog.behavior.skipCollapsed = true

        return dialog
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
        mBinding =
            FragmentUploadImageBottomSheetBinding.inflate(localizedInflater, container, false)
        dialogs = Dialog(requireActivity())
        dialogs?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            Log.d("CameraSuccess", "Success: $success, cameraImageUri: $cameraImageUri")
            if (success && cameraImageUri != null) {
                org.greenrobot.eventbus.EventBus.getDefault()
                    .post(ProfileImageUpdation(imageUri = cameraImageUri))
                dismiss()
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                org.greenrobot.eventbus.EventBus.getDefault()
                    .post(ProfileImageUpdation(imageUri = uri))
                dismiss()
            }
        }

        return mBinding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("camera_uri", cameraImageUri)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alertManager = AlertManager(mBinding.root as ViewGroup, requireContext())
        cameraImageUri = savedInstanceState?.getParcelable("camera_uri") ?: cameraImageUri
        val offsetFromTop = 200
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            isFitToContents = true
            expandedOffset = offsetFromTop
            (requireView().parent as View).setBackgroundColor(Color.TRANSPARENT)
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        dialog?.window?.setDimAmount(0.2f)
        mBinding.rlCamera.setOnClickListener {
            openCamera()
        }

        mBinding.rlGallery.setOnClickListener {
            openGallery()
        }
    }

    private fun openCamera(){
        if (checkCameraPermission()) {
            openCameraPicker()
        } else {
            requestCameraPermission()
        }
    }

    private fun openGallery(){
         if (checkGalleryPermission()) {
             openGalleryPicker()
         } else {
             requestGalleryPermission()
         }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED &&
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
    }

    private fun checkGalleryPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestCameraPermission() {
        val permissions = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(Manifest.permission.CAMERA)
        }
        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions,
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(permission),
            GALLERY_PERMISSION_REQUEST_CODE
        )
    }

    fun openCameraPicker() {
        if (!checkCameraPermission()) {
            requestCameraPermission()
            return
        }

        try {
            tempCameraFile = createImageFile()
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                tempCameraFile!!
            )
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, getString(R.string.could_not_create_file), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun openGalleryPicker() {
        if (!checkGalleryPermission()) {
            requestGalleryPermission()
            return
        }

        galleryLauncher.launch("image/*")
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        storageDir?.mkdirs()

        return File(storageDir, "JPEG_${timeStamp}_.jpg")
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    // Permission denied, show message to user
                }
            }
            GALLERY_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    // Permission denied, show message to user
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        val newContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        super.onAttach(newContext)
    }


}