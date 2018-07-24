package com.melberry.viewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomSheetBehavior
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.melberry.utils.MapMarker
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.melberry.data.*
import com.melberry.utils.City
import com.melberry.R
import com.melberry.utils.ParkingSpotState

class MainViewModel(application: Application) : AndroidViewModel(application),
        GoogleMap.OnMapClickListener, ClusterManager.OnClusterClickListener<MapMarker>, ClusterManager.OnClusterItemClickListener<MapMarker>,
        GoogleMap.OnCameraMoveStartedListener {

    companion object {
        var TAG = this::class.java.simpleName!!
    }

    // Copy of spotList from View Model
    val currentSpotList: MutableLiveData<List<SpotDataClass>> = MutableLiveData()
    var backupSpotList: MutableList<SpotDataClass> = mutableListOf()

    // UI data binding
    private val markerId = MutableLiveData<String>()
    private val infoBayId = MutableLiveData<String>()
    val status = MutableLiveData<String>()
    val typeDesc = MutableLiveData<String>()
    val price = MutableLiveData<String>()

    // region Restrictions Description - region
    val restrictions0Visibility = MutableLiveData<Boolean>()
    val restrictions1Visibility = MutableLiveData<Boolean>()
    val restrictions2Visibility = MutableLiveData<Boolean>()
    val restrictions3Visibility = MutableLiveData<Boolean>()
    val restrictions4Visibility = MutableLiveData<Boolean>()
    val restrictions5Visibility = MutableLiveData<Boolean>()

    val restrictions0Days = MutableLiveData<String>()
    val restrictions1Days = MutableLiveData<String>()
    val restrictions2Days = MutableLiveData<String>()
    val restrictions3Days = MutableLiveData<String>()
    val restrictions4Days = MutableLiveData<String>()
    val restrictions5Days = MutableLiveData<String>()

    val restrictions0Times = MutableLiveData<String>()
    val restrictions1Times = MutableLiveData<String>()
    val restrictions2Times = MutableLiveData<String>()
    val restrictions3Times = MutableLiveData<String>()
    val restrictions4Times = MutableLiveData<String>()
    val restrictions5Times = MutableLiveData<String>()

    val restrictions0Description = MutableLiveData<String>()
    val restrictions1Description = MutableLiveData<String>()
    val restrictions2Description = MutableLiveData<String>()
    val restrictions3Description = MutableLiveData<String>()
    val restrictions4Description = MutableLiveData<String>()
    val restrictions5Description = MutableLiveData<String>()

    // endregion

    // BottomSheet Behaviour
    val bottomSheetState: MutableLiveData<Int>
    val bottomSheetStateListener: BottomSheetBehavior.BottomSheetCallback
    val isBottomSheetExpanded = MutableLiveData<Boolean>()
    val isBottomSheetHidden = MutableLiveData<Boolean>()
    val isBottomSheetCollapsed = MutableLiveData<Boolean>()

    // Disability
    private var disability: Boolean = false

    // Filter bold preference
    val is14Bold = MutableLiveData<Boolean>()
    val is12Bold = MutableLiveData<Boolean>()
    val is1Bold = MutableLiveData<Boolean>()
    val is2Bold = MutableLiveData<Boolean>()
    val is4Bold = MutableLiveData<Boolean>()

    // Filter
    var currentFilterRestriction = 15 // TODO configuration file
    var filteredSpotList = MutableLiveData<List<SpotDataClass>>()
    val progressListener: SeekBar.OnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            Log.d(TAG, isDisableOn().toString())
            when {
            // 1/4P
                seekBar?.progress == 0 -> {
                    updateProgressDescription(0)
                    filteredSpotList.value = null
                    filteredSpotList.value = backupSpotList.filter { it.getDuration(isDisableOn()) >= 15 }

                    currentFilterRestriction = 15

                    // Analytics
                    val params = Bundle()
                    params.putString(getApplication<Application>().applicationContext.getString(R.string.event_key), getApplication<Application>().applicationContext.getString(R.string.filter_15_analytics_event))
                    analytics.logEvent(getApplication<Application>().applicationContext.getString(R.string.filter_15_analytics_event), params)
                }
            // 1/2P
                seekBar?.progress == 1 -> {
                    updateProgressDescription(1)
                    filteredSpotList.value = null
                    filteredSpotList.value = backupSpotList.filter { it.getDuration(isDisableOn()) >= 30 }

                    currentFilterRestriction = 30

                    // Analytics
                    val params = Bundle()
                    params.putString(getApplication<Application>().applicationContext.getString(R.string.event_key), getApplication<Application>().applicationContext.getString(R.string.filter_30_analytics_event))
                    analytics.logEvent(getApplication<Application>().applicationContext.getString(R.string.filter_30_analytics_event), params)
                }
            // 1P
                seekBar?.progress == 2 -> {
                    updateProgressDescription(2)
                    filteredSpotList.value = null
                    filteredSpotList.value = backupSpotList.filter { it.getDuration(isDisableOn()) >= 60 }

                    currentFilterRestriction = 60

                    // Analytics
                    val params = Bundle()
                    params.putString(getApplication<Application>().applicationContext.getString(R.string.event_key), getApplication<Application>().applicationContext.getString(R.string.filter_60_analytics_event))
                    analytics.logEvent(getApplication<Application>().applicationContext.getString(R.string.filter_60_analytics_event), params)
                }
            // 2P
                seekBar?.progress == 3 -> {
                    updateProgressDescription(3)
                    filteredSpotList.value = null
                    filteredSpotList.value = backupSpotList.filter { it.getDuration(isDisableOn()) >= 120 }

                    currentFilterRestriction = 120

                    // Analytics
                    val params = Bundle()
                    params.putString(getApplication<Application>().applicationContext.getString(R.string.event_key), getApplication<Application>().applicationContext.getString(R.string.filter_120_analytics_event))
                    analytics.logEvent(getApplication<Application>().applicationContext.getString(R.string.filter_120_analytics_event), params)
                }
            // 4P
                seekBar?.progress == 4 -> {
                    updateProgressDescription(4)
                    filteredSpotList.value = null
                    filteredSpotList.value = backupSpotList.filter { it.getDuration(isDisableOn()) >= 240 }

                    currentFilterRestriction = 240

                    // Analytics
                    val params = Bundle()
                    params.putString(getApplication<Application>().applicationContext.getString(R.string.event_key), getApplication<Application>().applicationContext.getString(R.string.filter_240_analytics_event))
                    analytics.logEvent(getApplication<Application>().applicationContext.getString(R.string.filter_240_analytics_event), params)
                }
            }
        }

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        }

    }

    // Centering stuff
    private var fusedLocationClient: FusedLocationProviderClient
    private var locationRequest: LocationRequest
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            // Inform activity that location FAB was pressed with current location
            locationFabNotification.value = CameraUpdateFactory.newLatLng(LatLng(p0?.lastLocation?.latitude!!, p0.lastLocation?.longitude!!))
        }
    }
    var isCameraFollowingUsersLocation = MutableLiveData<Boolean>()

    // Notifications with live data
    val cameraCenteringNotification: MutableLiveData<CameraUpdate> = MutableLiveData()
    val locationFabNotification: MutableLiveData<CameraUpdate> = MutableLiveData()
    val changeIconNotification: MutableLiveData<MapMarker> = MutableLiveData()
    val changeCityNotification: MutableLiveData<Int> = MutableLiveData()
    val navigateNotification: MutableLiveData<LatLng> = MutableLiveData()

    // Clicked marker
    private var clickedClusterItem: MapMarker? = null

    // Firebase data
    private var firebaseConnection: FirebaseConnectionInterface
    private var firebaseInterval: FirebaseIntervals = FirebaseIntervals
    private val dataObserver: Observer<List<SpotDataClass>>
    private val intervalObserver: Observer<List<ParkingInterval>>

    // SearchBar state
    val searchBarHidden: MutableLiveData<Boolean> = MutableLiveData()

    // Design configuration
    val isFilterVisible = MutableLiveData<Boolean>()
    val isTypeDescVisible = MutableLiveData<Boolean>()
    val isPriceVisible = MutableLiveData<Boolean>()
    val isRestrictionListVisible = MutableLiveData<Boolean>()

    // Firebase analytics
    private var analytics: FirebaseAnalytics

    init {

        // TODO change visibility/layout logic
        restrictions0Visibility.value = true
        restrictions1Visibility.value = true
        restrictions2Visibility.value = true
        restrictions3Visibility.value = true
        restrictions4Visibility.value = true
        restrictions5Visibility.value = true

        // Analytics init
        analytics = FirebaseAnalytics.getInstance(application)

        // Map configurations depending on chosen city
        val city = getCityPreference()
        when (city) {
            City.BRATISLAVA -> {
                isFilterVisible.value = false
                isTypeDescVisible.value = true
                isPriceVisible.value = true
                isRestrictionListVisible.value = false
                firebaseConnection = BratislavaFirebaseConnection
            }
            else -> {
                isFilterVisible.value = true
                isTypeDescVisible.value = true
                isPriceVisible.value = true
                isRestrictionListVisible.value = true
                firebaseConnection = MelbourneFirebaseConnection
            }

        }

        // Filter init
        is14Bold.value = true

        // Load disability preference
        disability = isDisableOn()

        // BottomSheet settings - after app is open is hidden
        isBottomSheetHidden.value = true
        bottomSheetState = MutableLiveData<Int>().apply { this.value = BottomSheetBehavior.STATE_HIDDEN }
        bottomSheetStateListener = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                bottomSheetState.value = newState

                // Update states to actualise depending layouts(filter, search bar...)
                isBottomSheetExpanded.value = bottomSheetState.value == BottomSheetBehavior.STATE_EXPANDED
                isBottomSheetHidden.value = bottomSheetState.value == BottomSheetBehavior.STATE_HIDDEN
                isBottomSheetCollapsed.value = bottomSheetState.value == BottomSheetBehavior.STATE_COLLAPSED

                // Link behaviour of search Bar with BottomSheet
                if (bottomSheetState.value == BottomSheetBehavior.STATE_EXPANDED ||
                        bottomSheetState.value == BottomSheetBehavior.STATE_COLLAPSED) {
                    searchBarHidden.value = true
                } else if (bottomSheetState.value == BottomSheetBehavior.STATE_HIDDEN) {
                    searchBarHidden.value = false
                    changeIconNotification.value = null
                }

                // Analytics
                if (bottomSheetState.value == BottomSheetBehavior.STATE_EXPANDED) {
                    // Analytics
                    val params = Bundle()
                    params.putString(getApplication<Application>().applicationContext.getString(R.string.event_key), getApplication<Application>().applicationContext.getString(R.string.expanded_detail_analytics_event))
                    analytics.logEvent(getApplication<Application>().applicationContext.getString(R.string.expanded_detail_analytics_event), params)

                    // Move marker up to be still visible
                    cameraCenteringNotification.value = CameraUpdateFactory.scrollBy(0f, 200f)
                }
            }
        }

        // Observer for using data about markers from firebase real time db
        dataObserver = Observer {

            currentSpotList.value = it?.filter { it.getDuration(isDisableOn()) >= currentFilterRestriction }

            it?.forEach { newItem ->
                val oldItem = backupSpotList.find { oldItem -> oldItem.markerId == newItem.markerId }
                backupSpotList.remove(oldItem)
                backupSpotList.add(newItem)

                if (newItem.markerId == markerId.value) {

                    // Data shown in BottomSheet are updated using new information from model spot list
                    updateMelbourneBottomSheetUI(newItem)
                }
            }
        }

        // Observer for using data about intervals from firebase real time db
        intervalObserver = Observer {
            updateMelbourneIntervalsUI(it)
        }

        // VM is listening on changes that happened in model spot list and after change is updating UI
        firebaseConnection.getData().observeForever(dataObserver) // TODO change getData vs data in next line
        firebaseInterval.data.observeForever(intervalObserver)

        // centering stuff
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>().applicationContext)
        locationRequest = LocationRequest()
        locationRequest.interval = 1000 // TODO configuration file

    }

    override fun onCleared() {
        super.onCleared()

        // Remove observers
        firebaseConnection.getData().removeObserver(dataObserver)
        firebaseInterval.data.removeObserver(intervalObserver)
    }

    override fun onClusterClick(cluster: Cluster<MapMarker>): Boolean {
        // Notify activity that map should zoom in because user clicked on cluster marker
        cameraCenteringNotification.value = CameraUpdateFactory.newLatLngBounds(getBounds(cluster), 10)
        return true
    }

    override fun onClusterItemClick(clickedClusterItem: MapMarker?): Boolean {
        this.clickedClusterItem = clickedClusterItem

        // Data shown in bottom sheet + updating UI
        val clickedSpot = backupSpotList.single { it.markerId == clickedClusterItem?.title }
        Log.d(TAG, clickedSpot.toString())

        // BS configuration depending on chosen city
        when (getCityPreference()) {
            City.MELBOURNE -> updateMelbourneBottomSheetUI(clickedSpot)
            else -> updateBratislavaBottomSheetUI(clickedSpot)
        }
        return true
    }

    override fun onMapClick(latLng: LatLng?) {
        // After click on map, bottom sheet is hidden
        bottomSheetState.value = BottomSheetBehavior.STATE_HIDDEN
        // and activity is notified that marker should not have clicked icon
        changeIconNotification.value = null
    }

    fun onLocationButtonClick() {
        var cameraUpdate: CameraUpdate? = null

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Start Following
                    cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 17f)
                } else {
                    Toast.makeText(getApplication<Application>().applicationContext, getApplication<Application>().applicationContext.getString(R.string.gps_turned_off_notification), Toast.LENGTH_SHORT).show()
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            isCameraFollowingUsersLocation.value = true // TODO should not be in if?

        } catch (e: SecurityException) {
            Log.e(TAG, e.toString())
        } finally {
            locationFabNotification.value = cameraUpdate
        }
    }

    override fun onCameraMoveStarted(reason: Int) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            stopFollowing()
        }
    }

    private fun isDisableOn(): Boolean {
        val sharedPref = getApplication<Application>().getSharedPreferences(getApplication<Application>().applicationContext.getString(R.string.disability_key), Context.MODE_PRIVATE)
        return sharedPref.getBoolean(getApplication<Application>().applicationContext.getString(R.string.disability_key), false)
    }

    private fun updateMelbourneBottomSheetUI(it: SpotDataClass) {
        bottomSheetState.value = BottomSheetBehavior.STATE_HIDDEN

        // Ask for intervals data
        val intervals = listOf<ParkingInterval>()
        firebaseInterval.getIntervals(it.infoBayId)
        Log.d(TAG, intervals.toString())

        // Data binding has new values
        markerId.value = it.markerId
        infoBayId.value = getApplication<Application>().applicationContext.getString(R.string.bay_label) + it.infoBayId

        if (it.status == ParkingSpotState.FREE.toString()) status.value = getApplication<Application>().applicationContext.getString(R.string.availabe_label)
        else status.value = it.status

        // Current Parking Restrictions
        if (it.getCurrentTypeDesc() == "") typeDesc.value = getApplication<Application>().applicationContext.getString(R.string.unrestricted_label)
        else typeDesc.value = it.getCurrentTypeDesc()

        price.value = getApplication<Application>().applicationContext.getString(R.string.melbourne_price)

        // expandBottomSheet() is not missing because it waits for interval data
    }

    private fun updateBratislavaBottomSheetUI(it: SpotDataClass) {
        bottomSheetState.value = BottomSheetBehavior.STATE_HIDDEN

        // Data binding has new values
        markerId.value = it.markerId
        infoBayId.value = getApplication<Application>().applicationContext.getString(R.string.bay_label) + it.infoBayId

        if (it.status == ParkingSpotState.FREE.toString()) status.value = getApplication<Application>().applicationContext.getString(R.string.availabe_label)
        else status.value = it.status

        // Current Parking Restrictions
        if (it.getCurrentTypeDesc() == "") typeDesc.value = getApplication<Application>().applicationContext.getString(R.string.unrestricted_label)
        price.value = getApplication<Application>().applicationContext.getString(R.string.price_free)

        expandBottomSheet() // expand because it is so small that collapsed state has no value
    }


    // TODO CHANGE!!!!
    private fun updateMelbourneIntervalsUI(intervals: List<ParkingInterval>?) {

        if (intervals != null) {
            if (intervals.isNotEmpty() && intervals[0].getFormattedFromDay() != "") {
                restrictions0Visibility.value = true

                if (intervals[0].getFormattedFromDay() == intervals[0].getFormattedToDay()) restrictions0Days.value = intervals[0].getFormattedFromDay()
                else restrictions0Days.value = intervals[0].getFormattedFromDay() + " - " + intervals[0].getFormattedToDay()

                restrictions0Times.value = intervals[0].getFormattedStartTime() + " - " + intervals[0].getFormattedEndTime()
                restrictions0Description.value = intervals[0].typedesc + getEphString(intervals[0].effectiveonph)

            } else {
                restrictions0Visibility.value = false
            }

            if (intervals.size >= 2 && intervals[1].getFormattedFromDay() != "") {
                restrictions1Visibility.value = true

                if (intervals[1].getFormattedFromDay() == intervals[1].getFormattedToDay()) restrictions1Days.value = intervals[1].getFormattedFromDay()
                else restrictions1Days.value = intervals[1].getFormattedFromDay() + " - " + intervals[1].getFormattedToDay()

                restrictions1Times.value = intervals[1].getFormattedStartTime() + " - " + intervals[1].getFormattedEndTime()
                restrictions1Description.value = intervals[1].typedesc + getEphString(intervals[1].effectiveonph)
            } else {
                restrictions1Visibility.value = false
            }

            if (intervals.size >= 3 && intervals[2].getFormattedFromDay() != "") {
                restrictions2Visibility.value = true

                if (intervals[2].getFormattedFromDay() == intervals[2].getFormattedToDay()) restrictions2Days.value = intervals[2].getFormattedFromDay()
                else restrictions2Days.value = intervals[2].getFormattedFromDay() + " - " + intervals[2].getFormattedToDay()

                restrictions2Times.value = intervals[2].getFormattedStartTime() + " - " + intervals[2].getFormattedEndTime()
                restrictions2Description.value = intervals[2].typedesc + getEphString(intervals[2].effectiveonph)
            } else {
                restrictions2Visibility.value = false
            }

            if (intervals.size >= 4 && intervals[3].getFormattedFromDay() != "") {
                restrictions3Visibility.value = true

                if (intervals[3].getFormattedFromDay() == intervals[3].getFormattedToDay()) restrictions3Days.value = intervals[3].getFormattedFromDay()
                else restrictions3Days.value = intervals[3].getFormattedFromDay() + " - " + intervals[3].getFormattedToDay()

                restrictions3Times.value = intervals[3].getFormattedStartTime() + " - " + intervals[3].getFormattedEndTime()
                restrictions3Description.value = intervals[3].typedesc + getEphString(intervals[3].effectiveonph)
            } else {
                restrictions3Visibility.value = false
            }

            if (intervals.size >= 5 && intervals[4].getFormattedFromDay() != "") {
                restrictions4Visibility.value = true

                if (intervals[4].getFormattedFromDay() == intervals[4].getFormattedToDay()) restrictions4Days.value = intervals[4].getFormattedFromDay()
                else restrictions4Days.value = intervals[4].getFormattedFromDay() + " - " + intervals[4].getFormattedToDay()

                restrictions4Times.value = intervals[4].getFormattedStartTime() + " - " + intervals[4].getFormattedEndTime()
                restrictions4Description.value = intervals[4].typedesc + getEphString(intervals[4].effectiveonph)
            } else {
                restrictions4Visibility.value = false
            }

            if (intervals.size >= 6 && intervals[5].getFormattedFromDay() != "") {
                restrictions5Visibility.value = true

                if (intervals[5].getFormattedFromDay() == intervals[5].getFormattedToDay()) restrictions5Days.value = intervals[5].getFormattedFromDay()
                else restrictions5Days.value = intervals[5].getFormattedFromDay() + " - " + intervals[5].getFormattedToDay()

                restrictions5Times.value = intervals[5].getFormattedStartTime() + " - " + intervals[5].getFormattedEndTime()
                restrictions5Description.value = intervals[5].typedesc + getEphString(intervals[5].effectiveonph)
            } else {
                restrictions5Visibility.value = false
            }
        }

        collapseBottomSheet()
    }

    fun onNavigateClick() {
        navigateNotification.value = clickedClusterItem?.position
    }

    private fun getBounds(cluster: Cluster<MapMarker>): LatLngBounds? {
        val builder = LatLngBounds.builder()
        cluster.items.forEach { builder.include(it.position) }

        return builder.build()
    }

    private fun getEphString(value: String): String {
        return if (value == "0") getApplication<Application>().applicationContext.getString(R.string.eph_star_sign)
        else getApplication<Application>().applicationContext.getString(R.string.eph_star_empty_string)
    }

    fun updateProgressDescription(progress: Int) {
        if (progress == 0) {
            is14Bold.value = true
            is12Bold.value = false
            is1Bold.value = false
            is2Bold.value = false
            is4Bold.value = false
        }

        if (progress == 1) {
            is14Bold.value = false
            is12Bold.value = true
            is1Bold.value = false
            is2Bold.value = false
            is4Bold.value = false
        }

        if (progress == 2) {
            is14Bold.value = false
            is12Bold.value = false
            is1Bold.value = true
            is2Bold.value = false
            is4Bold.value = false
        }

        if (progress == 3) {
            is14Bold.value = false
            is12Bold.value = false
            is1Bold.value = false
            is2Bold.value = true
            is4Bold.value = false
        }

        if (progress == 4) {
            is14Bold.value = false
            is12Bold.value = false
            is1Bold.value = false
            is2Bold.value = false
            is4Bold.value = true
        }

    }

    fun stopFollowing() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isCameraFollowingUsersLocation.value = false
        } catch (e: SecurityException) {
            Log.e(TAG, e.toString())
        }
    }

    fun onChangeCityButtonClick() {
        when (changeCityNotification.value) {
            0 -> changeCityNotification.value = 1
            else -> changeCityNotification.value = 0
        }
    }

    private fun getCityPreference(): City {
        val sharedPref = getApplication<Application>().getSharedPreferences(getApplication<Application>().applicationContext.getString(R.string.city_preference_key), Context.MODE_PRIVATE)
        return City.valueOf(sharedPref.getString(getApplication<Application>().applicationContext.getString(R.string.city_preference_key), City.DEFAULT.name))
    }

    // Hack to wait for bottomsheet intervals update, because of jumping after update
    private fun collapseBottomSheet() {
        val handler = Handler()
        handler.postDelayed({
            bottomSheetState.value = BottomSheetBehavior.STATE_COLLAPSED
            changeIconNotification.value = clickedClusterItem
        }, 1000)
    }

    private fun expandBottomSheet() {
        bottomSheetState.value = BottomSheetBehavior.STATE_EXPANDED
        changeIconNotification.value = clickedClusterItem
    }
}