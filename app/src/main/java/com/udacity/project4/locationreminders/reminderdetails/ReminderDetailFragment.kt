package com.udacity.project4.locationreminders.reminderdetails

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentDetailReminderBinding
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.android.synthetic.main.fragment_detail_reminder.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class ReminderDetailFragment : BaseFragment() {

    override val _viewModel: ReminderDetailViewModel by inject()
    private lateinit var binding: FragmentDetailReminderBinding
    private lateinit var geofencingClient: GeofencingClient
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

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        reminder = args.reminder
        Timber.i("Reminder %s", reminder)

        btn_delete_geofence.setOnClickListener { geofenceRemove() }
//Todo, receiving reminder via safe args, populate the screen with data and add delete option!!!!

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

}

