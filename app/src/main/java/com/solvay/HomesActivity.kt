package com.solvay

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.mapbox.geojson.Point
import com.solvay.Localization.LocaleHelper
import com.solvay.Network.SocketIOImpl
import com.solvay.UI.Notification.pushNotification.FireMsgService
import com.solvay.UI.settings.viewModel.SettingsViewModel
import com.solvay.Utils.DialogUtils
import com.solvay.Utils.Prefs
import com.solvay.authentication.AuthenticationActivity
import com.solvay.databinding.ActivityHomesBinding

class HomesActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {
    private lateinit var mBinding: ActivityHomesBinding
    lateinit var navController: NavController
    lateinit var navControllerNew: NavController
    private lateinit var socket: SocketIOImpl
    private val mViewModel by viewModels<SettingsViewModel>()
    var isRouteApiCall = false
    var hasPopUpAppeared = false
    var isMapVisible = true
    var isHomeClick = 0
    var styleUpdate = false
    var latitude = 0.0 // Made private, updated via location updates
    private var longitude = 0.0 // Made private, updated via location updates
    var destPoint = Point.fromLngLat(longitude, latitude) // Updated dynamically
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val REQUEST_CODE = 123
    private val LOCATION_SETTINGS_REQUEST_CODE = 1002 // Added for GPS settings check
    private val REQUEST_APP_SETTINGS = 101

    // Add FusedLocationProviderClient for GPS location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hasPopUpAppeared = false

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
                    destPoint = Point.fromLngLat(longitude, latitude) // Update destPoint for Mapbox
                    Log.d("HomesActivity", "Location updated: Lat=$latitude, Lng=$longitude")
                    // Optionally pass location to turnByTurnNavigationFragment via NavController
                }
            }
        }

        // Existing permission check for POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE
                )
            }
        }

        socket = MainApplication.get().socket
        mBinding = ActivityHomesBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        // Existing ViewCompat and NavController setup
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostController =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as NavHostFragment
        val navHostControllerNew =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerViewNew) as NavHostFragment

        navController = navHostController.navController
        navControllerNew = navHostControllerNew.navController

        // Register NavController listener
        navController.addOnDestinationChangedListener(this)
        navControllerNew.addOnDestinationChangedListener(this)

        // Existing click listeners
        isMapView(true)
        mBinding.map.setOnClickListener {
            if (isHomeClick >= 2) {
                logOutDialog()
            }
            isHomeClick++
            isMapView(true)
            updateBottomSheetUi(mBinding.map)
        }
        mBinding.settings.setOnClickListener {
            isHomeClick = 0
            isMapView(false)
            navControllerNew.navigate(R.id.settingsFragment)
            updateBottomSheetUi(mBinding.settings)
        }
        mBinding.chat.setOnClickListener {
            isHomeClick = 0
            isMapView(false)
            navControllerNew.navigate(R.id.chatUserListFragment)
            updateBottomSheetUi(mBinding.chat)
        }
        mBinding.notification.setOnClickListener {
            isHomeClick = 0
            isMapView(false)
            navControllerNew.navigate(R.id.notificationsFragment)
            updateBottomSheetUi(mBinding.notification)
        }

        locationPermission() // Check location permissions
        notificationRedirection(intent)
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

    // Stop location updates (e.g., when activity is paused or destroyed)
    private fun stopGettingLocation() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Existing isMapView method
    fun isMapView(isMap: Boolean) {
        isMapVisible = isMap
        if (isMap) {
            mBinding.apply {
                if (fragmentContainerView2.visibility == View.GONE) fragmentContainerView2.visibility =
                    View.VISIBLE
                if (fragmentContainerViewNew.visibility == View.VISIBLE) fragmentContainerViewNew.visibility =
                    View.GONE
            }
        } else {
            mBinding.apply {
                if (fragmentContainerView2.visibility == View.VISIBLE) fragmentContainerView2.visibility =
                    View.GONE
                if (fragmentContainerViewNew.visibility == View.GONE) fragmentContainerViewNew.visibility =
                    View.VISIBLE
            }
        }
    }

    // Existing setBottomNavigationVisibility method
    fun setBottomNavigationVisibility(visibility: Int) {
        mBinding.bottomNavigation.visibility = visibility
    }

    // Existing getResources method
    override fun getResources(): Resources {
        val res = super.getResources()
        val config = res.configuration
        if (config.fontScale != 1f) {
            config.fontScale = 1f
            res.updateConfiguration(config, res.displayMetrics)
        }
        return res
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

    // Existing openAppSettings method

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, REQUEST_APP_SETTINGS)
    }

    // Lifecycle methods to manage location updates
    override fun onStart() {
        super.onStart()
        if (!socket.isConnected()) {
            socket.connect()
        }
        // Resume location updates if permissions are granted
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationSettings()
        }
    }

    override fun onStop() {
        super.onStop()
        // Stop location updates to save battery
        stopGettingLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
        hasPopUpAppeared = false
        stopGettingLocation()


    }

    // Existing methods (unchanged)
    override fun attachBaseContext(newBase: Context) {
        val languageCode = Prefs.init().appLanguageCode ?: "en"
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.e("HomesActivity", "In onNewIntent")
        if (intent.extras?.getBoolean("fromNotification") == true) {
            Log.e("HomesActivity", "In onNewIntent from notification")
        }
        notificationRedirection(intent)
    }

    fun updateBottomSheetUi(icon: View) {
        mBinding.apply {
            when (icon) {
                map -> {
                    changeImageColor(homeImg, R.color.white)
                    changeImageColor(xChangeImg, R.color.text_color_secondary)
                    changeImageColor(uploadImg, R.color.text_color_secondary)
                    changeImageColor(profileImg, R.color.text_color_secondary)
                }

                settings -> {
                    changeImageColor(homeImg, R.color.text_color_secondary)
                    changeImageColor(xChangeImg, R.color.white)
                    changeImageColor(uploadImg, R.color.text_color_secondary)
                    changeImageColor(profileImg, R.color.text_color_secondary)
                }

                chat -> {
                    changeImageColor(homeImg, R.color.text_color_secondary)
                    changeImageColor(xChangeImg, R.color.text_color_secondary)
                    changeImageColor(uploadImg, R.color.white)
                    changeImageColor(profileImg, R.color.text_color_secondary)
                }

                notification -> {
                    changeImageColor(homeImg, R.color.text_color_secondary)
                    changeImageColor(xChangeImg, R.color.text_color_secondary)
                    changeImageColor(uploadImg, R.color.text_color_secondary)
                    changeImageColor(profileImg, R.color.white)
                }
            }
        }
    }

    private fun notificationRedirection(intent: Intent) {
        val notificationType = intent.extras?.getString(getString(R.string.notification_type))
        when (notificationType) {
            FireMsgService.NOTIFICATION_TYPE_CHAT -> {
                isHomeClick = 0
                isMapView(false)

                val bundle = Bundle()
                bundle.putString(
                    "name",
                    intent.extras?.getString(getString(R.string.notification_sender_name))
                )
                bundle.putString(
                    "_id",
                    intent.extras?.getString(getString(R.string.notification_sender_id))
                )
                bundle.putString(
                    "image",
                    intent.extras?.getString(getString(R.string.notification_sender_image))
                )
                bundle.putBoolean(
                    "isOnline",
                    intent.extras?.getBoolean(getString(R.string.notification_sender_is_online))
                        ?: false
                )
                bundle.putBoolean("pushNot", true)
                navControllerNew.navigate(R.id.chatUserListFragment, bundle)
            }

            FireMsgService.NOTIFICATION_TYPE_POLYGON -> {

            }
        }
    }

    private fun changeImageColor(imageView: ImageView, color: Int) {
        val color = ContextCompat.getColor(this, color)
        imageView.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when (controller.currentDestination?.id) {
            R.id.chatFragment, R.id.aboutUsFragment, R.id.termsAndConditionFragment, R.id.editProfileFragment -> {
                setBottomNavigationVisibility(0)
            }

            R.id.chatUserListFragment, R.id.newChatFragment -> {
                updateBottomSheetUi(mBinding.chat)
                setBottomNavigationVisibility(1)
            }

            R.id.settingsFragment -> {
                updateBottomSheetUi(mBinding.settings)
                setBottomNavigationVisibility(1)
            }

            R.id.notificationsFragment -> {
                updateBottomSheetUi(mBinding.notification)
                setBottomNavigationVisibility(1)
            }

            R.id.turnByTurnNavigationFragment -> {
                updateBottomSheetUi(mBinding.map)
                setBottomNavigationVisibility(1)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isMapVisible) {
            navController.let {
                if (it.currentDestination?.id == R.id.turnByTurnNavigationFragment) {
                    finishAffinity()
                }
            }
        } else {
            navControllerNew.let {
                when (it.currentDestination?.id) {
                    R.id.chatUserListFragment -> {
                        isMapView(true)
                        updateBottomSheetUi(mBinding.map)
                    }

                    R.id.settingsFragment -> {
                        isMapView(true)
                        updateBottomSheetUi(mBinding.map)
                    }

                    R.id.notificationsFragment -> {
                        isMapView(true)
                        updateBottomSheetUi(mBinding.map)
                    }
                }
            }
        }
    }

    private fun logOutDialog() {
        DialogUtils.showCustomDialog(
            context = this,
            titleMsg = getString(R.string.logout),
            textMsg = getString(R.string.are_you_sure_logout),
            imageIcon = null,
            singleButton = false,
            fullscreen = false,
            buttonTextLeft = getString(R.string.cancel),
            buttonTextRight = getString(R.string.logout),
            buttonClickLeft = {},
            buttonClickRight = {
                Prefs.init().isRouteCompleted = true
                mViewModel.logout()
                val intent = Intent(this, AuthenticationActivity::class.java)
                startActivity(intent)
                this.finish()
            }
        )
    }
}
