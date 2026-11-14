package com.solvay.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.solvay.Base.BaseFragment
import com.solvay.HomesActivity
import com.solvay.Localization.LocaleHelper
import com.solvay.R
import com.solvay.Utils.AlertManager
import com.solvay.Utils.Prefs
import com.solvay.authentication.authViewmodel.AuthViewModel
import com.solvay.authentication.models.RouteKeyRequest
import com.solvay.databinding.FragmentQRcodeBinding


class QRCodeFragment : BaseFragment() {
    private lateinit var mBinding: FragmentQRcodeBinding
    private lateinit var codeScanner: CodeScanner
    private val viewModel by viewModels<AuthViewModel>()
    private lateinit var alertManager: AlertManager
    var routeKey: String? = null
    var authActivity: AuthenticationActivity? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        authActivity = activity as AuthenticationActivity
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentQRcodeBinding.inflate(layoutInflater, container, false)
        alertManager = AlertManager(mBinding.root as ViewGroup, requireContext())
        observer()
        val scannerView = mBinding.scannerView
      //  showProgress(false)
        codeScanner = CodeScanner(requireContext(), scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
        //    showProgress(true)
            Log.e("hdsgahdgsadgsa", it.text.toString())
            Log.e("hdsgahdgsadgsa", it.toString())
            routeKey = it.text.toString()
            startJourney(routeKey = routeKey ?: "")
        }
        mBinding.includeProgress.clMain.visibility = View.INVISIBLE
        scannerView.post{
            scannerView.performClick()
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            findNavController().navigateUp()
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
        mBinding.llBack.setOnClickListener {
            findNavController().navigateUp()
        }
        return mBinding.root
    }

    private fun observer() {
        viewModel.routeData.observe(viewLifecycleOwner) {
            mBinding.includeProgress.clMain.visibility =View.INVISIBLE
            if (it.success == true) {
                if (it.data?.expired == true) {
                    authActivity?.showError(it.message ?: "")
                    findNavController().navigateUp()
                }else{
                    alertManager.showSuccess(it.message ?: "")
                    Prefs.init().isRouteCompleted = false
                    var intent = Intent(requireActivity(), HomesActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }


            } else {
                authActivity?.showError(it.message ?: "")
                findNavController().navigateUp()
            }
        }
    }

    private fun startJourney(routeKey: String) {
        if (validateRouteKey(routeKey)) {
            requireActivity().runOnUiThread {
                mBinding.includeProgress.clMain.visibility = View.VISIBLE
            }


            val request = RouteKeyRequest(
                route = routeKey,
                checkRoute = false,
                qr = true,
                location = listOf(authActivity?.latitude ?: 0.0, authActivity?.longitude ?: 0.0)

            )
            viewModel.getRouteKey(request)
            //showProgress(true)
            Prefs.init().isRouteKey = routeKey
        }
    }

    private fun validateRouteKey(routeKey: String): Boolean {
        if (routeKey.isEmpty()) {
            val baseContext =
                LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            authActivity?.showError(baseContext.getString(R.string.please_enter_route_key))
            return false
        }
        return true
    }

    private fun showProgress(visibility: Boolean) {
        mBinding.includeProgress.clMain.visibility = if (visibility) View.VISIBLE else View.GONE
    }
}