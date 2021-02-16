package com.udacity.project4.locationreminders.reminderdetails


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentDetailReminderBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import kotlinx.android.synthetic.main.fragment_detail_reminder.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class ReminderDetailFragment : BaseFragment(), OnMapReadyCallback {

    override val _viewModel: ReminderDetailViewModel by inject()
    private lateinit var binding: FragmentDetailReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var map: GoogleMap

    private lateinit var reminder: ReminderDataItem

    val args: ReminderDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_detail_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        setTitle(getString(R.string.reminder_detail_title))
        val mapFragment = childFragmentManager.findFragmentById(R.id.rdf_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //checking for arguments which will be 'arguments' in the intent bundle or 'args' if from the navigation safeargs
        if(arguments?.get(EXTRA_ReminderDataItem) != null) {
            reminder = arguments!![EXTRA_ReminderDataItem] as ReminderDataItem
        } else {
            reminder = args.reminder
            Timber.i("Reminder %s", reminder)
        }

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        setDisplayHomeAsUpEnabled(true)

        btn_remove.setOnClickListener { geofenceRemove() }
        btn_back.setOnClickListener { _viewModel.navigationCommand.value = NavigationCommand.Back }
        rdf_location.text = reminder.location
        rdf_title.text = reminder.title
        rdf_description.text = reminder.description

    }

    private fun geofenceRemove() {
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        geofencingClient.removeGeofences(listOf(reminder.id)).run {
            addOnSuccessListener {
                Timber.i(
                    "Geofence removed %s", reminder.id
                )
                _viewModel.deleteReminder(reminder)
            }
            addOnFailureListener {
                Timber.i("$it: Geofence not removed")
            }
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0 ?: return
        val reminderLatLng = LatLng(reminder.latitude!!, reminder.longitude!!)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(reminderLatLng, 18f))
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        map.addMarker(
            MarkerOptions()
                .position(reminderLatLng)
                .title(reminder.location)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"
    }

}

