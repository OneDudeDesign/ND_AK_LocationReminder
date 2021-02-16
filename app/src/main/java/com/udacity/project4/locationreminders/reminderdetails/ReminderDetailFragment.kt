package com.udacity.project4.locationreminders.reminderdetails

import android.app.PendingIntent
import android.content.Intent
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
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentDetailReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
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
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    private lateinit var reminder: ReminderDataItem

    val args: ReminderDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_detail_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        setTitle(getString(R.string.reminder_detail_title))
        val mapFragment = childFragmentManager.findFragmentById(R.id.rdf_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        reminder = args.reminder
        Timber.i("Reminder %s", reminder)
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
                Timber.i("Geofence removed %s", reminder.id
                )
                _viewModel.deleteReminder(reminder)
            }
            addOnFailureListener {
                Timber.i("Geofence not removed %s" , it)
            }
        }
        Timber.i ("try and remove the geofence ")

    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0 ?: return
        val reminderLatLng = LatLng(reminder.latitude!!, reminder.longitude!!)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(reminderLatLng,18f))
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
    }

}

