package com.solvay

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.Value
import com.mapbox.common.location.Location
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.tripdata.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.tripdata.progress.model.DistanceRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.tripdata.progress.model.PercentDistanceTraveledFormatter
import com.mapbox.navigation.tripdata.progress.model.TimeRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.components.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.components.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.voice.api.MapboxSpeechApi
import com.mapbox.navigation.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.voice.model.SpeechAnnouncement
import com.mapbox.navigation.voice.model.SpeechError
import com.mapbox.navigation.voice.model.SpeechValue
import com.mapbox.navigation.voice.model.SpeechVolume
import com.solvay.databinding.FragmentTurnByTurnNavigationBinding

import java.util.Locale
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.internal.route.routeOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.options.RerouteOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.base.trip.model.RouteProgressState
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.directions.session.RoutesExtra

import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.tripdata.maneuver.model.PrimaryManeuver
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.turf.TurfMeasurement
import com.solvay.Base.BaseFragment
import com.solvay.Localization.LocaleHelper
import com.solvay.Network.SocketIOImpl
import com.solvay.UI.settings.dataModels.UpdateLocationRequest
import com.solvay.UI.settings.dataModels.textUpdate
import com.solvay.UI.settings.viewModel.SettingsViewModel
import com.solvay.Utils.AlertManager
import com.solvay.Utils.DialogUtils
import com.solvay.Utils.EventObserver
import com.solvay.Utils.Prefs
import com.solvay.Utils.SlidingPanelHelper
import com.solvay.Utils.Utils.translateText
import com.solvay.Utils.preloadImage
import com.solvay.adapters.StopsAdapter
import com.solvay.authentication.AuthenticationActivity
import com.solvay.authentication.authViewmodel.AuthViewModel
import com.solvay.authentication.models.RouteKeyRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

import java.util.Date
import java.util.Objects
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.floor

class TurnByTurnNavigationFragment : BaseFragment() {
    private lateinit var binding: FragmentTurnByTurnNavigationBinding
    private val viewModel by viewModels<AuthViewModel>()
    private val mViewModel by viewModels<SettingsViewModel>()
    private lateinit var alertManager: AlertManager
    private var routeSource: com.solvay.authentication.models.Location? = null
    private var routeDestination: com.solvay.authentication.models.Location? = null
    private var routeList: ArrayList<com.solvay.authentication.models.Location>? = null
    private var isExpanded = false
    private var profile = DirectionsCriteria.PROFILE_DRIVING_TRAFFIC
    val latitude = 0.0
    val longitude = 0.0
    var startPoint = Point.fromLngLat(longitude, latitude)
    var destPoint = Point.fromLngLat(longitude, latitude)
    var lastDropPoints = Point.fromLngLat(longitude, latitude)
    var originPoint = Point.fromLngLat(longitude, latitude)
    var liveLocation = Point.fromLngLat(longitude, latitude)
    private lateinit var stopsAdapter: StopsAdapter
    private var currentLocationIndex: Int = -1
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private var destinationAnnotation: PointAnnotation? = null
    private lateinit var socket: SocketIOImpl
    private var offRouteDialog: Boolean? = null
    private var isBlueColor = true
    private var isStartReachedCallFirstTime = false
    private var lastSentLocation: Location? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var exitNavigation = false
    var locationUpdateCount = 0
    var lastSentLat: Double? = null
    var lastSentLng: Double? = null
    private val hasTriggered = AtomicBoolean(false)
    private var isCallFirstTime = false
    private var isInitialLocationIsLiveRoutesPoint = false
    private var onClickReached = false
    private var locationSendFirstTime = false
    private var mapStyle = Style.STANDARD
    private var dialogForReachedStartPoint: Dialog? = null
    private var hasTriggeredNew = AtomicBoolean(false)
    private var debounceJob: Job? = null

    private companion object {
        private const val BUTTON_ANIMATION_DURATION = 1500L
        private const val OFF_ROUTE_DISTANCE_THRESHOLD_METERS = 15.0
    }

    lateinit var navController: NavController

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private lateinit var replayProgressObserver: ReplayProgressObserver

    /**
     * Debug object that converts a route into events that can be replayed to navigate a route.
     */
    private val replayRouteMapper = ReplayRouteMapper()

    /**
     * Bindings to the example layout.
     */

    /**
     * Used to execute camera transitions based on the data generated by the [viewportDataSource].
     * This includes transitions from route overview to route following and continuously updating the camera as the location changes.
     */
    private lateinit var navigationCamera: NavigationCamera

    /**
     * Produces the camera frames based on the location and routing data for the [navigationCamera] to execute.
     */
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

    /*
     * Below are generated camera padding values to ensure that the route fits well on screen while
     * other elements are overlaid on top of the map (including instruction view, buttons, etc.)
     */
    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity, 40.0 * pixelDensity, 250.0 * pixelDensity, 40.0 * pixelDensity
        )
    }
    private val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity, 380.0 * pixelDensity, 200.0 * pixelDensity, 20.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            150.0 * pixelDensity, 40.0 * pixelDensity, 250.0 * pixelDensity, 40.0 * pixelDensity
        )
    }

    private val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity, 380.0 * pixelDensity, 200.0 * pixelDensity, 40.0 * pixelDensity
        )
    }


    /**
     * Generates updates for the [MapboxManeuverView] to display the upcoming maneuver instructions
     * and remaining distance to the maneuver point.
     */
    private lateinit var maneuverApi: MapboxManeuverApi

    /**
     * Generates updates for the [MapboxTripProgressView] that include remaining time and distance to the destination.
     */
    private lateinit var tripProgressApi: MapboxTripProgressApi

    /**
     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
     */
    private lateinit var routeLineApi: MapboxRouteLineApi

    /**
     * Draws route lines on the map based on the data from the [routeLineApi]
     */
    private lateinit var routeLineView: MapboxRouteLineView

    /**
     * Generates updates for the [routeArrowView] with the geometries and properties of maneuver arrows that should be drawn on the map.
     */
    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()

    /**
     * Draws maneuver arrows on the map based on the data [routeArrowApi].
     */
    private lateinit var routeArrowView: MapboxRouteArrowView

    /**
     * Stores and updates the state of whether the voice instructions should be played as they come or muted.
     */
    private var isVoiceInstructionsMuted = false
        set(value) {
            field = value
            if (value) {
                binding.soundButton.muteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                binding.soundButton.unmuteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(1f))
            }
        }

    /**
     * Extracts message that should be communicated to the driver about the upcoming maneuver.
     * When possible, downloads a synthesized audio file that can be played back to the driver.
     */
    private lateinit var speechApi: MapboxSpeechApi

    /**
     * Plays the synthesized audio files with upcoming maneuver instructions
     * or uses an on-device Text-To-Speech engine to communicate the message to the driver.
     * NOTE: do not use lazy initialization for this class since it takes some time to initialize
     * the system services required for on-device speech synthesis. With lazy initialization
     * there is a high risk that said services will not be available when the first instruction
     * has to be played. [MapboxVoiceInstructionsPlayer] should be instantiated in
     * `Activity#onCreate`.
     */
    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer

    /**
     * Observes when a new voice instruction should be played.
     */
    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
    }

    /**
     * Based on whether the synthesized audio file is available, the callback plays the file
     * or uses the fall back which is played back using the on-device Text-To-Speech engine.
     */
    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
            expected.fold({ error ->
                // play the instruction via fallback text-to-speech engine
                voiceInstructionsPlayer.play(
                    error.fallback, voiceInstructionsPlayerCallback
                )
            }, { value ->
                // play the sound file from the external generator
                voiceInstructionsPlayer.play(
                    value.announcement, voiceInstructionsPlayerCallback
                )
            })
        }

    /**
     * When a synthesized audio file was downloaded, this callback cleans up the disk after it was played.
     */
    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> { value ->
            // remove already consumed file to free-up space
            speechApi.clean(value)
        }

    /**
     * [NavigationLocationProvider] is a utility class that helps to provide location updates generated by the Navigation SDK
     * to the Maps SDK in order to update the user location indicator on the map.
     */
    private val navigationLocationProvider = NavigationLocationProvider()

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            // update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )

            // update camera position to account for new location
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()
            liveLocation = Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude)
            originPoint = liveLocation
            viewportDataSource.followingCenterPropertyOverride(liveLocation)
//            binding.mapView.mapboxMap.setCamera(
//                CameraOptions.Builder()
//                    .zoom(18.0)
//                    .pitch(70.0)
//                    .center(liveLocation)
//                    .build()
//            )
            //     followingPadding.bottom
            // viewportDataSource.options.followingFrameOptions.defaultPitch = 70.0
            // if this is the first location update the activity has received,
            // it's best to immediately move the camera to the current user location
            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                navigationCamera.requestNavigationCameraToOverview(
                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                        .maxDuration(0) // instant transition
                        .build()
                )
            }

            // 🌟 Send location unconditionally for the first 3 updates
            if (locationUpdateCount < 3) {
                sendLocation(enhancedLocation.latitude, enhancedLocation.longitude)
                lastSentLat = enhancedLocation.latitude
                lastSentLng = enhancedLocation.longitude
                locationUpdateCount++
                Log.e("locationUpdated", "0")
            } else {
                // After first 3 updates, send only if lat/lng changed
                if (enhancedLocation.latitude != lastSentLat || enhancedLocation.longitude != lastSentLng) {
                    sendLocation(enhancedLocation.latitude, enhancedLocation.longitude)
                    lastSentLat = enhancedLocation.latitude
                    lastSentLng = enhancedLocation.longitude


                    Log.e("locationUpdated", "1")
                }

                Log.e("locationUpdated", "2")
            }

            /** Update the user Location here */
            val shouldSendFirstTime = lastSentLocation == null
            val distanceMoved = if (!shouldSendFirstTime) {
                isLocationInMuseum(
                    lastSentLocation!!.latitude,
                    lastSentLocation!!.longitude,
                    enhancedLocation.latitude,
                    enhancedLocation.longitude
                )
            } else {
                0f
            }

            if (shouldSendFirstTime || distanceMoved > 1.0f) {
                lastSentLocation = enhancedLocation
                sendLocation(enhancedLocation.latitude, enhancedLocation.longitude)
            }


            if (startPoint != null && startPoint != Point.fromLngLat(
                    enhancedLocation.longitude, enhancedLocation.latitude
                )
            ) {
                //     binding.llButtons.visibility = View.GONE
            } else {
                //   binding.llButtons.visibility = View.VISIBLE
            }
        }
    }
    private var isOffRoute = false // Track off-route state
    private val offRouteToastHandler =
        Handler(Looper.getMainLooper()) // Handler for periodic toasts
    private val offRouteToastRunnable = object : Runnable {
        override fun run() {
            if (isOffRoute) {
                val baseContext =
                    LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
                alertManager.showError(baseContext.getString(R.string.you_are_out_off_track))

                // Schedule the next toast in 30 seconds
                offRouteToastHandler.postDelayed(this, 30_000)
            }
        }
    }


    val arrivalObserver = object : ArrivalObserver {
        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {

            // Optionally, clear route data from the viewport data source
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }

        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {

        }

        override fun onWaypointArrival(routeProgress: RouteProgress) {

        }
        // ... other callbacks
    }

    /**
     * Gets notified with progress along the currently active route.
     */
//    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
//        // update the camera position to account for the progressed fragment of the route
//        viewportDataSource.onRouteProgressChanged(routeProgress)
//        viewportDataSource.evaluate()
//
//        // draw the upcoming maneuver arrow on the map
//        val style = binding.mapView.mapboxMap.style
//        if (style != null) {
//            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
//            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
//        }
//
//        // update top banner with maneuver instructions
//        val maneuvers = maneuverApi.getManeuvers(routeProgress)
//        maneuvers.fold(
//            { error ->
//                Toast.makeText(
//                    requireContext(),
//                    error.errorMessage,
//                    Toast.LENGTH_SHORT
//                ).show()
//            },
//            {
//                binding.maneuverView.visibility = View.VISIBLE
//                binding.maneuverView.renderManeuvers(maneuvers)
//            }
//        )
//
//        // update bottom trip progress summary
//        binding.tripProgressView.render(
//            tripProgressApi.getTripProgress(routeProgress)
//        )
//
//        val remainingDistance = routeProgress.distanceRemaining
//        if (remainingDistance < 2) { // If less than 10 meters, consider arrived
//            binding.llButtons.visibility = View.VISIBLE
//            binding.btNextLocation.visibility = View.VISIBLE
//          //  if (Point.fromLngLat(routeDestination?.cords?.get(0)?:0.0,routeDestination?.cords?.get(1)?:0.0 )==destPoint){
//            if (startPoint!=destPoint){
//                showOffRouteDialog()
//            }
//
//            Toast.makeText(
//                requireContext(),
//                "You have arrived!",
//                Toast.LENGTH_LONG
//            ).show()
//            mapboxNavigation.stopTripSession() // Stop navigation
//        }
//
//    }
    @RequiresApi(Build.VERSION_CODES.S)
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // Update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        // Draw the upcoming maneuver arrow on the map
        val style = binding.mapView.mapboxMap.style
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        // Update top banner with maneuver instructions
        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold({ error ->

            error.errorMessage?.let { alertManager.showError(it) }!!

        }, { maneuverList ->
            Log.d("Maneuver", "Maneuver list size: ${maneuverList}")

            if (maneuverList.isNotEmpty()) {

                val primaryManeuver = maneuverList.first().primary
                val secondaryManeuver = maneuverList.first().secondary



                Log.d("Maneuver", "Primary instruction: ${primaryManeuver}")

//                            secondaryInstruction?.text = secondaryManeuver?.text ?: ""
//                            secondaryInstruction?.visibility =
                if (secondaryManeuver != null) View.VISIBLE else View.GONE
                Log.d("Maneuver", "Secondary instruction: ${secondaryManeuver?.text}")

                val distance =
                    routeProgress.currentLegProgress?.currentStepProgress?.distanceRemaining ?: 0f
                val totalTime = routeProgress.durationRemaining.toLong() // Time in seconds
                val formattedTotalTime = formatDuration(totalTime)
                org.greenrobot.eventbus.EventBus.getDefault().post(
                    textUpdate(
                        primaryManeuver.text,
                        distance.toString(),
                        formattedTotalTime,
                        primaryManeuver
                    )
                )
//                            distanceText?.text = formatDistance(distance)
                Log.d("Maneuver", "Distance: $distance meters")

//                            if (turnIcon != null) {
//                                setTurnIcon(turnIcon, primaryManeuver)
//                            } else {
//                                Log.e("ManeuverError", "turnIcon is null")
//                            }
            } else {
                //  customManeuverView?.visibility = View.GONE
                Log.d("Maneuver", "No maneuvers available")
            }
//                binding.maneuverView.visibility = View.VISIBLE
            binding.maneuverView.renderManeuvers(maneuvers)
        })

        // Update bottom trip progress summary
        binding.tripProgressView.render(
            tripProgressApi.getTripProgress(routeProgress)
        )
        // Calculate off-route distance

        val routeGeometry = routeProgress.route.geometry()?.let {
            PolylineUtils.decode(it, 5)
        }
        var offRouteDistance = Double.MAX_VALUE
        if (liveLocation != null && routeGeometry != null && routeGeometry.isNotEmpty()) {
            try {
                val closestPoint = TurfMeasurement.along(
                    routeGeometry, routeProgress.distanceTraveled.toDouble(), "meters"
                )
                offRouteDistance = TurfMeasurement.distance(liveLocation, closestPoint, "meters")
                Log.d("OffRoute", "Off-route distance: $offRouteDistance meters")
            } catch (e: Exception) {
                Log.e("OffRoute", "Error calculating off-route distance: ${e.message}")
            }
        } else {
            Log.d(
                "OffRoute",
                "Cannot calculate distance: userLocation=$liveLocation, routeGeometry=$routeGeometry"
            )
        }
        // Handle off-route detection
        val isCurrentlyOffRoute = routeProgress.currentState == RouteProgressState.OFF_ROUTE
        if (isCurrentlyOffRoute && !isOffRoute) {

            isOffRoute = true

            Handler().postDelayed({
                if (offRouteDialog != true) {
                    offRouteDialog = true
                    showOffRouteDialogs()
                }
            }, 1000)
            offRouteToastHandler.postDelayed(offRouteToastRunnable, 30_000)
        } else if (!isCurrentlyOffRoute && isOffRoute) {
            // User is back on route
            isOffRoute = false
            offRouteToastHandler.removeCallbacks(offRouteToastRunnable) // Stop the periodic toast
            val baseContext =
                LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
            // alertManager.showSuccess(baseContext.getString(R.string.you_are_back_your_start))
        }

        // Handle arrival logic
        val remainingDistance = routeProgress.distanceRemaining
        if (remainingDistance < 6) {
            binding.llButtons.visibility = View.VISIBLE
            binding.btNextLocation.visibility = View.VISIBLE
            binding.btNextLocation.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.button_color)
            binding.btNext.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.button_color)
            binding.btNextLocation.isEnabled = true
            binding.btNext.isEnabled = true
//            if (startPoint != destPoint) {
//                showReachedDestinationPointDialog()
//            } else {
//                binding.btNext.performClick()
//                val baseContext =
//                    LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
//                alertManager.showSuccess(baseContext.getString(R.string.you_are_reached_your_start))
//            }

            if (routeList?.getOrNull(0)?.isRescue == true){
                val currentRoute = routeList?.getOrNull(0)
                val imageUrl = currentRoute?.image

                val htmlText = currentRoute?.description
                val baseContext =
                    LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")

                if (binding.clStartPoint.visibility == View.GONE
                ) {
                    binding.apply {
                        if (currentRoute?.title.isNullOrEmpty()) {
                            tvMainTitle.text = baseContext.getString(R.string.reached)
                        } else {
                            tvMainTitle.text = currentRoute?.title
                        }

                        if (imageUrl.isNullOrEmpty()) {
                            binding.ivDialog.visibility = View.VISIBLE
                            binding.cvDialogImage.visibility = View.GONE
                        } else {
                            binding.ivDialog.visibility = View.GONE
                            binding.cvDialogImage.visibility = View.VISIBLE
                            loadImageForStartPoint(imageUrl)
                        }

                        if (htmlText.isNullOrEmpty()) {
                            tvsDescription.visibility = View.VISIBLE
                            tvsDescription.text =
                                baseContext.getString(R.string.dialog_message)
                        } else {
                            updateUsingHtml(htmlText, tvsDescription)
                        }
                    }
                    binding.clStartPoint.visibility = View.VISIBLE


                }

                if (destinationAnnotation != null) {
                    pointAnnotationManager.delete(destinationAnnotation!!)
                    destinationAnnotation = null // Clear reference
                }
            }else{
                if (destPoint == startPoint) {
                    val currentRoute = routeList?.getOrNull(currentLocationIndex - 1)
                    val imageUrl = currentRoute?.image

                    val htmlText = currentRoute?.description
                    val baseContext =
                        LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")

                    if (binding.clStartPoint.visibility == View.GONE
                    ) {
                        binding.apply {
                            if (currentRoute?.title.isNullOrEmpty()) {
                                tvMainTitle.text = baseContext.getString(R.string.reached)
                            } else {
                                tvMainTitle.text = currentRoute?.title
                            }

                            if (imageUrl.isNullOrEmpty()) {
                                binding.ivDialog.visibility = View.VISIBLE
                                binding.cvDialogImage.visibility = View.GONE
                            } else {
                                binding.ivDialog.visibility = View.GONE
                                binding.cvDialogImage.visibility = View.VISIBLE
                                loadImageForStartPoint(imageUrl)
                            }

                            if (htmlText.isNullOrEmpty()) {
                                tvsDescription.visibility = View.VISIBLE
                            } else {
                                updateUsingHtml(htmlText, tvsDescription)
                            }
                        }
                        binding.clStartPoint.visibility = View.VISIBLE


                    }

                    if (destinationAnnotation != null) {
                        pointAnnotationManager.delete(destinationAnnotation!!)
                        destinationAnnotation = null // Clear reference
                    }

                } else if (destPoint == lastDropPoints) {
                    newDestinationReachedPopUp(true)
                } else {
                    newDestinationReachedPopUp(false)
                }
            }





//            val isStartNav = isWithinRadius(
//                originPoint.latitude(),
//                originPoint.longitude(),
//                startPoint.latitude(),
//                startPoint.longitude(),
//                radiusInMeters=15.0
//            )
//            if (!isStartNav) {
//                if (startPoint != destPoint) {
////                    showReachedDestinationPointDialog()
//                    destinationReachedPopUp()
//
//                    alertManager.showSuccess("user reached point 2")
//                }
//            } else {
//                val isMatchWithLastLocation = isWithinRadius(
//                    originPoint.latitude(),
//                    originPoint.longitude(),
//                    routeDestination?.cords?.get(0) ?: 0.0,
//                    routeDestination?.cords?.get(1) ?: 0.0
//                )
//                if (isMatchWithLastLocation && routeList?.get(
//                        routeList?.size?.minus(1) ?: 0
//                    )?.isCurrentLocation == true
//                ) {
//                    alertManager.showSuccess("user reached point 3")
////                    showReachedDestinationPointDialog()
//                    destinationReachedPopUp()
//                } else {
//                    debounceJob?.cancel() // Cancel any previous debounce jobs
//                    alertManager.showSuccess("user reached point 4")
//                    debounceJob = lifecycleScope.launch {
//                        delay(2000) // Debounce: wait to ensure we don't fire multiple times
//
//                        if (binding.clStartPoint.visibility == View.GONE && hasTriggeredNew.compareAndSet(
//                                false, true
//                            )
//                        ) {
//
//                            binding.clStartPoint.visibility = View.VISIBLE
////
//                        }
//                    }
//
//
//
//                    //  nextRouteForStartingPoint()
////                val baseContext =
////                    LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
////                alertManager.showSuccess(baseContext.getString(R.string.you_are_reached_your_start))
//                    //  callNextRouteOnce()
////                Handler(Looper.getMainLooper()).post {
////                    if (!isStartReachedCallFirstTime) {
////                        isStartReachedCallFirstTime = true // Set it before the callback to prevent re-entry
////                        pickUpPointReached()
////                    }
////                }
//
//
//                }
//
//
////                val isStartNav = isWithinRadius(
////                    originPoint.latitude(),
////                    originPoint.longitude(),
////                    destPoint.latitude(),
////                    destPoint.longitude()
////                )
////                if (isStartNav) {
////                    if (startPoint != destPoint) {
////                        showReachedDestinationPointDialog(true)
////                    }else{
////
////                        Handler(Looper.getMainLooper()).post {
////                            if (hasTriggered.compareAndSet(false, true)) {
////                                pickUpPointReached()
////                            }
////                        }
////
////                    }
////                }else{
////                    Handler(Looper.getMainLooper()).post {
////                        if (hasTriggered.compareAndSet(false, true)) {
////                            pickUpPointReached()
////                        }
////                    }
////                }
//
//            }
            binding.mapView.mapboxMap.getStyle { style ->
                clearWalkingRoute(style)
            }
            clearPreviousRoutes()
//            val baseContext =
//                LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
//            alertManager.showSuccess(baseContext.getString(R.string.you_are_arrived))
            mapboxNavigation.stopTripSession() // Stop navigation

        }


    }


//    private fun showReachedDestinationPointDialog() {
//        val dialogView = layoutInflater.inflate(R.layout.dialog_off_route, null)
//        val imageView = dialogView.findViewById<ImageView>(R.id.dialog_gif)
//
//        // Load GIF using Glide
//        Glide.with(requireContext())
//            .asGif()
//            .load(R.drawable.truckparking) // Replace with your GIF file in 'res/drawable'
//            .into(imageView)
//
//        AlertDialog.Builder(requireContext())
//            .setTitle("")
//            .setView(dialogView)
//            .setCancelable(false)
//            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
//                dialog.dismiss()
//                val isStartNav = isWithinRadius(
//                    originPoint.latitude(),
//                    originPoint.longitude(),
//                    routeDestination?.cords?.get(0) ?: 0.0,
//                    routeDestination?.cords?.get(1) ?: 0.0
//                )
//                if (isStartNav) {
//                    logOutDialog(true)
//                }
//             //   binding.btNext.performClick()
//                viewportDataSource.clearRouteData()
//                viewportDataSource.evaluate()
//                binding.mapView.mapboxMap.getStyle { style ->
//                    clearWalkingRoute(style)
//                }
//            }
//            .show()
//    }

    private fun callNextRouteOnce() {
        if (!isCallFirstTime) {
            viewLifecycleOwner.lifecycleScope.launch {
                isCallFirstTime = true
                delay(1500) // 1.5-second delay
                nextRouteForStartingPoint()
            }
        }
    }

    private fun moveNextAutomatically() {
        if (routeList?.getOrNull(currentLocationIndex - 1)?.isAutomated == true) {
            if (routeList.isNullOrEmpty() || currentLocationIndex > (routeList!!.size - 1)) {
                val baseContext = LocaleHelper.setLocale(
                    requireContext(), Prefs.init().appLanguageCode
                )
                logOutDialog(false)
                return
            }

            exitNavigation = true
            clearPreviousRoutes()
            stopsAdapter.nextStop(currentLocationIndex)
            currentLocationIndex++
            destinationTagsUI(stopsAdapter.getExpandedState())


            val originalCords = routeList?.getOrNull(currentLocationIndex - 1)?.cords

            if (originalCords != null && originalCords.size >= 2) {
                val originalLat = originalCords?.get(0) ?: 0.0
                val originalLng = originalCords?.get(1) ?: 0.0
                Log.e("DestinationLog", "Lat: $originalLat, Lng: $originalLng")
                Log.e("RouteListDebugging", "btNext INDEX : ${currentLocationIndex}")
                destPoint = Point.fromLngLat(originalLng, originalLat)
                routeList?.getOrNull(currentLocationIndex - 1)?.type?.let { it1 ->
                    selectMode(
                        it1
                    )
                }

                Log.e(
                    "RouteListDebugging",
                    "type INDEX : ${routeList?.getOrNull(currentLocationIndex - 1)?.type}"
                )
                initNavigation()
                findRoute(destPoint)

            } else {
                val baseContext = LocaleHelper.setLocale(
                    requireContext(), Prefs.init().appLanguageCode ?: "en"
                )
                alertManager.showError(baseContext.getString(R.string.coordinates_not_found))
            }


            if (!routeList.isNullOrEmpty() && currentLocationIndex > routeList!!.size - 1) {
                binding.btNext.text = getString(R.string.complete)
            } else {
                binding.btNext.text = getString(R.string.nextStop)
            }
            Handler(Looper.getMainLooper()).postDelayed({
                binding.recenter.performClick()
            }, 2000)
        } else {
            // Initialize NavigationCamera
            navigationCamera = NavigationCamera(
                binding.mapView.mapboxMap, binding.mapView.camera, viewportDataSource
            )

// When you want to switch to overview mode:
        //    navigationCamera.requestNavigationCameraToOverview()
        }
    }

    private fun destinationReachedPopUp(isShowing: Boolean) {
        binding.apply {
            incDestinationReachedPopoUp.llRoot.visibility = View.VISIBLE
            Glide.with(requireContext()).asGif().load(R.drawable.truckparking)
                .into(incDestinationReachedPopoUp.dialogGif)

            incDestinationReachedPopoUp.dialogOkButton.setOnClickListener {
                incDestinationReachedPopoUp.llRoot.visibility = View.GONE
                val isStartNav = isWithinRadius(
                    originPoint.latitude(),
                    originPoint.longitude(),
                    routeDestination?.cords?.get(0) ?: 0.0,
                    routeDestination?.cords?.get(1) ?: 0.0
                )
//                if (isStartNav) {
//                    logOulogOuttDialog(true)
//                }
                if (isShowing) {
                    logOutDialog(true)
                }
                // binding.btNext.performClick() // Uncomment if needed
                viewportDataSource.clearRouteData()
                viewportDataSource.evaluate()
                binding.mapView.mapboxMap.getStyle { style ->
                    clearWalkingRoute(style)
                }
                if (destinationAnnotation != null) {
                    pointAnnotationManager.delete(destinationAnnotation!!)
                    destinationAnnotation = null // Clear reference
                }
                clearPreviousRoutes()
                moveNextAutomatically()
            }
        }
    }


    private fun newDestinationReachedPopUp(isShowing: Boolean) {
        val context = requireContext()
        val baseContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        val currentRoute = routeList?.getOrNull(currentLocationIndex - 1)
        val imageUrl = currentRoute?.image
        val htmlText = currentRoute?.description
        Log.e("jasgjfasf", currentRoute.toString())

//val description ="Here is the my location Please check your stop"
        binding.apply {

            if (currentRoute?.title.isNullOrEmpty()) {
                newIncReachedPopUp.dialogMessageReached.text =
                    baseContext.getString(R.string.reached)
            } else {
                newIncReachedPopUp.dialogMessageReached.text = currentRoute?.title
            }
            newIncReachedPopUp.ivDialog.visibility = View.GONE
            newIncReachedPopUp.dialogMessageReached.visibility = View.VISIBLE
            if (imageUrl.isNullOrEmpty()) {
                newIncReachedPopUp.dialogGif.visibility = View.GONE
            } else {
                Log.e("FunctiondescriptionimageUrl", imageUrl)
                newIncReachedPopUp.dialogGif.visibility = View.VISIBLE
                loadImage(imageUrl)
            }
            if (htmlText.isNullOrEmpty()) {
                newIncReachedPopUp.dialogMessage.text =
                    baseContext.getString(R.string.dialog_message)
            } else {
                newIncReachedPopUp.dialogMessage.visibility = View.VISIBLE

                updateUsingHtml(htmlText, newIncReachedPopUp.dialogMessage)
            }

            newIncReachedPopUp.llRoot.visibility = View.VISIBLE
            newIncReachedPopUp.dialogOkButton.setOnClickListener {
                newIncReachedPopUp.llRoot.visibility = View.GONE
                if (isShowing) {
                    logOutDialog(true)
                }
                viewportDataSource.clearRouteData()
                viewportDataSource.evaluate()
                binding.mapView.mapboxMap.getStyle { style ->
                    clearWalkingRoute(style)
                }
                if (destinationAnnotation != null) {
                    pointAnnotationManager.delete(destinationAnnotation!!)
                    destinationAnnotation = null // Clear reference
                }
                clearPreviousRoutes()
                moveNextAutomatically()
            }


        }
    }

    @Suppress("DEPRECATION")
    fun updateUsingHtml(htmlString: String?, textView: TextView) {
        if (htmlString.isNullOrEmpty()) {
            textView.text = ""
            return
        }

        // Remove color styles (e.g., style="color: rgb(255, 255, 255);")
        val cleanedHtml = htmlString.replace(Regex("style=\"[^\"]*color:[^;]*;?\""), "")

        // Wrap HTML with default styling
        val wrappedHtml = """
        <div style="text-align: center;">
            <span style="font-family: sans-serif; font-size: 16px; color: gray; font-weight: bold;">
                $cleanedHtml
            </span>
        </div>
    """.trimIndent()

        try {
            val spanned: Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(cleanedHtml, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(cleanedHtml)
            }

            // Always update the view on the main thread
            if (Looper.myLooper() == Looper.getMainLooper()) {
                textView.text = spanned
            } else {
                Handler(Looper.getMainLooper()).post {
                    textView.text = spanned
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            textView.text = htmlString
        }
    }


    private suspend fun translateDialogText(message: String): String {
        if (message.isEmpty()) {
            return message
        }
        return try {
            translateText(message, Prefs.init().appLanguageCode)
        } catch (e: Exception) {
            Log.e("Translation0101", "Translation failed: ${e.message}")
            message // Fallback to original message
        }
    }

    private fun loadImage(url: String) {
        try {
            if (url.lowercase().endsWith(".gif")) {
                Glide.with(requireContext())
                    .asGif()
                    .load(BuildConfig.IMAGE_URL + url)
                    .into(binding.newIncReachedPopUp.dialogGif)
            } else {
                // Load as regular image (JPG, PNG, etc.)
                Glide.with(requireContext())
                    .load(BuildConfig.IMAGE_URL + url)
                    .into(binding.newIncReachedPopUp.dialogGif)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback loading (as image in case of failure)
            Glide.with(requireContext())
                .load(BuildConfig.IMAGE_URL + url)
                .into(binding.newIncReachedPopUp.dialogGif)
        }
    }

    private fun loadImageForStartPoint(url: String) {

        Log.e("asfhfgf", url.toString())
        try {
            if (url.lowercase().endsWith(".gif")) {
                Glide.with(requireContext())
                    .asGif()
                    .load(BuildConfig.IMAGE_URL + url)
                    .into(binding.startPointImage)
            } else {
                // Load as regular image (JPG, PNG, etc.)
                Glide.with(requireContext())
                    .load(BuildConfig.IMAGE_URL + url)
                    .into(binding.startPointImage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback loading (as image in case of failure)
            Glide.with(requireContext())
                .load(BuildConfig.IMAGE_URL + url)
                .into(binding.startPointImage)
        }
    }


    private fun showReachedDestinationPointDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_off_route, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.dialog_gif)
        val okButton = dialogView.findViewById<TextView>(R.id.dialog_ok_button)

        // Load GIF using Glide
        Glide.with(requireContext()).asGif()
            .load(R.drawable.truckparking) // Replace with your GIF file in 'res/drawable'
            .into(imageView)

        val dialog = Dialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Transparent background for rounded card

        // Handle OK button click
        okButton.setOnClickListener {
            dialog.dismiss()
            val isStartNav = isWithinRadius(
                originPoint.latitude(),
                originPoint.longitude(),
                routeDestination?.cords?.get(0) ?: 0.0,
                routeDestination?.cords?.get(1) ?: 0.0
            )
            if (isStartNav) {
                logOutDialog(true)
            }
            // binding.btNext.performClick() // Uncomment if needed
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
            binding.mapView.mapboxMap.getStyle { style ->
                clearWalkingRoute(style)
            }
            clearPreviousRoutes()
        }

        dialog.show()
    }

    fun takeMeToStartLocation() {
        val dialogView = layoutInflater.inflate(R.layout.takeme_start_location_lyout, null)
        val takeMeButton = dialogView.findViewById<TextView>(R.id.btRegister)

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        dialog.show()

        takeMeButton.setOnClickListener {
            Log.e("lastDestinationPoint", destPoint.toString())
            findRoute(destPoint)
            dialog.dismiss()

        }
    }

    private fun setupBottomSheet() {
//        val bottomSheet = binding.bottomSheet
//        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
//        bottomSheetBehavior.isHideable = false
//
//        bottomSheet.post {
//            val straightLine = binding.straightLine
//            val params = straightLine.layoutParams as ViewGroup.MarginLayoutParams
//
//            val collapsedHeight = straightLine.height
//
//            val maxHeight = (Resources.getSystem().displayMetrics.heightPixels * 0.6).toInt()
//
//            with(bottomSheetBehavior) {
//                peekHeight = collapsedHeight
//                this.maxHeight = maxHeight
//                state = BottomSheetBehavior.STATE_COLLAPSED
//            }
//
//        }
//
//        bottomSheetBehavior.addBottomSheetCallback(object :
//            BottomSheetBehavior.BottomSheetCallback() {
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
//                    binding.rvStops.visibility = View.GONE
//                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
//                    binding.rvStops.visibility = View.VISIBLE
//                }
//            }
//
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//
//                if (binding.rvStops.visibility == View.GONE) {
//                    binding.rvStops.visibility = View.VISIBLE
//                }
//
//                binding.llStops.apply {
//                    alpha = slideOffset.coerceIn(0f, 1f)
//                }
//
//
//            }
//        })


        val slidingPanelHelper = SlidingPanelHelper(
            view = binding.bottomSheet,
            initialMinHeight = resources.getDimensionPixelSize(R.dimen.collapsed_height),
            initialMaxHeight = resources.getDimensionPixelSize(R.dimen.expanded_height)
        )

        binding.straightLine.setOnClickListener {
            slidingPanelHelper.toggle()
        }

    }


    private fun logOutDialog(isStartNav: Boolean? = null) {
        DialogUtils.showCustomDialog(
            context = requireContext(),
            titleMsg = getString(R.string.logout),
            textMsg = getString(R.string.are_you_sure_logout),
            imageIcon = R.drawable.ic_attention,
            singleButton = false,
            fullscreen = false,
            buttonTextLeft = getString(R.string.logout),
            buttonTextRight = getString(R.string.close),
            buttonClickLeft = {
                Prefs.init().isRouteCompleted = true
                if (isStartNav == true) {
                    viewModel.completeRoute()
                }
                if (isInternetAvailable(requireContext())){
                    mViewModel.logout()
                }else{
                    val baseContext = LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
                    alertManager.showError(baseContext.getString(R.string.no_internet_message))
                }
//                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
//                startActivity(intent)
//                requireActivity().finish()
            },
            buttonClickRight = {})
    }

    /**
     * Gets notified whenever the tracked routes change.
     *
     * A change can mean:
     * - routes get changed with [MapboxNavigation.setNavigationRoutes]
     * - routes annotations get refreshed (for example, congestion annotation that indicate the live traffic along the route)
     * - driver got off route and a reroute was executed
     */
//    private val routesObserver = RoutesObserver { routeUpdateResult ->
//        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
//            // generate route geometries asynchronously and render them
//            routeLineApi.setNavigationRoutes(
//                routeUpdateResult.navigationRoutes
//            ) { value ->
//                binding.mapView.mapboxMap.style?.apply {
//                    routeLineView.renderRouteDrawData(this, value)
//                }
//            }
//
//            // update the camera position to account for the new route
//            viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
//            viewportDataSource.evaluate()
//        } else {
//            // remove the route line and route arrow from the map
//            val style = binding.mapView.mapboxMap.style
//            if (style != null) {
//                routeLineApi.clearRouteLine { value ->
//                    routeLineView.renderClearRouteLineValue(
//                        style,
//                        value
//                    )
//                }
//                routeArrowView.render(style, routeArrowApi.clearArrows())
//            }
//
//            // remove the route reference from camera position evaluations
//            viewportDataSource.clearRouteData()
//            viewportDataSource.evaluate()
//        }
//    }


    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
            // Generate route geometries asynchronously and render them
            // Generate route geometries asynchronously and render them
            routeLineApi.setNavigationRoutes(
                routeUpdateResult.navigationRoutes
            ) { value ->
                binding.mapView.mapboxMap.style?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                    Log.d(
                        "TurnByTurnNavigation",
                        "Route line rendered for layers: mapbox-navigation-route-line"
                    )

                    // Confirm layer presence
                    val routeLayer = getLayer("mapbox-navigation-route-line")
                    if (routeLayer != null) {
                        Log.d(
                            "TurnByTurnNavigation",
                            "Confirmed: mapbox-navigation-route-line layer exists"
                        )
                        if (isAdded && context != null) {
//                            Toast.makeText(
//                                requireContext(),
//                                "Reroute line drawn",
//                                Toast.LENGTH_SHORT
//                            ).show()
                        }
                    }

                    // Check for walking route layer if applicable
                    if (profile == DirectionsCriteria.PROFILE_WALKING) {

                        clearWalkingRoute(this)
                        Log.d("TurnByTurnNavigation", "Cleared existing walking route layers")

                        // Draw new dashed walking route
                        drawDashedWalkingRoute(routeUpdateResult.navigationRoutes.first(), this)
                        val walkingLayer = getLayer("walking-route-layer")
                        if (walkingLayer != null) {

                            Log.d("TurnByTurnNavigation", "Confirmed: walking-route-layer exists")
                        }
                    }

                    // Check for dotted line layer
                    val dottedLayer = getLayer("dotted-line-layer")
                    if (dottedLayer != null) {
                        Log.d("TurnByTurnNavigation", "Confirmed: dotted-line-layer exists")
                    }
                }
            }

            // Update the camera position to account for the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
            viewportDataSource.evaluate()

            // Check if this is a reroute
            if (routeUpdateResult.reason == RoutesExtra.ROUTES_UPDATE_REASON_REROUTE && isAdded && context != null) {
                // Delay the alert slightly to avoid overlap with off-route dialog
                binding.mapView.mapboxMap.getStyle { style ->
                    clearWalkingRoute(style)
                }

            }
        } else {
            // Remove the route line and route arrow from the map
            val style = binding.mapView.mapboxMap.style
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(style, value)
                }
                clearWalkingRoute(style)
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }

            // Remove the route reference from camera position evaluations
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @RequiresApi(Build.VERSION_CODES.S)
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.registerArrivalObserver(arrivalObserver)
                mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)

                // Remove the replayProgressObserver since we won't use replay simulation
                // replayProgressObserver = ReplayProgressObserver(mapboxNavigation.mapboxReplayer)
                // mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)

                // Start a real trip session to receive live location updates
                mapboxNavigation.startTripSession(true)
            }

            @RequiresApi(Build.VERSION_CODES.S)
            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
                mapboxNavigation.registerArrivalObserver(arrivalObserver)
                mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)


                // mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
                // mapboxNavigation.mapboxReplayer.finish()
            }
        }, onInitialize = this::initNavigation
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        binding = FragmentTurnByTurnNavigationBinding.inflate(layoutInflater)
    }


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = FragmentTurnByTurnNavigationBinding.inflate(inflater, container, false)
//
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val baseContext =
            LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
        val themedContext = ContextThemeWrapper(baseContext, R.style.AppTheme)
        val localizedInflater = inflater.cloneInContext(themedContext)
        retainInstance = true
        // Inflate the layout for this fragment
        binding = FragmentTurnByTurnNavigationBinding.inflate(localizedInflater, container, false)
        alertManager = AlertManager(binding.root as ViewGroup, requireContext())
        socket = MainApplication.get().socket
        initNavigation()
        if (::binding.isInitialized) {
            showNavBottom()
        }

        if (socket.isConnected()) {
            if (!locationSendFirstTime) {
                sendLocation(originPoint.latitude(), originPoint.longitude())
                locationSendFirstTime = true
            }
        }
        callApi()
        observer()
        binding.apply {
            setupBottomSheet()
            incDestinationReachedPopoUp.llRoot.visibility = View.GONE
            pointAnnotationManager = binding.mapView.annotations.createPointAnnotationManager()
            stopsAdapter = StopsAdapter(requireContext(), arrayListOf())
            rvStops.apply {
                layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                adapter = stopsAdapter
                setHasFixedSize(true)
            }

            tvsDescription.movementMethod = android.text.method.ScrollingMovementMethod()
            newIncReachedPopUp.dialogMessage.movementMethod = android.text.method.ScrollingMovementMethod()

            binding.btDialog.setOnClickListener {
                binding.clStartPoint.visibility = View.GONE
                Log.e(
                    "currentLocationIndexSTartPoint",
                    routeList?.getOrNull(currentLocationIndex - 1)?.isAutomated.toString()
                )
                if (routeList?.getOrNull(0)?.isRescue == true){
                    logOutDialog(true)
                }else{
                    Log.e("currentLocationIndexSTartPoint", currentLocationIndex.toString())
                    if (routeList?.getOrNull(currentLocationIndex - 1)?.isAutomated == true) {
                        binding.btNext.performClick()
                    }
                }



            }


            /*   closeDestination.setOnClickListener {
                   coordinatorLayout.visibility = View.GONE
               }*/

//            motionLayout.setOnTouchListener { _, event ->
//                if (event.action == MotionEvent.ACTION_UP) {
//                    if (motionLayout.progress > 0.5f) {
//                        destinationTagsUI(true)
//                        motionLayout.transitionToEnd()
//                    } else {
//                        destinationTagsUI(false)
//                        motionLayout.transitionToStart()
//                    }
//                }
//                false
//            }


            binding.btNext.setOnClickListener {
                navigationCamera.requestNavigationCameraToIdle()
             //   navigationCamera.requestNavigationCameraToFollowing()
                Log.d("RouteListDebugging", "btNext LIST: ${routeList}")
                Log.d("RouteListDebugging", "btNext LIST SIZE: ${routeList?.size}")
                if (routeList?.getOrNull(0)?.isRescue == true){
                    logOutDialog(true)
                }else{
                    if (routeList.isNullOrEmpty() || currentLocationIndex > (routeList!!.size - 1)) {
                        val baseContext = LocaleHelper.setLocale(
                            requireContext(), Prefs.init().appLanguageCode ?: "en"
                        )
                        // viewModel.completeRoute()
                        logOutDialog(true)
                        return@setOnClickListener
                    }

                    exitNavigation = true
                    clearPreviousRoutes()
                    stopsAdapter.nextStop(currentLocationIndex - 1)
                    currentLocationIndex++
                    destinationTagsUI(stopsAdapter.getExpandedState())

                    val cords = routeList?.getOrNull(currentLocationIndex)?.cords
                    val originalCords = routeList?.getOrNull(currentLocationIndex - 1)?.cords

                    if (originalCords != null && originalCords.size >= 2) {
//                    val desLat = cords[0]
//                    val desLng = cords[1]
                        val originalLat = originalCords?.get(0) ?: 0.0
                        val originalLng = originalCords?.get(1) ?: 0.0
                        Log.e("DestinationLog", "Lat: $originalLat, Lng: $originalLng")
                        Log.e("RouteListDebugging", "btNext INDEX : ${currentLocationIndex}")
                        destPoint = Point.fromLngLat(originalLng, originalLat)
                        routeList?.getOrNull(currentLocationIndex - 1)?.type?.let { it1 ->
                            selectMode(
                                it1
                            )
                        }

                        Log.e(
                            "RouteListDebugging",
                            "type INDEX : ${routeList?.getOrNull(currentLocationIndex - 1)?.type}"
                        )
                        initNavigation()
                        findRoute(destPoint)

                        val newIconBitmap =
                            BitmapFactory.decodeResource(resources, R.drawable.detination_map_icon)
                        //    updateDestinationMarker(destPoint, newIconBitmap)
                    } else {
                        val baseContext = LocaleHelper.setLocale(
                            requireContext(), Prefs.init().appLanguageCode ?: "en"
                        )
                        alertManager.showError(baseContext.getString(R.string.coordinates_not_found))
                    }


                    // ✅ Now update the button label based on updated index
                    if (!routeList.isNullOrEmpty() && currentLocationIndex > routeList!!.size - 1) {
                        binding.btNext.text = getString(R.string.complete)
                    } else {
                        binding.btNext.text = getString(R.string.nextStop)
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.recenter.performClick()
                    }, 2000)
                    Log.e(
                        "currentLocationIndexValue",
                        "btNextLocation INDEX : ${currentLocationIndex.toString()}"
                    )
                }




            //    newDestinationReachedPopUp(false)
            }



            binding.btNextLocation.setOnClickListener {
                hasTriggeredNew = AtomicBoolean(false)
                navigationCamera.requestNavigationCameraToIdle()
             //   navigationCamera.requestNavigationCameraToFollowing()
                Log.e(
                    "RouteListDebugging",
                    "btNextLocation INDEX : ${currentLocationIndex.toString()}"
                )
                Log.d("RouteListDebugging", "btNextLocation LIST: ${routeList}")
                Log.d("RouteListDebugging", "btNextLocation LIST SIZE: ${routeList?.size}")
                if (isInternetAvailable(requireContext())) {
                    customManeuver.visibility = View.VISIBLE
                    btBackMainMap.visibility = View.VISIBLE
                    btBackDummyView.visibility = View.VISIBLE
                    llButtonMainMap.visibility = View.GONE
                    clRoutes.visibility = View.VISIBLE
                    exitNavigation = true

                    if (routeList.isNullOrEmpty() || currentLocationIndex >= (routeList!!.size - 1)) {
                        val baseContext = LocaleHelper.setLocale(
                            requireContext(), Prefs.init().appLanguageCode ?: "en"
                        )
                        alertManager.showError(baseContext.getString(R.string.cant_move_next))

                        return@setOnClickListener
                    }

                    clearPreviousRoutes()
                    currentLocationIndex++
                    destinationTagsUI(stopsAdapter.getExpandedState())

                    val cords = routeList?.getOrNull(currentLocationIndex)?.cords


                    val originalCords = routeList?.getOrNull(currentLocationIndex - 1)?.cords
                    if (routeList?.getOrNull(0)?.isRescue == true){
                        destPoint = Point.fromLngLat(startPoint.longitude(), startPoint.latitude())
                        initNavigation()
                        findRoute(destPoint)
                        binding.btNext.text = getString(R.string.complete)
                    }else{
                        if (cords != null && cords.size >= 2) {
                            var originalLat = originalCords?.get(0) ?: 0.0
                            var originalLng = originalCords?.get(1) ?: 0.0
                            Log.e("Destination", "Lat: $originalLat, Lng: $originalLng")
                            val isStartNav = isWithinRadiusForRoutes(
                                originPoint.latitude(),
                                originPoint.longitude(),
                                originalLat,
                                originalLng
                            )


                            Log.e(
                                "Destination",
                                "Lat: ${routeList?.getOrNull(currentLocationIndex)?.type}, Lng: ${
                                    routeList?.getOrNull(currentLocationIndex)?.type
                                }"
                            )
                            if (isStartNav) {
                                stopsAdapter.nextStop(currentLocationIndex - 1)
                                originalLat = cords?.get(0) ?: 0.0
                                originalLng = cords?.get(1) ?: 0.0
                                destPoint = Point.fromLngLat(originalLng, originalLat)
                                currentLocationIndex++
                                isInitialLocationIsLiveRoutesPoint = true
                                routeList?.getOrNull(1)?.type?.let { it1 ->
                                    selectMode(
                                        it1
                                    )
                                }
                            } else {
                                routeList?.getOrNull(0)?.type?.let { it1 ->
                                    selectMode(
                                        it1
                                    )
                                }
                                destPoint = Point.fromLngLat(originalLng, originalLat)
                                isInitialLocationIsLiveRoutesPoint = false
                            }


                            initNavigation()
                            findRoute(destPoint)
                            Log.e(
                                "DestinationPointValue", "Lat: ${destPoint}, Lng: ${
                                    routeList?.getOrNull(currentLocationIndex)?.type
                                }"
                            )
                            //       findRoute(Point.fromLngLat(11.753672, 51.798840))
                            // Load the new icon
                            val newIconBitmap =
                                BitmapFactory.decodeResource(resources, R.drawable.detination_map_icon)
                            //   updateDestinationMarker(destPoint, newIconBitmap)
                        } else {
                            val baseContext = LocaleHelper.setLocale(
                                requireContext(), Prefs.init().appLanguageCode ?: "en"
                            )
                            alertManager.showError(baseContext.getString(R.string.coordinates_not_found))
                        }
                    }


                    binding.btNextLocation.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.grey_hintColor)
//                    binding.btNext.backgroundTintList =
//                        ContextCompat.getColorStateList(requireContext(), R.color.grey_hintColor)
                    binding.btNextLocation.isEnabled = false
                    //       binding.btNext.isEnabled = false
                    Handler(Looper.myLooper()!!).postDelayed({
                        binding.recenter.performClick()
                    }, 3000)

                } else {
                    val baseContext = LocaleHelper.setLocale(
                        requireContext(), Prefs.init().appLanguageCode ?: "en"
                    )
                    alertManager.showError(
                        baseContext.getString(R.string.check_internet_connection)
                    )
                }
                Log.e(
                    "currentLocationIndexValue",
                    "btNextLocation INDEX : ${currentLocationIndex.toString()}"
                )



                    val currentRoute = routeList?.getOrNull(currentLocationIndex - 1)
                    val imageUrl = currentRoute?.image
//                if(currentRoute?.imageBitmap!=null){
//                    if (imageUrl.isNullOrEmpty()) {
//                        binding.ivDialog.visibility = View.VISIBLE
//                        binding.cvDialogImage.visibility = View.GONE
//                    } else {
//                        binding.ivDialog.visibility = View.GONE
//                        binding.cvDialogImage.visibility = View.VISIBLE
//                        loadImageForStartPoint(imageUrl)
//                    }
//                }else{
//                    if (imageUrl.isNullOrEmpty()) {
//                        binding.ivDialog.visibility = View.VISIBLE
//                        binding.cvDialogImage.visibility = View.GONE
//                    } else {
//                        binding.ivDialog.visibility = View.GONE
//                        binding.cvDialogImage.visibility = View.VISIBLE
//                        loadImageForStartPoint(imageUrl)
//                    }
//                }





//                    val htmlText = currentRoute?.description
//                    val baseContext =
//                        LocaleHelper.setLocale(requireContext(), Prefs.init().appLanguageCode ?: "en")
//
//                    if (binding.clStartPoint.visibility == View.GONE
//                    ) {
//                        binding.apply {
//                            if (currentRoute?.title.isNullOrEmpty()) {
//                                tvMainTitle.text = baseContext.getString(R.string.reached)
//                            } else {
//                                tvMainTitle.text = currentRoute?.title
//                            }
//
//                            if (imageUrl.isNullOrEmpty()) {
//                                binding.ivDialog.visibility = View.VISIBLE
//                                binding.cvDialogImage.visibility = View.GONE
//                            } else {
//                                binding.ivDialog.visibility = View.GONE
//                                binding.cvDialogImage.visibility = View.VISIBLE
//                                loadImageForStartPoint(imageUrl)
//                            }
//
//                            if (htmlText.isNullOrEmpty()) {
//                                tvsDescription.visibility = View.VISIBLE
//                            } else {
//                                updateUsingHtml(htmlText, tvsDescription)
//                            }
//                        }
//                        binding.clStartPoint.visibility = View.VISIBLE
//
//                    }
//
//                    if (destinationAnnotation != null) {
//                        pointAnnotationManager.delete(destinationAnnotation!!)
//                        destinationAnnotation = null // Clear reference
//                    }

            }

//            btBack.setOnClickListener {
//                customManeuver.visibility=View.GONE
//                btBackMainMap.visibility=View.GONE
//                btBackDummyView.visibility=View.GONE
//                clRoutes.visibility=View.GONE
//                llButtonMainMap.visibility = View.VISIBLE
//                currentLocationIndex--
////                binding.btNextLocation.text = getString(R.string.start_nav)
//                binding.btNextLocation.backgroundTintList =
//                    ContextCompat.getColorStateList(requireContext(), R.color.button_color)
//                binding.btNextLocation.isEnabled = true
//                clearPreviousRoutes()
//                binding.mapView.mapboxMap.getStyle { style ->
//                    clearWalkingRoute(style)
//                    clearDottedLine(style)
//                    clearRouteAndStopNavigation()
//                }
//            }


            btBack.setOnClickListener {

                Log.e("currentLocationIndexValue", currentLocationIndex.toString())
                val startIndex = if (isInitialLocationIsLiveRoutesPoint) {
                    2
                    //When pickUp == LiveLocation
                } else {
                    1
                    //When pickUp != LiveLocation
                }
                if (currentLocationIndex == startIndex || currentLocationIndex == 0) {
                    val baseContext = LocaleHelper.setLocale(
                        requireContext(), Prefs.init().appLanguageCode ?: "en"
                    )
                    customManeuver.visibility = View.GONE
                    btBackMainMap.visibility = View.GONE
                    btBackDummyView.visibility = View.GONE
                    clRoutes.visibility = View.GONE
                    llButtonMainMap.visibility = View.VISIBLE
                    currentLocationIndex = 0
                    isInitialLocationIsLiveRoutesPoint = false
                    binding.btNextLocation.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.button_color)
                    binding.btNextLocation.isEnabled = true
                    clearPreviousRoutes()
                    binding.mapView.mapboxMap.getStyle { style ->
                        clearWalkingRoute(style)
                        clearDottedLine(style)
                        clearRouteAndStopNavigation()
                    }

                    // Initialize NavigationCamera
                    navigationCamera = NavigationCamera(
                        binding.mapView.mapboxMap, binding.mapView.camera, viewportDataSource
                    )

// When you want to switch to overview mode:
                //    navigationCamera.requestNavigationCameraToOverview()
                    //     return@setOnClickListener
                } else {
                    clearPreviousRoutes()
                    stopsAdapter.moveBack(currentLocationIndex - 1)
                    currentLocationIndex--
                    destinationTagsUI(stopsAdapter.getExpandedState())

                    val cords = routeList?.getOrNull(currentLocationIndex)?.cords
                    val originalCords = routeList?.getOrNull(currentLocationIndex - 1)?.cords

                    if (originalCords != null && originalCords.size >= 2) {
//                        val desLat = cords[0]
//                        val desLng = cords[1]
                        var originalLat = originalCords?.get(0) ?: 0.0
                        var originalLng = originalCords?.get(1) ?: 0.0
                        //   Log.e("Destination", "Lat: $desLat, Lng: $desLng")
                        val isStartNav = isWithinRadiusForRoutes(
                            originPoint.latitude(),
                            originPoint.longitude(),
                            originalLat,
                            originalLng
                        )
                        if (isStartNav) {
                            originalLat = cords?.get(0) ?: 0.0
                            originalLng = cords?.get(1) ?: 0.0
                            destPoint = Point.fromLngLat(originalLng, originalLat)
                            currentLocationIndex--
                        } else {
                            destPoint = Point.fromLngLat(originalLng, originalLat)
                        }

                        routeList?.getOrNull(currentLocationIndex - 1)?.type?.let { it1 ->
                            selectMode(
                                it1
                            )
                        }
                        initNavigation()
                        findRoute(destPoint)
                        // Load the new icon
                        if (startPoint == destPoint) {
                            val newIconBitmap = BitmapFactory.decodeResource(
                                resources, R.drawable.detination_map_icon
                            )
                            updateDestinationMarker(destPoint, newIconBitmap)
                        } else {
                            val newIconBitmap =
                                BitmapFactory.decodeResource(resources, R.drawable.detination_icon)
                            updateDestinationMarker(destPoint, newIconBitmap)
                        }


                    } else {
                        val baseContext = LocaleHelper.setLocale(
                            requireContext(), Prefs.init().appLanguageCode ?: "en"
                        )
                        alertManager.showError(baseContext.getString(R.string.coordinates_not_found))
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.recenter.performClick()
                    }, 1000)
                }
                if (!routeList.isNullOrEmpty() && currentLocationIndex < routeList!!.size - 1) {
                    binding.btNext.text = getString(R.string.nextStop)
                } else {
                    binding.btNext.text = getString(R.string.complete)
                }

                hasTriggered.set(false)
                onClickReached = false
            }

            btBackMainMap.setOnClickListener {
                btBackMainMap.visibility = View.GONE
                btBackDummyView.visibility = View.GONE
                currentLocationIndex--
                binding.btNextLocation.text = getString(R.string.start_nav)
                binding.btNextLocation.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.button_color)
                binding.btNextLocation.isEnabled = true
                clearPreviousRoutes()
                binding.mapView.mapboxMap.getStyle { style ->
                    clearWalkingRoute(style)
                    clearDottedLine(style)
                    clearRouteAndStopNavigation()
                }
            }
        }


        // initialize Navigation Camera
        viewportDataSource = MapboxNavigationViewportDataSource(binding.mapView.mapboxMap)

        // Set initial zoom and pitch
        viewportDataSource.followingZoomPropertyOverride(18.0)
        viewportDataSource.options.followingFrameOptions.defaultPitch = 70.0
        // viewportDataSource.followingCenterPropertyOverride(liveLocation)
        viewportDataSource.options.followingFrameOptions.pitchNearManeuvers.enabled = false
        //     viewportDataSource.options.followingFrameOptions.paddingUpdatesAllowed=false
        //    viewportDataSource.options.followingFrameOptions.zoomUpdatesAllowed=true
        viewportDataSource.options.followingFrameOptions.bearingSmoothing.maxBearingAngleDiff = 0.0
        viewportDataSource.options.followingFrameOptions.bearingSmoothing.enabled = false
//        viewportDataSource.options.followingFrameOptions.maximizeViewableGeometryWhenPitchZero = true
        navigationCamera = NavigationCamera(
            binding.mapView.mapboxMap, binding.mapView.camera, viewportDataSource
        )

        // set the animations lifecycle listener to ensure the NavigationCamera stops
        // automatically following the user location when the map is interacted with
        binding.mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )

        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
            // shows/hide the recenter button depending on the camera state
            Log.d("Navigation", "Camera state changed to: $navigationCameraState")
            when (navigationCameraState) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING, NavigationCameraState.FOLLOWING -> binding.recenter.visibility =
                    View.VISIBLE

                NavigationCameraState.TRANSITION_TO_OVERVIEW, NavigationCameraState.OVERVIEW, NavigationCameraState.IDLE -> binding.recenter.visibility =
                    View.VISIBLE
            }
        }
        // set the padding values depending on screen orientation and visible view layout
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.overviewPadding = landscapeOverviewPadding
            viewportDataSource.followingPadding = landscapeFollowingPadding
        } else {
            viewportDataSource.overviewPadding = overviewPadding
            viewportDataSource.followingPadding = followingPadding
        }
        // make sure to use the same DistanceFormatterOptions across different features
        val distanceFormatterOptions = DistanceFormatterOptions.Builder(requireContext()).build()

        // initialize maneuver api that feeds the data to the top banner maneuver view
        maneuverApi = MapboxManeuverApi(
            MapboxDistanceFormatter(distanceFormatterOptions)
        )

        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(requireContext()).distanceRemainingFormatter(
                DistanceRemainingFormatter(distanceFormatterOptions)
            ).timeRemainingFormatter(
                TimeRemainingFormatter(requireContext())
            ).percentRouteTraveledFormatter(
                PercentDistanceTraveledFormatter()
            ).estimatedTimeToArrivalFormatter(
                EstimatedTimeToArrivalFormatter(requireContext(), TimeFormat.NONE_SPECIFIED)
            ).build()
        )

        // initialize voice instructions api and the voice instruction player
        speechApi = MapboxSpeechApi(
            requireContext(), Locale.US.language
        )
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
            requireContext(), Locale.US.language
        )

        // initialize route line, the routeLineBelowLayerId is specified to place
        // the route line below road labels layer on the map
        // the value of this option will depend on the style that you are using
        // and under which layer the route line should be placed on the map layers stack
        val routeLineColorResources = RouteLineColorResources.Builder()
            .routeLineTraveledColor(Color.TRANSPARENT) // Make the traveled part invisible
            .build()
        val mapboxRouteLineViewOptions = MapboxRouteLineViewOptions.Builder(requireContext())
            .routeLineBelowLayerId("road-label-navigation").displayRestrictedRoadSections(false)
            .routeLineColorResources(routeLineColorResources).build()

        routeLineApi = MapboxRouteLineApi(
            MapboxRouteLineApiOptions.Builder().calculateRestrictedRoadSections(false)
                .vanishingRouteLineEnabled(true).build()
        )
        routeLineView = MapboxRouteLineView(mapboxRouteLineViewOptions)


        val onPositionChangedListener = OnIndicatorPositionChangedListener { point ->
            val result = routeLineApi.updateTraveledRouteLine(point)
            binding.mapView.mapboxMap.style?.let { routeLineView.renderRouteLineUpdate(it, result) }
        }

        binding.mapView.location.addOnIndicatorPositionChangedListener(onPositionChangedListener)

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(requireContext()).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)
        val customDuskStyleUri = "mapbox://styles/your-username/your-style-id"
        // load map style

        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.detination_map_icon)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 70, 75, false)
//        if ((activity as HomesActivity).styleUpdate){
//            mapStyle =Style.STANDARD_SATELLITE
//        }else{
//            mapStyle =Style.STANDARD
//        }
        binding.mapView.mapboxMap.loadStyle(mapStyle) {
            it.addImage(
                "destination-icon", scaledBitmap
            )
            it.setStyleImportConfigProperty("standard", "lightPreset", Value.valueOf("dusk"))

            // Ensure route line layers are initialized
            routeLineView.initializeLayers(it)


            // Disable Mapbox UI plugins
            binding.mapView.logo.enabled = false // Disable LogoPlugin
            binding.mapView.scalebar.enabled = false // Disable ScaleBarPlugin
            binding.mapView.compass.enabled = false // Disable CompassPlugin
            binding.mapView.location.enabled = false
            binding.mapView.attribution.enabled = false
            binding.mapView.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(), R.color.black
                )
            )

            //  binding.mapView.attribution.visibility = View.GONE
        }

        binding.btStreetView.setOnClickListener {
            (activity as HomesActivity).styleUpdate = !(activity as HomesActivity).styleUpdate
            if ((activity as HomesActivity).styleUpdate) {
                mapStyle = Style.STANDARD_SATELLITE
            } else {
                mapStyle = Style.STANDARD
            }

            binding.mapView.mapboxMap.loadStyle(mapStyle) {
                it.addImage(
                    "destination-icon", scaledBitmap
                )
                it.setStyleImportConfigProperty(
                    "standard", "lightPreset", Value.valueOf("dusk")
                )
            }
//            parentFragmentManager.beginTransaction().apply {
//                detach(this@TurnByTurnNavigationFragment)
//                attach(this@TurnByTurnNavigationFragment)
//                commit()
//            }

        }

        // initialize view interactions
        binding.stop.setOnClickListener {
            clearRouteAndStopNavigation()
        }


//        binding.recenter.setOnClickListener {
//            navigationCamera.requestNavigationCameraToFollowing()
//            binding.routeOverview.showTextAndExtend(TurnByTurnExperienceActivity.BUTTON_ANIMATION_DURATION)
//            binding.llDestination.visibility = View.GONE
//          //  viewportDataSource.followingZoomPropertyOverride(18.0)
//            viewportDataSource.overviewPadding = followingPadding
//            viewportDataSource.options.followingFrameOptions.defaultPitch = 70.0
//            viewportDataSource.evaluate()
//          //  followingPadding.bottom
//            binding.routeOverview.showTextAndExtend(TurnByTurnExperienceActivity.BUTTON_ANIMATION_DURATION)
//        }


        binding.recenter.setOnClickListener {
            viewportDataSource.clearFollowingOverrides()
            viewportDataSource.clearOverviewOverrides()
            // Set your overrides here
            viewportDataSource.followingZoomPropertyOverride(18.0)
            viewportDataSource.options.followingFrameOptions.defaultPitch = 70.0
         //   viewportDataSource.followingCenterPropertyOverride(liveLocation)
            viewportDataSource.options.followingFrameOptions.pitchNearManeuvers.enabled = false
            viewportDataSource.options.followingFrameOptions.bearingSmoothing.maxBearingAngleDiff = 0.0
            viewportDataSource.options.followingFrameOptions.bearingSmoothing.enabled = false
            // Evaluate after setting overrides
            viewportDataSource.evaluate()
            // Move camera to IDLE to cancel any ongoing transitions
          //  navigationCamera.requestNavigationCameraToIdle()

            // Now request following state
            navigationCamera.requestNavigationCameraToFollowing()
            Log.d(
                "Navigation",
                "Recentered - Zoom: ${binding.mapView.mapboxMap.cameraState.zoom}, Pitch: ${binding.mapView.mapboxMap.cameraState.pitch}"
            )
        }


//        binding.routeOverview.setOnClickListener {
//            navigationCamera.requestNavigationCameraToOverview()
//            binding.recenter.showTextAndExtend(BUTTON_ANIMATION_DURATION)
//        }
        binding.soundButton.setOnClickListener {
            // mute/unmute voice instructions
            isVoiceInstructionsMuted = !isVoiceInstructionsMuted
        }
        // set initial sounds button state
        binding.soundButton.unmute()


//        binding.clShowRoute.setOnClickListener {
//            if (binding.coordinatorLayout.visibility == View.VISIBLE) {
//                binding.coordinatorLayout.visibility = View.GONE
//                //     binding.btShowRoute.setText(R.string.show_route)
//            } else {
//                binding.coordinatorLayout.visibility = View.VISIBLE
//                //   binding.btShowRoute.setText(R.string.hide_route)
//            }
//        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        maneuverApi.cancel()
        routeLineApi.cancel()
        routeLineView.cancel()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
        // Clean up the off-route toast handler
        offRouteToastHandler.removeCallbacks(offRouteToastRunnable)
    }


    private fun showOffRouteDialogs() {
//        if (offRouteDialog == true){
//            return
//        }else{
        DialogUtils.showCustomDialog(
            context = requireContext(),
            titleMsg = getString(R.string.off_route),
            textMsg = getString(R.string.move_off_the_route),
            imageIcon = R.drawable.ic_attention,
            singleButton = true,
            //  fullscreen = false,
            buttonText = getString(R.string.ok),
            buttonClickLeft = {},
            buttonClick = {
                offRouteDialog = false
            })
        offRouteDialog = true
//        }
    }


    private fun clearPreviousRoutes() {
        routeLineApi.cancel()
        routeLineView.cancel()
        mapboxNavigation.stopTripSession()
        mapboxNavigation.setNavigationRoutes(emptyList())


        binding.mapView.mapboxMap.getStyle { style ->
            // Remove all route-related layers
            listOf(
                "mapbox-navigation-route-line",
                "mapbox-navigation-alternative-route-line",
                "mapbox-navigation-route-casing-layer",
                "walking-route-layer",
                "driving-route-layer"
            ).forEach { layerId ->
                style.getLayer(layerId)?.let { style.removeStyleLayer(layerId) }
            }

            // Remove all route-related sources
            listOf(
                "mapbox-navigation-route-source", "walking-route-source", "driving-route-source"
            ).forEach { sourceId ->
                style.getSource(sourceId)?.let { style.removeStyleSource(sourceId) }
            }

            // ✅ Clear route line view explicitly
            //   routeLineView.hideRouteLines(style) { }

            // ✅ Force map refresh to ensure UI updates
            binding.mapView.mapboxMap.triggerRepaint()
        }
        viewportDataSource.followingZoomPropertyOverride(15.0)
        viewportDataSource.options.followingFrameOptions.defaultPitch = 0.0
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(requireContext())

                .rerouteOptions(
                    RerouteOptions.Builder()
                        //    .enableReroute(false) // Disable automatic rerouting
                        .build()
                ).build()
        )

        // initialize location puck
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            this.locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.from(
                    R.drawable.puck_icon
                )
            )
            puckBearingEnabled = true
            enabled = true
        }

        replayOriginLocation()
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun replayOriginLocation() {
        with(mapboxNavigation.mapboxReplayer) {
            play()
            pushEvents(
                listOf(
                    ReplayRouteMapper.mapToUpdateLocation(
                        Date().time.toDouble(),
                        Point.fromLngLat(-122.39726512303575, 37.785128345296805)
                    )
                )
            )
            playFirstLocation()
        }
    }

    private fun findRoute(destination: Point) {
        binding.contentLoadingProgressBar.visibility = View.VISIBLE
        val originLocation = navigationLocationProvider.lastLocation ?: return
        originPoint = Point.fromLngLat(originLocation.longitude, originLocation.latitude)

        binding.customManeuver.visibility = View.VISIBLE
        (activity as HomesActivity).isRouteApiCall = true
        viewModel.updateLastLocation(UpdateLocationRequest(index = currentLocationIndex-1))
        mapboxNavigation.requestRoutes(
            RouteOptions.builder().applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(requireContext())
                .coordinatesList(listOf(originPoint, destination))
                .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC).annotationsList(
                    listOf(
                        DirectionsCriteria.ANNOTATION_CONGESTION_NUMERIC,
                        DirectionsCriteria.ANNOTATION_DISTANCE
                    )
                ).apply {
                    originLocation.bearing?.let { bearing ->
                        bearingsList(
                            listOf(
                                Bearing.builder().angle(bearing).degrees(45.0).build(), null
                            )
                        )
                    }
                }.layersList(listOf(mapboxNavigation.getZLevel(), null)).build(),
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {
                    // no impl
                    binding.contentLoadingProgressBar.visibility = View.GONE
                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
                    // no impl
                    binding.contentLoadingProgressBar.visibility = View.GONE
                }

                override fun onRoutesReady(
                    routes: List<NavigationRoute>, routerOrigin: String
                ) {
                    binding.contentLoadingProgressBar.visibility = View.GONE

                    if (routes.isNotEmpty()) {
                        if (profile == DirectionsCriteria.PROFILE_WALKING) {
                            binding.mapView.mapboxMap.getStyle { style ->
                                clearWalkingRoute(style)
                                clearDottedLine(style)
                                drawDottedLineToDestination(routes[0], destination, style, "dotted")
                                drawDashedWalkingRoute(routes[0], style) // Call the method here
                                setRouteAndStartNavigation(routes)
                            }
                        } else {
                            binding.mapView.mapboxMap.getStyle { style ->
                                clearWalkingRoute(style)
                                clearDottedLine(style)
                                drawDottedLineToDestination(routes[0], destination, style, "dotted")
                                // drawDashedWalkingRoute(routes[0], style) // Call the method here
                            }
                            val destinationPoint = routes[0].routeOptions.coordinatesList().last()
                            // addFlagMarker(destinationPoint)
                            setRouteAndStartNavigation(routes)
                        }
                    }


                }


            })

        updateDestinationMarker(destination)
    }


    private fun drawDottedLineToDestination(
        route: NavigationRoute, destination: Point, style: Style, lineType: String = "dotted"
    ) {
        val sourceId = "dotted-line-source"
        val layerId = "dotted-line-layer"

        val routeGeometry = route.directionsRoute.geometry() ?: return
        val lineString = LineString.fromPolyline(routeGeometry, 6)
        val coordinates = lineString.coordinates()
        if (coordinates.isEmpty()) return
        val lastRoutePoint = coordinates.last()

        val dottedLineString = LineString.fromLngLats(listOf(lastRoutePoint, destination))
        val featureCollection =
            FeatureCollection.fromFeature(Feature.fromGeometry(dottedLineString))

        val existingSource = style.getSource(sourceId) as? GeoJsonSource
        if (existingSource != null) {
            existingSource.featureCollection(featureCollection)
        } else {
            val routeSource = geoJsonSource(sourceId) {
                featureCollection(featureCollection)
            }
            style.addSource(routeSource)
        }

        val dashArray = when (lineType) {
            "dotted" -> listOf(0.5, 2.0)
            "dashed" -> listOf(2.0, 2.0)
            "solid" -> null
            else -> listOf(0.5, 2.0)
        }

        val existingLayer = style.getLayer(layerId) as? LineLayer
        if (existingLayer != null) {
            existingLayer.lineColor(Color.parseColor("#2196F3"))
            if (dashArray != null) {
                existingLayer.lineDasharray(dashArray)
            } else {
                // existingLayer.lineDasharray(null)
            }
        } else {
            val dottedLineLayer = LineLayer(layerId, sourceId).apply {
                lineColor(Color.parseColor("#2196F3"))
                lineWidth(4.0)
                if (dashArray != null) {
                    lineDasharray(dashArray)
                }
                lineCap(LineCap.ROUND)
                lineJoin(LineJoin.ROUND)
            }
            style.addLayer(dottedLineLayer)
        }
    }

    private fun clearDottedLine(style: Style) {
        val sourceId = "dotted-line-source"
        val layerId = "dotted-line-layer"
        style.getSource(sourceId)?.let { style.removeStyleSource(sourceId) }
        style.getLayer(layerId)?.let { style.removeStyleLayer(layerId) }
    }

    private fun setRouteAndStartNavigation(routes: List<NavigationRoute>) {
        // set routes, where the first route in the list is the primary route that
        // will be used for active guidance
        mapboxNavigation.setNavigationRoutes(routes)

        // show UI elements
        binding.soundButton.visibility = View.VISIBLE
        // binding.routeOverview.visibility = View.GONE
        binding.tripProgressCard.visibility = View.VISIBLE

        // move the camera to overview when new route is available
        //   navigationCamera.requestNavigationCameraToOverview()

        // start simulation
        // startSimulation(routes.first().directionsRoute)
    }

    private fun clearRouteAndStopNavigation() {
        // clear
        mapboxNavigation.setNavigationRoutes(listOf())
        destinationAnnotation?.let { pointAnnotationManager.delete(it) }
        destinationAnnotation = null
        // stop simulation
        stopSimulation()

        // hide UI elements
        binding.soundButton.visibility = View.VISIBLE
        binding.maneuverView.visibility = View.INVISIBLE
        //    binding.routeOverview.visibility = View.INVISIBLE
        binding.tripProgressCard.visibility = View.INVISIBLE
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun startSimulation(route: DirectionsRoute) {
        mapboxNavigation.mapboxReplayer.stop()
        mapboxNavigation.mapboxReplayer.clearEvents()
        val replayData = replayRouteMapper.mapDirectionsRouteGeometry(route)
        mapboxNavigation.mapboxReplayer.pushEvents(replayData)
        mapboxNavigation.mapboxReplayer.seekTo(replayData[0])
        mapboxNavigation.mapboxReplayer.play()
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun stopSimulation() {
        mapboxNavigation.mapboxReplayer.stop()
        mapboxNavigation.mapboxReplayer.clearEvents()
    }


    private fun callApi() {
        val request = RouteKeyRequest(
            route = null, checkRoute = true,
         //   location = listOf(liveLocation.latitude() ?: 0.0, liveLocation.longitude() ?: 0.0)
        )
        viewModel.getRouteKey(request)
    }

    private fun observer() {
        viewModel.routeData.observe(viewLifecycleOwner) {
            if (it.success == true) {
                val route = it.data?.route
                initNavigation()

                route?.apply {
                    routeSource = pickUp
                    routeDestination = drop
                    val stops = ArrayList(route.stops ?: arrayListOf())
                    if (routeList == null) {
                        routeList = arrayListOf()
                    } else {
                        routeList?.clear()
                    }
                    routeList?.apply {
                        routeSource?.isCurrentLocation = true
                        routeDestination?.isLastLocation = true
                        add(routeSource ?: com.solvay.authentication.models.Location())
                        if (stops.isNotEmpty()) addAll(stops)
                        add(routeDestination ?: com.solvay.authentication.models.Location())
                    }
                    if (routeList != null) {
                        for (i in routeList ?: arrayListOf()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                i.title = translateDialogText(i.title ?: "")
                                i.description = translateDialogText(i.description ?: "")
                            }
                            preloadImage(requireContext(), i, {
                                Log.e("preloadImage", "imageISLoad")
                            })
                        }

                    }
                    currentLocationIndex = 0
                    Log.d("RouteListDebugging", "LIST: ${routeList}")
                    Log.d("RouteListDebugging", "LIST SIZE: ${routeList?.size}")
                    Log.d("RouteListDebugging", "SOURCE: ${routeSource}")
                    Log.d("RouteListDebugging", "DESTINATION: ${routeDestination}")
                    Log.d("RouteListDebugging", "STOPS: ${stops}")
                    Log.d("RouteListDebugging", "STOPS SIZE: ${stops.size}")
                    startPoint =
                        Point.fromLngLat(pickUp?.cords?.get(1) ?: 0.0, pickUp?.cords?.get(0) ?: 0.0)

//                    lastDropPoints =
//                        Point.fromLngLat(drop?.cords?.get(1) ?: 0.0, drop?.cords?.get(0) ?: 0.0)


                    val cords = drop?.cords
                    lastDropPoints = if (cords != null && cords.size >= 2) {
                        Point.fromLngLat(cords[1], cords[0])
                    } else {
                        // Provide a fallback or handle the error
                        Log.e("DropError", "Invalid coordinates: $cords")
                        Point.fromLngLat(0.0, 0.0) // or handle gracefully
                    }
                    val calculateDistanceInMeters = isLocationInMuseum(
                        startPoint.latitude(),
                        startPoint.longitude(),
                        originPoint.latitude(),
                        originPoint.longitude()
                    )


                    Log.e("fjskfjkf", calculateDistanceInMeters.toString())

                    val isStartNav = isWithinRadius(
                        originPoint.latitude(),
                        originPoint.longitude(),
                        startPoint.latitude(),
                        startPoint.longitude()
                    )
                    Log.e("fjskfjkf", isStartNav.toString())
                    if (!isStartNav) {
                        routeDestination?.type?.let { it1 -> selectMode(it1) }

                        destPoint = Point.fromLngLat(
                            pickUp?.cords?.get(1) ?: 0.0, pickUp?.cords?.get(0) ?: 0.0
                        )
//                        if ((activity as HomesActivity).hasPopUpAppeared == false) {
//                          //  takeMeToStartLocation()
//                            (activity as HomesActivity).hasPopUpAppeared = true
//                        } else {
//                            findRoute(destPoint)
//                        }
                        //   findRoute(destPoint)
                        binding.llButtons.visibility = View.VISIBLE
                        binding.btNextLocation.visibility = View.VISIBLE
                    } else {
                        val newIconBitmap =
                            BitmapFactory.decodeResource(resources, R.drawable.detination_map_icon)
                        updateDestinationMarker(destPoint, newIconBitmap)
                        binding.llButtons.visibility = View.VISIBLE
                        binding.btNextLocation.visibility = View.VISIBLE
                    }


                }
                destinationTagsUI(true)
            } else {
                alertManager.showError(it.message ?: "")
            }
        }


        mViewModel.logoutResponse.observe(viewLifecycleOwner, EventObserver{
            Log.e("hjfgdsjf", "jfhjhfjhj")
            if (it.success==true){
                val intent = Intent(requireActivity(), AuthenticationActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }else{
                it.message?.let { it1 -> alertManager.showError(it1) }
            }

        })
    }

    fun calculateDistanceInMeters(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Float {
        val startLocation = android.location.Location("").apply {
            latitude = lat1
            longitude = lon1
        }

        val endLocation = android.location.Location("").apply {
            latitude = lat2
            longitude = lon2
        }

        return startLocation.distanceTo(endLocation) // Returns distance in meters
    }

    private fun isWithinRadius(
        currentLat: Double,
        currentLng: Double,
        startLat: Double,
        startLng: Double,
        radiusInMeters: Double = 30.0
    ): Boolean {
        val distance = calculateDistanceInMeters(currentLat, currentLng, startLat, startLng)
        Log.e("ditanceInMeeter", distance.toString())
        return distance <= radiusInMeters
    }

    private fun isWithinRadiusForRoutes(
        currentLat: Double,
        currentLng: Double,
        startLat: Double,
        startLng: Double,
        radiusInMeters: Double = 2.0
    ): Boolean {
        val distance = calculateDistanceInMeters(currentLat, currentLng, startLat, startLng)
        Log.e("ditanceInMeeter", distance.toString())
        return distance <= radiusInMeters
    }

    private fun destinationTagsUI(isExpanded: Boolean) {
        binding.llStops.visibility = View.VISIBLE

        Log.d("StopsList", "Before update, list size: ${routeList?.size}")

        val list = getStopsList(isExpanded)

        Log.d("StopsList", "After update, list size: ${routeList?.size}")
        stopsAdapter.setExpandedState(isExpanded)
        stopsAdapter.updateList(list)
    }

    private fun getStopsList(isExpanded: Boolean): ArrayList<com.solvay.authentication.models.Location> {
        val stops = arrayListOf<com.solvay.authentication.models.Location>()

        if (routeList.isNullOrEmpty()) {
            return stops
        }

        if (isExpanded) {
            return routeList ?: arrayListOf()
        } else {
            if (currentLocationIndex in 0 until (routeList?.size ?: 0)) {
                routeList?.get(currentLocationIndex)?.isCurrentLocation = true

                // Add current location
                stops.add(
                    routeList?.get(currentLocationIndex)
                        ?: com.solvay.authentication.models.Location()
                )

                // Add next location if available
                if (currentLocationIndex + 1 < (routeList?.size ?: 0)) {
                    stops.add(
                        routeList?.get(currentLocationIndex + 1)
                            ?: com.solvay.authentication.models.Location()
                    )
                }
            }
            return stops
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
        org.greenrobot.eventbus.EventBus.getDefault().register(this)
//        if (!socket.isConnected()) {
//            socket.connect()
//        }
    }

    override fun onPause() {
        super.onPause()
        (activity as HomesActivity).destPoint = destPoint
        //originPoint = liveLocation
    }

//    override fun onStop() {
//        super.onStop()
//        (activity as HomesActivity).destPoint = destPoint
//    }
//    override fun onResume() {
//        super.onResume()
//        Handler(Looper.myLooper()!!).postDelayed({
//            findRoute((activity as HomesActivity).destPoint)
////            binding.mapView.mapboxMap.setCamera(
////                CameraOptions.Builder()
////                    .zoom(18.0)
////                    .pitch(70.0)
////                    .center(liveLocation)
////                    .build()
////            )
//        }, 3000)
//        Log.e("Djskhdsajkdsa", (activity as HomesActivity).destPoint.toString())
//
//    }

    private fun sendLocation(lat: Double, long: Double) {
        val locationArray = JSONArray().apply {
            put(lat)
            put(long)
        }
        // Create the JSON object { location: [long, lat] }
        val obj = JSONObject().apply {
            put("location", locationArray)
        }

        socket.emitMessage("updateLocation", obj) { _, _ ->
            Log.d("Socket", "Location update sent: $obj")
        }
    }

    private fun addFlagMarker(point: Point) {
        // Remove any existing flag marker
        destinationAnnotation?.let { pointAnnotationManager.delete(it) }

        // Load and scale the flag icon
        val flagBitmap = BitmapFactory.decodeResource(resources, R.drawable.green_location)
        val scaledFlagBitmap = Bitmap.createScaledBitmap(flagBitmap, 80, 85, false)

        // Add the flag icon image to the style
        binding.mapView.mapboxMap.getStyle { style ->
            style.addImage("flag-icon", scaledFlagBitmap)
        }

        // Create a PointAnnotation for the flag
        val pointAnnotationOptions =
            PointAnnotationOptions().withPoint(point).withIconImage("flag-icon").withIconSize(1.0)
                .withIconOffset(listOf(0.0, -15.0))

        // Add the annotation to the map
        destinationAnnotation = pointAnnotationManager.create(pointAnnotationOptions)
    }

    private fun location(destination: Point, iconBitmap: Bitmap? = null) {
        // Remove the previous destination marker if it exists
        destinationAnnotation?.let { pointAnnotationManager.delete(it) }

        // Prepare the icon image
        val bitmap =
            iconBitmap ?: BitmapFactory.decodeResource(resources, R.drawable.detination_map_icon)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 70, 75, false)

        // Add the new icon image to the style
        binding.mapView.mapboxMap.getStyle { style ->
            style.addImage("destination-icon", scaledBitmap)
        }

        // Create a new PointAnnotation for the destination
        val pointAnnotationOptions =
            PointAnnotationOptions().withPoint(destination).withIconImage("destination-icon")
                .withIconSize(1.0).withIconOffset(listOf(0.0, -10.0))

        // Add the annotation to the map
        destinationAnnotation = pointAnnotationManager.create(pointAnnotationOptions)
    }

    private fun updateDestinationMarker(destination: Point, iconBitmap: Bitmap? = null) {
        // Remove the previous destination marker if it exists
        destinationAnnotation?.let { pointAnnotationManager.delete(it) }

        // Prepare the icon image
        val bitmap =
            iconBitmap ?: BitmapFactory.decodeResource(resources, R.drawable.detination_map_icon)
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 70, 75, false)

        // Add the new icon image to the style
        binding.mapView.mapboxMap.getStyle { style ->
            style.addImage("destination-icon", scaledBitmap)
        }

        // Create a new PointAnnotation for the destination
        val pointAnnotationOptions =
            PointAnnotationOptions().withPoint(destination).withIconImage("destination-icon")
                .withIconSize(1.0).withIconOffset(listOf(0.0, -10.0))

        // Add the annotation to the map
        destinationAnnotation = pointAnnotationManager.create(pointAnnotationOptions)
    }

    private fun drawDashedWalkingRoute(route: NavigationRoute, style: Style) {
        val sourceId = "walking-route-source"
        val layerId = "walking-route-layer"

        // Convert the route geometry to a GeoJSON feature.
        val routeGeometry = route.directionsRoute.geometry() ?: return
        val lineString = LineString.fromPolyline(routeGeometry, 6)
        val featureCollection = FeatureCollection.fromFeature(Feature.fromGeometry(lineString))

        // Check if the source exists and update it; otherwise, create and add a new source.
        val existingSource = style.getSource(sourceId) as? GeoJsonSource
        if (existingSource != null) {
            existingSource.featureCollection(featureCollection)
        } else {
            val routeSource = geoJsonSource(sourceId) {
                featureCollection(featureCollection)
            }
            style.addSource(routeSource)
        }

        // Toggle color logic
        val newColor = if (isBlueColor) "#4CAF50" else "#2196F3" // Green and Blue

        // Check if the layer exists
        val existingLayer = style.getLayer(layerId) as? LineLayer
        if (existingLayer != null) {
            // If the layer exists, toggle its color
            existingLayer.lineColor(Color.parseColor("#2196F3"))
        } else {
            // Create the dashed line layer
            val dashedLineLayer = LineLayer(layerId, sourceId).apply {
                lineColor(Color.parseColor("#2196F3"))
                lineWidth(7.0)
                lineDasharray(listOf(0.1, 2.0)) // Dash pattern
                lineCap(LineCap.ROUND)
                lineJoin(LineJoin.ROUND)
            }
            style.addLayer(dashedLineLayer)
        }

        // Flip the toggle state
        isBlueColor = !isBlueColor
    }

    fun selectMode(type: String) {
        if (type == "Walking") {
            profile = DirectionsCriteria.PROFILE_WALKING
        } else {
            profile = DirectionsCriteria.PROFILE_DRIVING_TRAFFIC
        }

    }

    private fun clearWalkingRoute(style: Style) {
        val sourceId = "walking-route-source"
        val layerId = "walking-route-layer"

        // Check & remove existing source
        style.getSource(sourceId)?.let { style.removeStyleSource(sourceId) }

        // Check & remove existing layer
        style.getLayer(layerId)?.let { style.removeStyleLayer(layerId) }
    }

    fun isLocationInMuseum(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val distance = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, distance)
        return distance[0]
    }

    private fun setTurnIcon(imageView: ImageView, maneuver: PrimaryManeuver) {
        // Map maneuver type to drawable resource
        Log.e("jhfhsdjfk", maneuver.type.toString())
        val drawableRes = when (maneuver.type) {
            "turn" -> when (maneuver.modifier) {
                "left" -> R.drawable.left_turn
                "right" -> R.drawable.right_turn
                "straight" -> R.drawable.straight_line
                else -> R.drawable.straight_line
            }

            "roundabout" -> R.drawable.u_turn
            "arrive" -> R.drawable.straight_line
            else -> R.drawable.straight_line
        }
        imageView.setImageResource(drawableRes)
        // Apply turn icon style programmatically
//        imageView.setColorFilter(Color.parseColor("#FFEB3B")) // Yellow tint
//        imageView.setBackgroundColor(Color.parseColor("#7A6E03")) // Shadow color
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return when {
            hours > 0 -> "${hours} hr ${minutes} min"
            minutes > 0 -> "${minutes} min"
            else -> "${seconds} sec"
        }
    }

    override fun onAttach(context: Context) {
        val newContext = LocaleHelper.setLocale(context, Prefs.init().appLanguageCode ?: "en")
        super.onAttach(newContext)
    }

    override fun onStop() {
        super.onStop()
        org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateImageApi(event: textUpdate?) {
        Log.d("FragmentLifecycleCheck", "$event")
        val roundedDistance = event?.distance?.toDouble()?.let { floor(it).toInt() }
        binding.tvDistance.text = roundedDistance.toString() + " " + getString(R.string.meters)
        binding.tvTime.text = event?.time?.toString()
        binding.tvDestinationName.text = event?.primaryText.toString()
        event?.primaryManeuver?.let { setTurnIcon(binding.ivTurnColor, it) }
    }


    fun nextRouteForStartingPoint() {
        if (isInternetAvailable(requireContext())) {
            binding.apply {
                customManeuver.visibility = View.VISIBLE
                btBackMainMap.visibility = View.VISIBLE
                btBackDummyView.visibility = View.VISIBLE
                //  binding.btNextLocation.text = getString(R.string.nextStop)
                llButtonMainMap.visibility = View.GONE
                clRoutes.visibility = View.VISIBLE
            }

            exitNavigation = true

            if (routeList.isNullOrEmpty() || currentLocationIndex >= (routeList!!.size - 1)) {
                val baseContext = LocaleHelper.setLocale(
                    requireContext(), Prefs.init().appLanguageCode ?: "en"
                )
                alertManager.showError(baseContext.getString(R.string.cant_move_next))

                return
            }
            clearPreviousRoutes()
            stopsAdapter.nextStop(currentLocationIndex)
            currentLocationIndex++
            destinationTagsUI(stopsAdapter.getExpandedState())

            val cords = routeList?.getOrNull(currentLocationIndex)?.cords
            val originalCords = routeList?.getOrNull(currentLocationIndex - 1)?.cords

            if (cords != null && cords.size >= 2) {
//                        val desLat = cords[0]
//                        val desLng = cords[1]
                val originalLat = originalCords?.get(0) ?: 0.0
                val originalLng = originalCords?.get(1) ?: 0.0
                Log.e("Destination", "Lat: $originalLat, Lng: $originalLng")

                destPoint = Point.fromLngLat(originalLng, originalLat)
                routeList?.getOrNull(currentLocationIndex)?.type?.let { it1 ->
                    selectMode(
                        it1
                    )
                }

                initNavigation()
                findRoute(destPoint)

                // Load the new icon
                val newIconBitmap =
                    BitmapFactory.decodeResource(resources, R.drawable.detination_map_icon)
                updateDestinationMarker(destPoint, newIconBitmap)
            } else {
                val baseContext = LocaleHelper.setLocale(
                    requireContext(), Prefs.init().appLanguageCode ?: "en"
                )
                alertManager.showError(baseContext.getString(R.string.coordinates_not_found))
            }
            binding.btNextLocation.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.grey_hintColor)
//                    binding.btNext.backgroundTintList =
//                        ContextCompat.getColorStateList(requireContext(), R.color.grey_hintColor)
            binding.btNextLocation.isEnabled = false
            //       binding.btNext.isEnabled = false
            Handler(Looper.myLooper()!!).postDelayed({
                binding.recenter.performClick()
            }, 2000)

        } else {
            val baseContext = LocaleHelper.setLocale(
                requireContext(), Prefs.init().appLanguageCode ?: "en"
            )
            alertManager.showError(
                baseContext.getString(R.string.check_internet_connection)
            )
        }
    }

    fun pickUpPointReached() {
        DialogUtils.showCustomDialog(
            context = requireContext(),
            titleMsg = getString(R.string.alert),
            textMsg = getString(R.string.you_are_reached_your_start),
            imageIcon = R.drawable.des_reached,
            singleButton = true,
            fullscreen = false,
            buttonText = getString(R.string.ok),
            buttonClick = {
                if (!onClickReached) {
                    onClickReached = true
                    binding.btNext.performClick()
                }

            }

        )
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun customDialogAlert() {
        // ✅ Don't show again if already shown
        if (dialogForReachedStartPoint != null && dialogForReachedStartPoint?.isShowing == true) return
        dialogForReachedStartPoint?.dismiss()
        dialogForReachedStartPoint = null
        lifecycleScope.launch {
            delay(1000)
            dialogForReachedStartPoint?.dismiss()
            dialogForReachedStartPoint = null
            dialogForReachedStartPoint = Dialog(requireActivity())
            dialogForReachedStartPoint?.requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            dialogForReachedStartPoint?.setCancelable(true)
            dialogForReachedStartPoint?.setContentView(R.layout.layout_dialog)
            dialogForReachedStartPoint?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogForReachedStartPoint?.window?.decorView?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN

            val llButtons: LinearLayout = dialogForReachedStartPoint!!.findViewById(R.id.llButtons)
            val btDialog: Button = dialogForReachedStartPoint!!.findViewById(R.id.btDialog)
            val tvTitle: TextView = dialogForReachedStartPoint!!.findViewById(R.id.tvTitle)
            val tvText: TextView = dialogForReachedStartPoint!!.findViewById(R.id.tvText)
            val ivDialog: ImageView = dialogForReachedStartPoint!!.findViewById(R.id.ivDialog)
            val clDialog: ConstraintLayout =
                dialogForReachedStartPoint!!.findViewById(R.id.clDialog)
// Convert 300dp to pixels
            val widthInDp = 350
            val scale = requireContext().resources.displayMetrics.density
            val widthInPx = (widthInDp * scale + 0.5f).toInt()

            val params = clDialog.layoutParams
            params.width = widthInPx
            clDialog.layoutParams = params
            llButtons.visibility = View.GONE
            btDialog.visibility = View.VISIBLE
            tvTitle.text = getString(R.string.alert)
            tvText.text = getString(R.string.you_are_reached_your_start)
            ivDialog.setImageResource(R.drawable.des_reached)
            ivDialog.visibility = View.VISIBLE
            btDialog.text = getString(R.string.ok)
            btDialog.setOnClickListener {
                dialogForReachedStartPoint?.dismiss()
                if (!onClickReached) {
                    onClickReached = true
                    binding.btNext.performClick()
                }
                dialogForReachedStartPoint = null // ✅ Clear the dialog reference after dismiss
            }

            dialogForReachedStartPoint?.setOnDismissListener {
                dialogForReachedStartPoint?.dismiss()
                if (!onClickReached) {
                    onClickReached = true
                    binding.btNext.performClick()
                }
                dialogForReachedStartPoint = null // ✅ Also clear if dismissed in any other way
            }
            val window = activity?.window
            val decorView = window?.decorView as ViewGroup
            val blurView = View(activity)
            blurView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            decorView.addView(blurView)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            window.setDimAmount(2f)
            dialogForReachedStartPoint?.show()

        }
    }
}