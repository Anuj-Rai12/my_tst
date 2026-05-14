package com.pos10.view.screens

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.rememberCameraPositionState
import android.os.Looper
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.*
//import android.graphics.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.pos10.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import com.google.accompanist.permissions.isGranted
import com.pos10.domain.AuthViewModel
import com.pos10.view.CoordinateManager
import com.pos10.view.navigation.Screen


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RouteTrackHistory(navHostController: NavHostController,viewModel: AuthViewModel= hiltViewModel()) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    LaunchedEffect(Unit) { locationPermission.launchPermissionRequest() }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    BackHandler {
        navHostController.navigate(Screen.DashboardScreen.route) {
            popUpTo(0) {
                inclusive = true
            }
        }
    }

    Log.d("TAG", "RouteTrackHistory: TRack ---- ${CoordinateManager.getCoordinates()}")
    val routeCoordinates = remember {
        CoordinateManager.getCoordinates().map { LatLng(it.latitude, it.longitude) }
    }

    val currentPosition = remember { mutableStateOf<LatLng?>(null) }
    val fullRoute = remember { mutableStateListOf<LatLng>() }
    val currentRotation = remember { mutableFloatStateOf(0f) }

    // Fetch curved route once
    LaunchedEffect(Unit) {
        val newRoute = fetchRoutePolyline1(
            origin = routeCoordinates.first(),
            destination = routeCoordinates.last(),
            apiKey ="AIzaSyASl47Ihm4-tNKiuEJQZ0wFNUjZa7-ymRA"
        )
        if (newRoute.isNotEmpty()) {
            fullRoute.clear()
            fullRoute.addAll(newRoute)
            cameraPositionState.centerOnLocation1(newRoute.first())
            if (newRoute.size > 1) {
                currentRotation.floatValue = bearingBetween1(newRoute[0], newRoute[1])
            }
        }
    }

    val locationCallback = rememberUpdatedState(newValue = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val newLatLng = LatLng(location.latitude, location.longitude)
            currentPosition.value = newLatLng

            coroutineScope.launch {
                cameraPositionState.centerOnLocation(newLatLng)
            }
        }
    })

    // Start location updates
    LaunchedEffect(locationPermission.status) @androidx.annotation.RequiresPermission(
        allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
    ) {
        if (locationPermission.status.isGranted) {
            val request = LocationRequest.create().apply {
                interval = 50_000
                fastestInterval = 5_000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback.value,
                Looper.getMainLooper()
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback.value)
        }
    }

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false)
        ) {
            if (fullRoute.isNotEmpty()) {
                Polyline(points = fullRoute, color = Color.Blue, width = 8f)
            }

            currentPosition.value?.let { pos ->
                Marker(
                    state = MarkerState(pos),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    rotation = currentRotation.floatValue,
                    flat = true,
                    anchor = Offset(0.5f, 0.5f),
                    title = "Live Location"
                )
            }

            // End marker
            Marker(
                state = MarkerState(routeCoordinates.last()),
                title = "Destination"
            )
        }

        Image(
            painter = painterResource(id = R.drawable.close_ic),
            contentDescription = "Close Map",
            modifier = Modifier
                .padding(top = 16.dp, end = 16.dp)
                .align(Alignment.TopEnd)
                .size(36.dp)
                .background(Color.White, shape = CircleShape)
                .clickable {
                    navHostController.navigate(Screen.DashboardScreen.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
        )
    }
}

fun decodePolyline1(encoded: String): List<LatLng> {
    val poly = mutableListOf<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
        lng += dlng

        val point = LatLng(lat / 1E5, lng / 1E5)
        poly.add(point)
    }
    return poly
}

suspend fun fetchRoutePolyline1(origin: LatLng, destination: LatLng, apiKey: String): List<LatLng> {
    val url = "https://maps.googleapis.com/maps/api/directions/json?" +
            "origin=${origin.latitude},${origin.longitude}" +
            "&destination=${destination.latitude},${destination.longitude}" +
            "&key=$apiKey"

    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            val jsonData = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(jsonData)
            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return@withContext emptyList()

            val overviewPolyline = routes.getJSONObject(0)
                .getJSONObject("overview_polyline")
                .getString("points")

            decodePolyline1(overviewPolyline)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

fun bearingBetween1(start: LatLng, end: LatLng): Float {
    val lat1 = Math.toRadians(start.latitude)
    val lon1 = Math.toRadians(start.longitude)
    val lat2 = Math.toRadians(end.latitude)
    val lon2 = Math.toRadians(end.longitude)

    val dLon = lon2 - lon1
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    return Math.toDegrees(atan2(y, x)).toFloat()
}

suspend fun CameraPositionState.centerOnLocation1(location: LatLng) {
    animate(CameraUpdateFactory.newLatLngZoom(location, 16f))
}

