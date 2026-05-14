package com.pos10.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar

@SuppressLint("StaticFieldLeak")
object GlobalSnackbar {

    private var currentActivity: Activity? = null

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = activity
            }
            @SuppressLint("StaticFieldLeak")
            override fun onActivityStarted(activity: Activity) {
                currentActivity = activity
            }
            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity == activity) {
                    currentActivity = null
                }
            }
        })
    }

    private fun show(message: String, bgColor: Int) {
        val activity = currentActivity ?: return
        val rootView: View = activity.findViewById(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).apply {
            setBackgroundTint(bgColor)
            setTextColor(Color.WHITE)
            show()
        }
    }

    fun success(message: String) = show(message, Color.parseColor("#333333"))
    fun error(message: String) = show(message, Color.parseColor("#F44336"))   // Red
    fun info(message: String) = show(message, Color.parseColor("#333333"))
}
