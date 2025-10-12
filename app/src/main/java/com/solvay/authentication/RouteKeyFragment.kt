package com.solvay.authentication

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.common.eventbus.Subscribe
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.solvay.HomesActivity
import com.solvay.Localization.LocaleHelper
import com.solvay.MainApplication
import com.solvay.Network.SocketIOImpl
import com.solvay.R
import com.solvay.UI.chat.model.ChatHistory
import com.solvay.Utils.AlertManager
import com.solvay.Utils.Prefs
import com.solvay.Utils.Utils
import com.solvay.Utils.Utils.showError
import com.solvay.Utils.isSocketConnected
import com.solvay.authentication.authViewmodel.AuthViewModel
import com.solvay.authentication.models.RouteKeyRequest
import com.solvay.databinding.FragmentRouteKeyBinding
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import io.github.g00fy2.quickie.content.QRContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class RouteKeyFragment : Fragment() {
    private lateinit var binding: FragmentRouteKeyBinding
    private val viewModel by viewModels<AuthViewModel>()
    private lateinit var alertManager: AlertManager
    var routeKey: String? = null
    private lateinit var socket: SocketIOImpl
var authActivity: AuthenticationActivity? = null
    val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) { results ->
        handleResult(results)
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
//            scanQrCodeLauncher.launch(null)
            findNavController().navigate(R.id.QRCodeFragment3)
        }
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        authActivity = activity as AuthenticationActivity
      //  (activity as AuthenticationActivity).callSocketConnection()
        socket = MainApplication.get().socket
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
        binding = FragmentRouteKeyBinding.inflate(localizedInflater, container, false)
        observer()
        alertManager = AlertManager(binding.root as ViewGroup, requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (::binding.isInitialized) {

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)

            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                socket.disconnect()
                findNavController().navigate(R.id.registerFragment)
            }



            binding.apply {
                showProgress(false)
                ivScanQr.setOnClickListener {
                 //   openQrScanner()
                    findNavController().navigate(R.id.QRCodeFragment3)
                }
                btStart.setOnClickListener {
                    routeKey = etRouteKey.text.toString()
                    startJourney(routeKey =  routeKey?:"" , qr=false)
                }

                llBack.setOnClickListener {
                    socket.disconnect()
                    findNavController().navigate(R.id.registerFragment)
                }
            }
        }

    }

    private fun handleResult(results: QRResult) {
        val text = when (results) {
            is QRResult.QRSuccess -> {
                results.content.rawValue
                    ?: results.content.rawBytes?.let { String(it) }.orEmpty()
            }

            QRResult.QRUserCanceled -> getString(R.string.user_canceled)
            QRResult.QRMissingPermission -> getString(R.string.missing_permission)
            is QRResult.QRError -> "${results.exception.javaClass.simpleName}: ${results.exception.localizedMessage}"
        }

        if (results is QRResult.QRSuccess) {
            routeKey = results.content.rawValue?:""
            startJourney(routeKey =  routeKey?:"" , qr = true)
        } else {
            Log.d(getString(R.string.qr_scan_result), "QrResult : ${text}")
        }
    }

    private fun openQrScanner() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            scanQrCodeLauncher.launch(null)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startJourney(routeKey: String  , qr:Boolean){
        if (validateRouteKey(routeKey)){
            val request = RouteKeyRequest(
                route = routeKey,
                qr = qr,
                checkRoute = false,
                location = listOf(authActivity?.latitude ?: 0.0, authActivity?.longitude ?: 0.0)
            )
            viewModel.getRouteKey(request)
            showProgress(true)
            Prefs.init().isRouteKey = routeKey
        }
    }

    private fun observer(){
        viewModel.routeData.observe(viewLifecycleOwner) {
            showProgress(false)
            if (it.success == true){
                if (it.data?.expired == true) {
                    authActivity?.showError(it.message ?: "")
                    findNavController().navigateUp()
                }else{
                    alertManager.showSuccess( it.message?:"")
                    Prefs.init().isRouteCompleted = false
                    var intent =Intent(requireActivity() , HomesActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }else{
                authActivity?.showError( it.message?:"")
            }
        }
    }

    private fun validateRouteKey(routeKey: String): Boolean {
        if (routeKey.isEmpty()) {
            val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            authActivity?.showError(baseContext.getString(R.string.please_enter_route_key))
            return false
        }
        return true
    }

    override fun onAttach(context: Context) {
        val newContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        super.onAttach(newContext)
    }

    private fun showProgress(visibility: Boolean){
        binding.includeProgress.clMain.visibility = if(visibility) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()

        socket.connectSocketAndListenForEvents { isConnected ->
            if (isConnected) {
                Log.e("SocketStatus", "Socket is connected")
                setSocketListenON()
            } else {
                Log.e("SocketStatus", "Socket failed to connect or disconnected")
            }
        }
    }

    private fun setSocketListenON() {
        //   socket.listenOff("chatSuccess")
        socket.listenOff("routeAssigned")

        socket.listenOff("error")
        socket.listenOn("error") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                val obj = JSONObject(args[0].toString())
                Log.e("jsakhdjdhsad", obj.toString())
                // Utils.showToast(message)
            }
        }
        socket.listenOn("routeAssigned") { args ->
            CoroutineScope(Dispatchers.Main).launch {
                Log.e("payloadValue", "calllll")
                val obj = JSONObject(args[0].toString())
                val routeKey = obj.optString("route", "")
                Log.e("payloadValue", "routeKey" + routeKey.toString())
                var intent =Intent(requireActivity() , HomesActivity::class.java)
                startActivity(intent)
               // startJourney(routeKey =  routeKey?:"" , qr=false)
//                val userId = obj.getString("user")
//                val isOnline = obj.getBoolean("isOnline")
//                Log.e("UserID", userId)

            }
        }

    }

    @Subscribe()
    fun isSocketConnected(event: isSocketConnected) {
        if (event.isConnected) {
            setSocketListenON()
            //    getNotificationCount()
        }
    }
}