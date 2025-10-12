package com.solvay.authentication

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.solvay.Localization.LocaleHelper
import com.solvay.R
import com.solvay.Utils.AlertManager
import com.solvay.Utils.Prefs
import com.solvay.Utils.Utils
import com.solvay.Utils.Utils.showError
import com.solvay.authentication.authViewmodel.AuthViewModel
import com.solvay.data.response.LoginRequest
import com.solvay.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {
    private lateinit var mBinding: FragmentRegisterBinding
    private val viewModel by viewModels<AuthViewModel>()
    private var appLanguage: String? = null
    private var appLanguageCode: String? = null
    private var appLanguageValue: String? = null
    private lateinit var firebaseInstance: FirebaseMessaging
    private lateinit var alertManager: AlertManager
    private var userName: String? = null
    private var plateNum: String? = null
    private var contractNum: String? = null
    private lateinit var locationCallback: LocationCallback
    var latitude = 0.0
    var longitude = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        appLanguage = arguments?.getString(getString(R.string.language)) ?: Prefs.init().appLanguage
        appLanguageCode = arguments?.getString(getString(R.string.selected_language_code)) ?: Prefs.init().appLanguageCode
        appLanguageValue = arguments?.getString(getString(R.string.selected_language_value)) ?: Prefs.init().appLanguageValue

        val baseContext = LocaleHelper.setLocale(requireContext(), appLanguageCode ?: "de")

        // Wrap the context with your app theme
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)

        mBinding = FragmentRegisterBinding.inflate(localizedInflater, container, false)
        firebaseInstance = FirebaseMessaging.getInstance()
        generateFcmToken()
        observer()
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (::mBinding.isInitialized){
//            if (Prefs.init().isRouteCompleted){
//                findNavController().navigate(R.id.routeKeyFragment)
//            }

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            locationRequest = LocationRequest.create().apply {
                interval = 5000 // in milliseconds
                fastestInterval = 2000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        val latitude = location.latitude
                        val longitude = location.longitude
                        Log.d("LiveLocation", "Lat: $latitude, Lng: $longitude")
                    }
                }
            }

            showProgress(false)
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                val navController = findNavController()
                if (navController.previousBackStackEntry?.destination?.id == R.id.routeKeyFragment) {
                    val appLanguage = Prefs.init().appLanguage
                    val appLanguageCode = Prefs.init().appLanguageCode
                    val appLanguageValue = Prefs.init().appLanguageValue
                    Prefs.init().clear()
                    Prefs.init().appLanguage = appLanguage
                    Prefs.init().appLanguageCode = appLanguageCode
                    Prefs.init().appLanguageValue = appLanguageValue
                    findNavController().navigate(R.id.languageSelectionFragment)
                }
            }


            setupTouchListenerToHideKeyboard(view)
            alertManager = AlertManager(mBinding.root as ViewGroup, requireContext())
            mBinding.apply {

                etName.setText(Prefs.init().user_name)
                etPlate.setText(Prefs.init().plateNumber)
                etAuftrag.setText(Prefs.init().contractNumber)

                llBack.setOnClickListener {
                    val appLanguage = Prefs.init().appLanguage
                    val appLanguageCode = Prefs.init().appLanguageCode
                    val appLanguageValue = Prefs.init().appLanguageValue
                    Prefs.init().clear()
                    Prefs.init().appLanguage = appLanguage
                    Prefs.init().appLanguageCode = appLanguageCode
                    Prefs.init().appLanguageValue = appLanguageValue
                    findNavController().navigate(R.id.languageSelectionFragment)
                }

                btNext.setOnClickListener {
                    val firstName = etName.text.toString().trim()
                    val plateNumber = etPlate.text.toString().trim()
                    val aufTragNumber = etAuftrag.text.toString().trim()
                    register(firstName, plateNumber, aufTragNumber)
                }
            }
        }
    }

    private fun validateInputs(
        firstName: String,
        plateNumber: String,
        aufTragNumber: String
    ): Boolean {
        if (firstName.isEmpty()) {
            val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            alertManager.showError(baseContext.getString(R.string.please_enter_your_name))

            return false
        }

        if (firstName.length < 3){
            val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            alertManager.showError(baseContext.getString(R.string.your_name_length))
            return false
        }

        if (plateNumber.isEmpty()) {
            val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            alertManager.showError(baseContext.getString(R.string.please_enter_plate_number))
            return false
        }

        if (aufTragNumber.isEmpty()) {
            val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            alertManager.showError(baseContext.getString(R.string.please_enter_auftrag_number))

            return false
        }

        return true
    }

    private fun register(firstName: String, plateNumber: String, aufTragNumber: String) {
        val deviceToken = Prefs.init().deviceToken

 var activity = activity as AuthenticationActivity
        if(validateInputs(
            firstName, plateNumber, aufTragNumber
        )){
            val request = LoginRequest(
                deviceToken = deviceToken,
                fcmToken = deviceToken,
                contractNumber = aufTragNumber,
                plateNumber = plateNumber,
                name = firstName,
                language = appLanguageValue,
                location = listOf(activity.latitude ?: 0.0, activity.longitude ?: 0.0)
            )
            userName = firstName
            plateNum = plateNumber
            contractNum = aufTragNumber
            viewModel.login(loginRequest = request)
            showProgress(true)
        }

    }

    fun observer(){
        viewModel.loginData.observe(viewLifecycleOwner) {
            showProgress(false)
            if (it.success){
                val accessToken = it.data?.accessToken?:""
                val refreshToken = it.data?.refreshToken?:""
                val userId = it.data?.userId?:""

                if (accessToken != "" && refreshToken != "" && appLanguageValue != ""){
                    Prefs.init().accessToken = accessToken
                    Prefs.init().refreshToken = refreshToken
                    Prefs.init().appLanguage = appLanguage?:""
                    Prefs.init().appLanguageCode = appLanguageCode?:"de"
                    Prefs.init().appLanguageValue = appLanguageValue?:"german"
                    Prefs.init().userId = userId

                    Prefs.init().user_name = userName?:""
                    Prefs.init().plateNumber = plateNum?:""
                    Prefs.init().contractNumber = contractNum?:""
                    (activity as AuthenticationActivity).callSocketConnection()
                    findNavController().navigate(R.id.routeKeyFragment)
                }else{
                    val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
                    alertManager.showError(baseContext.getString(R.string.something_went_wrong))
                }
            }else{
                alertManager.showError(it.message?:"")
            }
            Prefs.init().appLanguageCodeTemp = ""
        }
    }

    private fun generateFcmToken() {
        try {
            if (FirebaseApp.getApps(requireContext()).isNotEmpty()) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let { token ->
                            Prefs.init().deviceToken = token
                            Log.d(getString(R.string.fcm), "Token: $token")
                        }
                    } else {
                        Log.e(getString(R.string.fcm), getString(R.string.fetching_token_failed), task.exception)
                    }
                }
            } else {
                Log.e(getString(R.string.fcm), getString(R.string.firebase_not_initialized))

            }
        } catch (e: Exception) {
            Log.e(getString(R.string.fcm), getString(R.string.error_getting_fcm_token), e)
        }
    }

    override fun onAttach(context: Context) {
        val languageCode = if (Prefs.init().appLanguageCodeTemp != "") {
            Prefs.init().appLanguageCodeTemp
        }else if(Prefs.init().appLanguageCode != ""){
            Prefs.init().appLanguageCode
        }else{
            "en"
        }
        Log.d("LngCode","LanguageCode: $languageCode")
        val newContext = LocaleHelper.setLocale(context, languageCode ?: "en")
        super.onAttach(newContext)
    }

    private fun setupTouchListenerToHideKeyboard(view: View) {
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                hideKeyboard()
                view.clearFocus()
                false
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setupTouchListenerToHideKeyboard(view.getChildAt(i))
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showProgress(visibility: Boolean){
        mBinding.includeProgress.clMain.visibility = if(visibility) View.VISIBLE else View.GONE
    }
}