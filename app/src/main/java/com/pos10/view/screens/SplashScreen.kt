package com.pos10.view.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import com.pos10.R
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.pos10.helper.SharedPreference
import com.pos10.view.MainActivity
import com.pos10.view.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController? = null) {
    val context = LocalContext.current
    val activity = context as MainActivity
    val sharedPref = SharedPreference.get(context)

    // Hide status & navigation bar for full splash
    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.window.setDecorFitsSystemWindows(false)
        activity.window.statusBarColor = Color.Transparent.toArgb()
        activity.window.navigationBarColor = Color.Transparent.toArgb()
    } else {
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility =
            activity.window.decorView.systemUiVisibility or
                    android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        @Suppress("DEPRECATION")
        activity.window.statusBarColor = Color.Transparent.toArgb()
    }

    // --- Permissions to request ---
    val requiredPermissions = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE
    )

    // Add FGS location permission only for Android 14+ (API 34+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        requiredPermissions += Manifest.permission.FOREGROUND_SERVICE_LOCATION
    }

    val permissionGranted = remember {
        mutableStateOf(requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }

    val isGpsEnabled = remember { mutableStateOf(checkGpsEnabled(context)) }

    // --- Permission Launcher ---
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionGranted.value = requiredPermissions.all { permissions[it] == true }
        isGpsEnabled.value = checkGpsEnabled(context)
    }

    // --- Lifecycle Observer to refresh on resume ---
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionGranted.value = requiredPermissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }
                isGpsEnabled.value = checkGpsEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // --- Request permissions when screen starts ---
    LaunchedEffect(Unit) {
        if (!permissionGranted.value) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        }
    }

    // --- Main Logic ---
    LaunchedEffect(permissionGranted.value, isGpsEnabled.value) {
        delay(2000) // splash delay

        if (permissionGranted.value) {
            if (isGpsEnabled.value) {
                // Add small delay to ensure activity visible (required on Android 12+)
                delay(500)

                // Start tracking service safely
                startTrackingService(context)

                // Navigate next
                if (!sharedPref.accessToken.isNullOrBlank()) {
                    navController?.navigate(Screen.DashboardScreen.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                } else {
                    navController?.navigate(Screen.LoginScreen.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            } else {
                //  GPS disabled → open settings
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            }
        } else {
            //  Permission not granted → request again
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        }
    }

    // --- UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFff6900)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_pos10),
            contentDescription = "POS10 Logo",
            modifier = Modifier.size(200.dp)
        )
    }

}

// Utility: Check GPS
private fun checkGpsEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

// Utility: Safely start Foreground Service for all Android versions
private fun startTrackingService(context: Context) {
    try {
        val intent = Intent(context, com.assetinfinity.app.gpsclient.TrackingService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requires app to be in foreground state
            if (context is Activity && !context.isFinishing) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                // if not safe, skip to avoid crash
                return
            }
        } else {
            // Android 11 and below
            ContextCompat.startForegroundService(context, intent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
@Preview(showSystemUi = true)
fun PreviewUi() {
    SplashScreen()
}


