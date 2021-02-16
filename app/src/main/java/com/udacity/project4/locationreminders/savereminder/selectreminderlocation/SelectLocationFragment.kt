package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*
import kotlin.concurrent.fixedRateTimer

class SelectLocationFragment : BaseFragment(),
    OnMapReadyCallback {  //, GoogleMap.OnMarkerDragListener add after onmap to implement on drag

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null
    private var currentLocationMarker: Marker? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    //values below for dropping a pin that is not a POI FUTURE IMPLEMENTATION
    //private lateinit var marker: Marker
    //private lateinit var markerStart: Marker
    //private lateinit var markerEnd: Marker
    //private var poiMarker: Marker
    private var locationSelected = false
    private val REQUEST_LOCATION_PERMISSION = 211
    private var marker = Any()

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()

                lastLocation = location
                if (currentLocationMarker != null) {
                    currentLocationMarker?.remove()
                }

                //Place current location marker
                val latLng = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title("Current Position")
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                currentLocationMarker = map.addMarker(markerOptions)

                //move map camera
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0F))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnSaveMapLocation.setOnClickListener {
            checkForSetLocationAndNavigate()
        }


//        DONE: add the map setup implementation
//        DONE: zoom to the user location after taking his permission
//        DONE: add style to the map
//        DONE: put a marker to location that the user selected


//        DONE: call this function after the user confirms on the selected location
        //onLocationSelected()

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        //stop the updates on pause
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
    }

    private fun onLocationSelected() {
        //        DONE: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence

        _viewModel.navigationCommand.postValue(
            NavigationCommand.Back
        )


    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // DONE: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        R.id.save_map_location -> {
            checkForSetLocationAndNavigate()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setPoiClick(map)
        setMapStyle(map)

        locationRequest = LocationRequest()
        locationRequest.interval = 120000
        locationRequest.fastestInterval = 120000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        //once the map is ready check permissions again and ebnnable the location with fused location provider
        //permissions check here might be overkill but it is conceivable the app was opened to the list
        //gaining permissions then the permissions were revoked..
        enableMyLocation()

//        val latitude = 48.02870
//        val longitude = -122.13355
//        val zoomLevel = 15f
//        val location = map.myLocation
//
//        val homeLatLng = LatLng(location.latitude, location.longitude)
//
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
//
//        map.addMarker(MarkerOptions().position(homeLatLng))

        //set this later for nonPOI marker and draggability
        //map.setOnMarkerDragListener(this)

        //setMapLongClick(map)
    }


//use for setting a non poi location later
//    private fun setMapLongClick(map: GoogleMap) {
//        map.setOnMapLongClickListener { latLng ->
//            // A Snippet is Additional text that's displayed below the title.
//            val snippet = String.format(
//                Locale.getDefault(),
//                "Lat: %1$.5f, Long: %2$.5f",
//                latLng.latitude,
//                latLng.longitude
//            )
//            marker = map.addMarker(
//                MarkerOptions()
//                    .position(latLng)
//                    .title(getString(R.string.dropped_pin))
//                    .snippet(snippet)
//                    .draggable(true)
//                    .alpha(0.6f)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//            )
//            //markerStart = marker
//
//            Timber.i(latLng.toString())
//            locationSelected = true
//        }
//    }

    fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
            _viewModel.selectedPOI.value = poi
            _viewModel.latitude.value = poiMarker.position.latitude
            _viewModel.longitude.value = poiMarker.position.longitude
            _viewModel.reminderSelectedLocationStr.value = poiMarker.title
            locationSelected = true

        }
    }


    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Timber.i("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Timber.i("Can't find style. Error: %s", e)
        }
    }

    private fun enableMyLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //Location Permission already granted
                fusedLocationProviderClient?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.myLooper()
                )
                map.isMyLocationEnabled = true
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
            map.isMyLocationEnabled = true
        }
    }

//overrides for marker drag
//    override fun onMarkerDragStart(p0: Marker?) {
//
//    }
//
//    override fun onMarkerDrag(p0: Marker?) {
//
//    }
//
//    override fun onMarkerDragEnd(p0: Marker) {
//
//        marker = p0
//        Timber.i(marker.position.toString())
//
//    }

    private fun checkForSetLocationAndNavigate() {
        if (locationSelected) {
            onLocationSelected()
        } else {
            _viewModel.showSnackBar.value = getString(R.string.select_poi)
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                val dialog = android.app.AlertDialog.Builder(requireContext())
                dialog.setTitle(R.string.location_required_error)
                dialog.setMessage(R.string.fine_location_denied_explanation)
                dialog.setPositiveButton(android.R.string.ok, null)
                dialog.setOnDismissListener {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_LOCATION_PERMISSION
                    )
                }
                dialog.show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        fusedLocationProviderClient?.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.myLooper()
                        )
                        map.setMyLocationEnabled(true)
                    }

                } else {

                    _viewModel.showSnackBar.value =
                        getString(R.string.fine_location_denied_explanation)
                }
                return
            }
        }
    }

}