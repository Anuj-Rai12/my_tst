package com.pos10.util

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pos10.R
import com.pos10.view.MainActivity
import java.lang.ref.WeakReference

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val contextRef: WeakReference<Context> = WeakReference(this)
    override fun onNewToken(token: String) {

    }

    override fun onMessageReceived(p0: RemoteMessage) {
        if (p0 != null) {
            p0.notification?.let {
                val title = it.title
                val body = it.body
                sendNotificationData(contextRef.get(), body, title)
            }


            if (p0.data.isNotEmpty()) {
                val type = p0.data["NeedToRefresh"] ?: ""
                if (type.equals("1", ignoreCase = true)) {
                    val broadcastIntent = Intent("com.pay10.REFRESH_ACTION").apply {
                        putExtra("NeedToRefresh", type)
                    }
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)
                } else {
                }
            }
        }
    }

    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName

        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    override fun handleIntent(intent: Intent) {
        try {
            if (intent.extras != null) {
                val builder = RemoteMessage.Builder("MyFirebaseMessagingService")
                for (key in intent.extras!!.keySet()) {
                    builder.addData(key!!, intent.extras!![key].toString())
                }
                onMessageReceived(builder.build())
            } else {
                super.handleIntent(intent)
            }
        } catch (e: java.lang.Exception) {
            super.handleIntent(intent)
        }
    }

    private fun sendNotificationData(
        context: Context?,
        body: String?,
        title: String?,
        ) {
        val builder =
            NotificationCompat.Builder(this, "Channel_01")
        // Create a notificationManager object
        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // If android version is greater than 8.0 then create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "Channel_01",
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH)

            notificationChannel.enableLights(true)
            
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)

            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300)


            // Pass the notificationChannel object to notificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        var pendingIntent:PendingIntent
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("noty", "noty") // Pass any extra data if needed
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        // Set the notification parameters to the notification builder object
        builder.setContentTitle(title)
            .setContentText(body)
            .setColor(Color.WHITE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setSound(defaultSoundUri)
            .setPriority(Notification.DEFAULT_VIBRATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Set the image for the notification
        notificationManager.notify(1, builder.build())
    }

}