package com.solvay.authentication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mapbox.geojson.Point
import com.solvay.Localization.LocaleHelper
import com.solvay.MainApplication
import com.solvay.Network.SocketIOImpl
import com.solvay.R
import com.solvay.Utils.AlertManager
import com.solvay.Utils.Prefs
import com.solvay.databinding.ActivityAuthenticationBinding

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityAuthenticationBinding
    lateinit var navController: NavController
    private lateinit var alertManager: AlertManager
    private lateinit var socket: SocketIOImpl
    private val LOCATION_SETTINGS_REQUEST_CODE = 1002 // Added for GPS settings check
    private val REQUEST_APP_SETTINGS = 101
     var latitude = 0.0
     var longitude = 0.0
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val REQUEST_CODE = 123
    // Add FusedLocationProviderClient for GPS location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAuthenticationBinding.inflate(layoutInflater)
        socket = MainApplication.get().socket
        alertManager = AlertManager(mBinding.root as ViewGroup, localizedContext = this)
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Initialize LocationRequest
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // Update every 10 seconds
            fastestInterval = 5000 // Fastest update interval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // GPS accuracy
        }

        // Initialize LocationCallback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.d("HomesActivity", "Location updated: Lat=$latitude, Lng=$longitude")

                    Prefs.init().appLanguageCode
                    // Optionally pass location to turnByTurnNavigationFragment via NavController
                }
            }
        }
        setContentView(mBinding.root)
        val navHostController =
            supportFragmentManager.findFragmentById(R.id.authFragmentContainerView) as NavHostFragment
        val isLogout = intent.getBooleanExtra("EXTRA_IS_LOGOUT", false)
        if (isLogout){
            showError(getString(R.string.already_logged_in))
        }
        navController = navHostController.navController
        locationPermission()

    }
    override fun getResources(): Resources {
        val res = super.getResources()
        val config = res.configuration
        if (config.fontScale != 1f) {
            config.fontScale = 1f
            res.updateConfiguration(config, res.displayMetrics)
        }
        return res
    }

    override fun attachBaseContext(newBase: Context) {
        val languageCode = Prefs.init().appLanguageCode ?: "en"
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

    fun showError(text: String){
        alertManager.showError(text)
    }
    fun callSocketConnection(){
        if (!socket.isConnected()) {
            socket.connect()
        }
    }
    fun callSocketDisConnection(){
        if (!socket.isConnected()) {
            socket.disconnect()
        }
    }
    // Location Permission Code
    private fun locationPermission() {
        val fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = android.Manifest.permission.ACCESS_COARSE_LOCATION

        if (ContextCompat.checkSelfPermission(
                this,
                fineLocationPermission
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                coarseLocationPermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(fineLocationPermission, coarseLocationPermission),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions already granted, check location settings
            checkLocationSettings()
        }
    }

    // Check if GPS is enabled
    private fun checkLocationSettings() {
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                // GPS is enabled, start location updates
                startGettingLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // GPS is disabled, prompt user to enable it
                    try {
                        exception.startResolutionForResult(this, LOCATION_SETTINGS_REQUEST_CODE)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e("HomesActivity", "Unable to show GPS dialog", e)
                        callCustomFunctionForLocation()
                    }
                } else {
                    // Other errors (e.g., location unavailable)
                    callCustomFunctionForLocation()
                }
            }
    }

    // Handle permission and GPS settings results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            ) {
                // Permissions granted, check GPS settings
                checkLocationSettings()
            } else {
                // Permissions denied
                callCustomFunctionForLocation()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("recreate" , requestCode.toString())
        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // User enabled GPS, start location updates
                startGettingLocation()
            } else {
                // User declined to enable GPS
                callCustomFunctionForLocation()
            }
        } else if (requestCode == REQUEST_APP_SETTINGS) {
            Log.e("recreate" , " reCreate the Location")
            recreate()
        }
    }

    // Start receiving GPS location updates
    @SuppressLint("MissingPermission") // Permission check handled in locationPermission()
    private fun startGettingLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
                .addOnFailureListener { e ->
                    Log.e("HomesActivity", "Failed to start location updates", e)
                    callCustomFunctionForLocation()
                }
        } else {
            Log.e("HomesActivity", "Location permission not granted")
            callCustomFunctionForLocation()
        }
    }
    // Existing callCustomFunctionForLocation method
    private fun callCustomFunctionForLocation() {
        val builder = AlertDialog.Builder(this)
        val boldText = getString(R.string.location_permissions_title)
        val message = getString(R.string.location_permissions_text)

        builder.setTitle(Html.fromHtml(boldText, Html.FROM_HTML_MODE_LEGACY))
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton(getString(R.string.open_settings)) { _, _ ->
            openAppSettings()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
    // Stop location updates (e.g., when activity is paused or destroyed)
    private fun stopGettingLocation() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, REQUEST_APP_SETTINGS)
    }
    override fun onDestroy() {
        super.onDestroy()
        callSocketDisConnection()
        fusedLocationClient.removeLocationUpdates(locationCallback)

    }
}