package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
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

    companion object {
        const val GEOFENCE_RADIUS_IN_METERS = 100f
        const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 411

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            //testing
            Timber.i(
                "Data I have: %s, %s, %s, %s, %s",
                title,
                description,
                location,
                latitude,
                longitude
            )

            //start the process of adding the reminder to the GeoFence list and the DB
            val newReminder: ReminderDataItem =
                (ReminderDataItem(title, description, location, latitude, longitude))


            validateDataForSaveAndGeoFence(newReminder)


//            DONE: use the user entered reminder details to:
//             1) add a geofencing request
//             2)DONE save the reminder to the local db
        }
    }

    fun addNewReminderGeoFence(reminder: ReminderDataItem) {
        //build Geofence object
        val geofence = Geofence.Builder()
            //set the id from the new reminder
            .setRequestId(reminder.id)
            //set the region
            .setCircularRegion(reminder.latitude!!, reminder.longitude!!, GEOFENCE_RADIUS_IN_METERS)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(0)
            .addGeofence(geofence)
            .build()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermissions()
        }
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {

                //I do not add the reminder to the DB unless the geofence is there otherwise
                //the user might think they have set a reminder when in reality there is no geofence
                //and it will never alert
                addReminderToDb(reminder)
            }
            addOnFailureListener {
                _viewModel.showToast.value = "Problem adding the Geofence"
                Timber.i(it)
            }
        }
    }

    private fun checkPermissions() {
        when {
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                return

            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) -> {
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle(getString(R.string.allow_location_all_time))
                dialog.setMessage(getString(R.string.must_allow_location))
                dialog.setPositiveButton(android.R.string.ok, null)
                dialog.setOnDismissListener {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
                dialog.show()

            }
            else -> {

                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun addReminderToDb(reminder: ReminderDataItem) {
        _viewModel.validateAndSaveReminder(reminder)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (
            grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED
        ) {
            // Permission denied.

            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle(R.string.location_required_error)
            dialog.setMessage(R.string.fine_location_denied_explanation)
            dialog.setPositiveButton(android.R.string.ok, null)
            dialog.setOnDismissListener {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            dialog.show()

        } else {
            return
        }
    }

    private fun validateDataForSaveAndGeoFence(reminder: ReminderDataItem) {
        Timber.i("Reminder %s", reminder)

        when (null) {
            reminder.latitude -> {
                //snackout select location
                _viewModel.showSnackBar.value = getString(R.string.err_select_location)
                Timber.i("Coordinates are Empty")
                return
            }
            reminder.title -> {
                Timber.i("Title is Empty")
                //snack out title
                _viewModel.showSnackBar.value = getString(R.string.err_enter_title)
                return
            }

            reminder.description -> {
                Timber.i("Description is Empty")
                //snack out description
                _viewModel.showSnackBar.value = getString(R.string.err_enter_description)
                return
            }
            else -> {
                //ok to set geofence and save
                addNewReminderGeoFence(reminder)
            }
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
