package com.udacity.project4.authentication

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*
import timber.log.Timber


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthenticationActivity"
        const val AUTHENTICATION_RESULT_CODE = 1001
        const val FINE_LOCATION_PERMISSION_REQUEST_CODE = 311

    }

    override fun onCreate(savedInstanceState: Bundle?) {

//         DONE: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          DONE: If the user was authenticated, send him to RemindersActivity

//          DONE: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)


        btn_login_auth_act.background.alpha = 127
        btn_login_auth_act.setOnClickListener { checkForAuthenticatedUser() }
        btn_login_auth_act.isClickable = false
    }

    override fun onStart() {
        super.onStart()
        checkFineLocationPermissions()
    }



    private fun checkFineLocationPermissions() {
        when {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                    btn_login_auth_act.isClickable = true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle(R.string.location_required_error)
                dialog.setMessage(R.string.fine_location_denied_explanation)
                dialog.setPositiveButton(android.R.string.ok, null)
                dialog.setOnDismissListener {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        FINE_LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
                dialog.show()

            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }


    private fun checkForAuthenticatedUser() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            //signed in
            launchReminders()
        } else {
            // not signed in
            launchLogin()
        }
    }

    private fun launchLogin() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                //.setAuthMethodPickerLayout(customLayout)
                .setTheme(R.style.LoginTheme)
                .build(),
            AUTHENTICATION_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTHENTICATION_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Timber.i("$TAG Successfully Signed in User ${FirebaseAuth.getInstance().currentUser?.displayName}")
                //hide text and button on page
                hideWelcomeTextAndLogin()
                launchReminders()

            } else {
                // Sign in failed.
                Timber.i("$TAG Sign in unsuccessful ${response?.error?.errorCode}")
                showToast(getString(R.string.login_required))
            }
        }
    }

    private fun hideWelcomeTextAndLogin() {
        textView.isVisible = false
        btn_login_auth_act.isVisible = false
    }

    private fun showToast(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()

    }

    private fun launchReminders() {
        val intent = Intent(this, RemindersActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
       Timber.i("$TAG, onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED
        ) {
            // Permission denied.

            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(R.string.location_required_error)
            dialog.setMessage(R.string.fine_location_denied_explanation)
            dialog.setPositiveButton(android.R.string.ok, null)
            dialog.setOnDismissListener {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            dialog.show()


        }

         else {
            Timber.i("Permissions: %s", permissions[0])
            btn_login_auth_act.isClickable = true

        }
    }

}

