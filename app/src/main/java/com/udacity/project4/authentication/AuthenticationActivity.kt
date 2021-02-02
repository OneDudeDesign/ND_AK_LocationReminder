package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import timber.log.Timber


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthenticationActivity"
        const val AUTHENTICATION_RESULT_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            //signed in
            val intent = Intent(this, RemindersActivity::class.java)
            startActivity(intent)
        } else {
            // not signed in
            launchLogin()

            //val loginButton = findViewById<Button>(R.id.auth_login_button)
            //loginButton.setOnClickListener { launchLogin()}
        }

//         DONE: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          DONE: If the user was authenticated, send him to RemindersActivity

//          DONE: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

    }

    private fun launchLogin() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
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
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)

            } else {
                // Sign in failed.
                Timber.i("$TAG Sign in unsuccessful ${response?.error?.errorCode}")
                showToast(R.string.sign_in_error)
            }
        }
    }

    private fun showToast(error: Any) {
        Toast.makeText(this, "$error", Toast.LENGTH_SHORT).show()

    }


}
