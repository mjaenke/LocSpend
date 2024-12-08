package com.cs407.locspend

import android.Manifest
import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings.Global.getString
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class NotificationHelper private constructor() {
    lateinit var sharedPreferences: SharedPreferences

    // Unique ID for each notification instance
    private var notificationId : Int = 0
    // Location Category
    private var locationCategory : String? = null
    // Message content
    private var message : String? = null

    private val notificationItems : ArrayList<NotificationItem> = ArrayList()

    companion object {
        @Volatile
        private var instance: NotificationHelper? = null

        const val CHANNEL_ID = "channel location"

        fun getInstance(): NotificationHelper {
            return instance ?: synchronized(this){
                instance ?: NotificationHelper().also {instance = it}
            }
        }
    }

    public fun createNotificationChannel(context : Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(mChannel)
        }

    }

    fun appendNotificationItem(locationCategory : String) {
        val item = NotificationItem(
            locationCategory,
            notificationItems.size
        )
        notificationItems.add(item)
    }

    fun showNotification(context : Context, id : Int) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, exit without showing notification
            return
        }

        //check if notifications have been enabled/disabled in settings
        val enabled : Boolean
        enabled = sharedPreferences.getBoolean("notifications_enabled", false)
        if (!enabled) {
            return // don't show notifications if they've been disabled
        }


        val item: NotificationItem = if (id ==-1) {
            notificationItems[notificationItems.size -1]
        } else {
            notificationItems[id]
        }

        // ignore notification button
        val ignoreIntent = Intent(context, ReplyReceiver::class.java).apply {
            putExtra("id", item.getId())
        }

        val ignorePendingIntent = PendingIntent.getBroadcast(
        context,
        item.getId(),
        ignoreIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
        )

        val ignoreAction = NotificationCompat.Action.Builder(
            R.drawable.ic_ignore_icon,
            "Ignore",
            ignorePendingIntent
        ).build()

        // start budgeting action button
        val startIntent = Intent(context, MainActivity::class.java)

        val startPendingIntent = PendingIntent.getActivity(
        context,
        item.getId(),
        startIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
        )

        val startAction = NotificationCompat.Action.Builder(
            R.drawable.ic_start_budgeting_icon,
            "Start Budgeting",
            startPendingIntent
        ).build()

        // Setup NotificationCompat.Builder to create and customize notification
        var builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle("Location: " + locationCategory)
            .setContentText("Check your Budget?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(ignoreAction)
            .addAction(startAction)


        // Get a NotificationManagerCompat instance to issue the notification
        val notificationManager = NotificationManagerCompat.from(context)
        // Send out notification with unique Id
        notificationManager.notify(item.getId(), builder.build())
    }



}