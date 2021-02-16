package com.udacity.project4.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

fun sendNotification(context: Context, reminderDataItem: ReminderDataItem) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // We need to create a NotificationChannel associated with our CHANNEL_ID before sending a notification.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null
    ) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }
    //changed this up to navigate to a fragment instead of the activity
    //decided to use the fragment/viewmodel construct to use the same screen if a user clicks
    //on the list or gets sent their from the notification, less code and baseviewmodel options available

    //used NavDeepLinkBuilder after reviewing on Developer.android.com

    //val intent = ReminderDescriptionActivity.newIntent(context.applicationContext, reminderDataItem)
    val bundle = bundleOf("EXTRA_ReminderDataItem" to reminderDataItem)
    val navigationIntent = NavDeepLinkBuilder(context.applicationContext)
        .setComponentName(RemindersActivity::class.java)
        .setGraph(R.navigation.nav_graph)
        .setDestination(R.id.reminderDetailFragment)
        .setArguments(bundle)
        .createTaskStackBuilder()
        //.createPendingIntent()


    //create a pending intent that opens ReminderDescriptionActivity when the user clicks on the notification
//    val stackBuilder = TaskStackBuilder.create(context)
//        .addParentStack(ReminderDescriptionActivity::class.java)
//        .addNextIntent(intent)
    val notificationPendingIntent = navigationIntent
        .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)

//    build the notification object with the data to be shown
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(reminderDataItem.title)
        .setContentText(reminderDataItem.location)
        .setContentIntent(notificationPendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(getUniqueId(), notification)
}

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())