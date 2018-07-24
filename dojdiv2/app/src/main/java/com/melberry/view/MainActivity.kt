package com.melberry.view

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.maps.android.clustering.ClusterManager
import com.melberry.R
import com.melberry.data.SpotDataClass
import com.melberry.databinding.ActivityMainBinding
import com.melberry.utils.City
import com.melberry.utils.MapClusterRenderer
import com.melberry.utils.MapMarker
import com.melberry.utils.ParkingSpotState
import com.melberry.view.onboarding.OnboardingActivity
import com.melberry.viewModel.MainViewModel


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        var TAG = this::class.java.simpleName!!

        // Permissions
        private val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        private const val PERMISSION_ALL = 1
    }

    // VM
    private lateinit var mainViewModel: MainViewModel

    // Map
    private lateinit var mainMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<MapMarker>
    private lateinit var mapClusterRenderer: MapClusterRenderer
    private var markerList: MutableMap<String, MapMarker?> = mutableMapOf() // markerList[title] -> title of markers is used as an index

    // Permissions
    private var permissionGranted: Boolean = false

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Search bar
    private lateinit var autocompleteFragment: PlaceAutocompleteFragment
    private lateinit var searchLayout: LinearLayout
    private var searchMarker: Marker? = null

    // Progress bar
    private lateinit var progressBar: ProgressBar
    private lateinit var progressLock: LinearLayout

    // Changing clicked marker
    private var previousMarker: MapMarker? = null

    // Analytics
    private lateinit var analytics: FirebaseAnalytics

    // Dialogs
    private var noConnectionDialog: AlertDialog? = null
    private var navigationDialog: AlertDialog? = null

    @Suppress("UNCHECKED_CAST") // Because of "as T"  mainViewModel = ViewModelProviders... - not checked yet from Kotlin side
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, ".onCreate()")
        super.onCreate(savedInstanceState)

        // Data binding
        val binding: ActivityMainBinding? = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Progress lock to disable possibility of user to click somewhere during loading data
        progressBar = findViewById(R.id.progress_bar)
        progressLock = findViewById(R.id.progress_lock)

        // View model initialization
        mainViewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return MainViewModel(this@MainActivity.application) as T
            }
        }).get(MainViewModel::class.java)

        // Map fragment adding
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Binding connecting with ViewModel
        binding?.let {
            it.viewModel = mainViewModel
            it.setLifecycleOwner(this)
        }

        // region Search bar + animation
        searchLayout = findViewById(R.id.search_layout)
        searchLayout.translationY = 0f
        mainViewModel.searchBarHidden.observe(this, Observer {
            if (it == false) {
                // Come
                searchLayout.visibility = View.VISIBLE
                //searchLayout.alpha = 0.0f

                searchLayout.animate()
                        .translationY(0f)
                        //.alpha(1.0f)
                        .setListener(null)
            } else {
                // Go away
                searchLayout.animate()
                        .translationY(0f - searchLayout.height)
                        //.alpha(0.0f)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                searchLayout.visibility = View.INVISIBLE
                            }
                        })
            }
        })
        autocompleteFragment = fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment
        // endregion

        // Check to internet connectivity
        checkConnection()

        // Analytics
        analytics = FirebaseAnalytics.getInstance(this)

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        Log.i(TAG, ".onMapReady()")

        // Map init
        mainMap = googleMap!!
        clusterManager = ClusterManager(this, mainMap)
        mapClusterRenderer = MapClusterRenderer(this, mainMap, clusterManager)
        clusterManager.renderer = mapClusterRenderer

        // Turn progress lock on and show progress bar
        progressBar.visibility = View.VISIBLE
        progressLock.visibility = View.VISIBLE

        // If location is not allowed, filter to city that has been chosen in menu
        if (getCityPreference() == City.MELBOURNE) {
            mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-37.824363, 144.972497), 14f))
        } else if (getCityPreference() == City.BRATISLAVA) {
            mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(48.149667, 17.073889), 14f))
        }

        // Permissions check
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
        } else {
            centerToCurrentLocation()
        }

        // Map styling
        mainMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))

        // Listen on spotList changes and update markers on change
        mainViewModel.currentSpotList.observe(this, Observer { t ->
            if (t != null) updateSpots(t)
            else mainMap.clear()
        })

        mainViewModel.filteredSpotList.observe(this, Observer { t ->
            filterSports(t)
        })

        // Listeners for interaction with map
        mainMap.setOnMapClickListener(mainViewModel)
        mainMap.setOnCameraIdleListener(clusterManager)
        mainMap.setOnCameraMoveStartedListener(mainViewModel)
        mainMap.setOnMarkerClickListener(clusterManager)

        // Cluster manager settings
        clusterManager.setOnClusterItemClickListener(mainViewModel)
        clusterManager.setOnClusterClickListener(mainViewModel)

        // region FAB location blue dot
        mainMap.uiSettings.isMyLocationButtonEnabled = false
        mainViewModel.cameraCenteringNotification.observe(this, Observer { t -> mainMap.animateCamera(t) })

        // Show blue dot of your location
        try {
            mainMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            Log.e(TAG, e.toString())
        }

        // Registering observer for Location FAB interaction
        mainViewModel.locationFabNotification.observe(this, Observer { cameraUpdate ->
            // Request location permissions or animate camera if you have them
            if (cameraUpdate == null) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
            } else {
                mainMap.animateCamera(cameraUpdate)
            }
        })

        // Registering observer for CitiesMenu FAB interaction
        mainViewModel.changeCityNotification.observe(this, Observer {
            // Open Onboarding activity without animation
            // TODO open/reuse CitiesFragment from MainActivity?
            val intent = Intent(this@MainActivity, OnboardingActivity::class.java)
            intent.putExtra(getString(R.string.skip_animation_key), true)
            startActivity(intent)
            finish()
        })
        // endregion

        // Search - if place found - re-center map
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status?) {
                Log.d(TAG, "error")
            }

            override fun onPlaceSelected(place: Place) {
                // If some place was search before and marker is visible, remove it
                searchMarker?.remove()

                // Move camera to searched location
                mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 19f))

                // Create and show searching POI
                val searchMarkerOptions = MarkerOptions().position(place.latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.search_poi))
                searchMarker = mainMap.addMarker(searchMarkerOptions)

                // Analytics
                val params = Bundle()
                params.putString(getString(R.string.event_key), getString(R.string.search_event_key))
                analytics.logEvent(getString(R.string.search_event_key), params)

                // Stop following to not recenter camera to users location
                mainViewModel.stopFollowing()
            }

        })

        // Change Icon on ClusterClick to bigger one
        mainViewModel.changeIconNotification.observe(this, Observer { t -> changeClickedMarkerIcon(t) })

        // Open navigation
        mainViewModel.navigateNotification.observe(this, Observer {

            // Analytics
            val params = Bundle()
            params.putString(getString(R.string.event_key), getString(R.string.navigate_event_key))
            analytics.logEvent(getString(R.string.navigate_event_key), params)

            // region Show dialog that navigation is in test - region
            val dialogBuilder = AlertDialog.Builder(this)

            dialogBuilder.setTitle(R.string.navigation_title)
                    .setMessage(R.string.navigation_text)

            dialogBuilder.setPositiveButton(R.string.navigate_label) { _, _ ->
                if (it != null) {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val intent = Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("google.navigation:q=$latitude,$longitude"))
                    startActivity(intent)
                }
            }

            dialogBuilder.setNegativeButton(R.string.cancel_label) { _, _ -> }
            if (navigationDialog?.isShowing == false || navigationDialog == null) {
                navigationDialog = dialogBuilder.create()
                navigationDialog?.show()
            }
            // endregion
        })
    }

    private fun updateSpots(updatedSpotList: List<SpotDataClass>?) {
        Log.i(TAG, ".updateSpots()")

        // TODO test if occupied markers are removed
        updatedSpotList?.forEach {

            // if changed marker is already on map, remove it and...
            markerList[it.markerId]?.let {
                clusterManager.removeItem(it)
            }

            // ...add new/changed marker to map if it is free
            if (ParkingSpotState.valueOf(it.status) == ParkingSpotState.FREE) {
                val mapMarker = MapMarker(it.lat.toDouble(), it.lng.toDouble(), it.markerId, it.status, null)
                clusterManager.addItem(mapMarker)
                markerList[it.markerId] = mapMarker
            }

            // If it is occupied, remove it from the list also
            if (ParkingSpotState.valueOf(it.status) == ParkingSpotState.OCCUPIED) {
                markerList[it.markerId] = null
            }
        }

        // Cluster
        clusterManager.cluster()

        // Loading is finish turn lock of and show progress bar
        progressBar.visibility = View.GONE
        progressLock.visibility = View.GONE
    }

    private fun filterSports(filteredSpotList: List<SpotDataClass>?) {
        clusterManager.clearItems()
        filteredSpotList?.forEach {
            if (ParkingSpotState.valueOf(it.status) == ParkingSpotState.FREE) {
                val mapMarker = MapMarker(it.lat.toDouble(), it.lng.toDouble(), it.markerId, it.status, null)
                clusterManager.addItem(mapMarker)
            }
        }
        clusterManager.cluster()
    }

    private fun changeClickedMarkerIcon(marker: MapMarker?) {
        // "Un-click" previous marker
        if (previousMarker != null) {
            updateIcon(previousMarker, ParkingSpotState.FREE.toString(), null)
        }
        previousMarker = marker

        // No marker is clicked - for example map was clicked
        if (marker == null) {
            updateIcon(previousMarker, ParkingSpotState.FREE.toString(), null)
            previousMarker = null
        } else {
            // Make the clicked marker bigger
            updateIcon(marker, ParkingSpotState.CLICKED.toString(), 1f)
            mainMap.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
        }
    }

    private fun updateIcon(marker: MapMarker?, status: String, zIndex: Float?) {
        if (marker != null) {
            markerList[marker.title]?.let {
                clusterManager.removeItem(it)
            }

            // Add new/changed marker to map
            val mapMarker = MapMarker(marker.position.latitude, marker.position.longitude, marker.title, status, zIndex)
            clusterManager.addItem(mapMarker)
            markerList[marker.title] = mapMarker

            // Cluster
            clusterManager.cluster()
        }
    }

    private fun checkConnection(): Boolean {
        if (!isNetworkConnected()) {
            // Show dialog that connection has to be turned on
            val dialogBuilder = AlertDialog.Builder(this)

            dialogBuilder.setTitle(R.string.no_connection_title)
                    .setMessage(R.string.no_connection_text)

            dialogBuilder.setPositiveButton(R.string.try_again_label) { _, _ -> checkConnection() }
            dialogBuilder.setNegativeButton(R.string.close_label) { _, _ -> finish() }

            // Show only one dialog - pseudo singleton
            if (noConnectionDialog?.isShowing == false || noConnectionDialog == null) {
                noConnectionDialog = dialogBuilder.create()
                noConnectionDialog?.setCancelable(false)
                noConnectionDialog?.show()
            }
        }
        return true
    }

    private fun centerToCurrentLocation() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.applicationContext)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    mainMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 17f))
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo.isConnected
    }

    private fun getCityPreference(): City {
        val sharedPref = getSharedPreferences("cityPreference", Context.MODE_PRIVATE)
        return City.valueOf(sharedPref.getString("cityPreference", City.DEFAULT.name))
    }


    // region Permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionGranted = false
        if (hasPermissions(this, PERMISSIONS)) {
            centerToCurrentLocation()
        }
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions
                    .filter { ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
                    .forEach { return false }
            permissionGranted = true
        }
        return true
    }
    // endregion

}
