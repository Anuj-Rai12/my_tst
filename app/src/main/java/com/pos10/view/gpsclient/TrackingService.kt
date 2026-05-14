package com.assetinfinity.app.gpsclient

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.pos10.R
import com.pos10.view.MainActivity

class TrackingService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var trackingController: TrackingController? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")

        // ✅ Create notification channel BEFORE calling startForeground()
        createNotificationChannel()

        // ✅ Acquire partial wake lock to keep CPU running during background tracking
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "$packageName:$TAG"
        )
        wakeLock?.acquire()

        // ✅ Start foreground service with proper notification
        val notification = createNotification(this)
        startForeground(NOTIFICATION_ID, notification)

        // ✅ Notify service started
        sendBroadcast(Intent(ACTION_STARTED).setPackage(packageName))

        // ✅ Check permission before starting location tracking
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            trackingController = TrackingController(this)
            trackingController?.start()
        } else {
            Log.w(TAG, "Location permission not granted. Stopping service.")
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service onStartCommand")
        return START_STICKY // ✅ ensures service restarts if killed by system
    }

    override fun onDestroy() {
        Log.i(TAG, "Service destroyed")

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)

        // ✅ Notify stopped
        sendBroadcast(Intent(ACTION_STOPPED).setPackage(packageName))

        // ✅ Release wake lock safely
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }

        // ✅ Stop controller
        trackingController?.stop()
        trackingController = null

        super.onDestroy()
    }

    // ===========================================================
    // Notification setup
    // ===========================================================
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MainActivity.PRIMARY_CHANNEL,
                "Background Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows that location tracking is active"
                setShowBadge(false)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createNotification(context: Context): Notification {
        val channelId = MainActivity.PRIMARY_CHANNEL

        val intent = Intent(context, MainActivity::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, flags)
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Location tracking active")
            .setContentText("Running in background")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

    }

    companion object {
        private const val TAG = "TrackingService"
        private const val NOTIFICATION_ID = 1
        const val ACTION_STARTED = "com.assetinfinity.action.SERVICE_STARTED"
        const val ACTION_STOPPED = "com.assetinfinity.action.SERVICE_STOPPED"
    }
}
