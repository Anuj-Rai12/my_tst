package com.pos10.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.assetinfinity.app.gpsclient.DatabaseHelper
import com.assetinfinity.app.gpsclient.Position
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.pos10.db.synchandler.GlobalDbSyncHandler
import com.pos10.di.AuthEvent
import com.pos10.di.AuthEventManager
import com.pos10.domain.AuthViewModel
import com.pos10.helper.CommonUtils.returnAccessToken
import com.pos10.helper.NetworkService
import com.pos10.helper.SharedPreference
import com.pos10.model.remote.LocationTrackRequest
import com.pos10.ui.theme.ICronoDealerTheme
import com.pos10.view.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


val LocalNavHostController =
    compositionLocalOf<NavHostController> { error("Error Occurred in NavHostController creation") }


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var dbSyncHandler: GlobalDbSyncHandler
    private lateinit var networkService: NetworkService

    @Inject
     lateinit var authEventManager: AuthEventManager

    companion object {
        const val PRIMARY_CHANNEL = "default"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(512, 512)
        System.setProperty("http.keepAliveDuration", (30 * 1000).toString())
        // Initialize NetworkService with applicationContext
        networkService = NetworkService.getInstance(applicationContext)
        // Setup sync
        setupSync()
        setContent {
            WindowCompat.getInsetsController(window,
                LocalView.current).isAppearanceLightStatusBars = true
            val navController = rememberNavController()
            ICronoDealerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SwipeRefreshWrapper(dbSyncHandler, navController)

                }
            }
        }
    }

    @Composable
    private fun SwipeRefreshWrapper(dbSyncHandler: GlobalDbSyncHandler,
                                    navController: NavController,
                                    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current) {
        var isRefreshing by remember { mutableStateOf(false) }
        val swipeState = rememberSwipeRefreshState(isRefreshing)

        SwipeRefresh(state = swipeState, onRefresh = {
            isRefreshing = true
            setupSync(navController)

            lifecycleOwner.lifecycleScope.launch {
                delay(1500)
                isRefreshing = false
            }
        }) {
            LocationTracker(viewModel)
            AppNavigation(authViewModel = viewModel, authEventManager = authEventManager, context = this@MainActivity)
        }
    }

    fun setupSync(navController: NavController? = null) {
        Log.d("TAG", "setupSync: ------neww")
        val token = SharedPreference.get(this).accessToken
        if (token.isEmpty()) return
        // Manual sync if online
        if (networkService.getCurrentStatus()) {
            dbSyncHandler.manualSync(this, this, onInvalidToken = {
                Log.d("TAG", "setupSync: ------newwToken")
                SelectedRequestHolder.clearRequest()
                SingletonObject.clear()
                SharedPreference.get(this).accessToken = ""
                SharedPreference.get(this).name = ""
                SharedPreference.get(this).email = ""
                SharedPreference.get(this).userId = ""
            })
        }
        // Observe network for automatic future sync
        dbSyncHandler.observeNetworkAndSync(this)
    }


    @Composable
    private fun LocationTracker(viewModel: AuthViewModel) {
        val context = this@MainActivity
        var location by remember { mutableStateOf<Pair<Double, Double>?>(null) }

        LaunchedEffect(Unit) {
            val dbHelper = DatabaseHelper(context)

            while (true) {
                dbHelper.selectPositionAsync(object : DatabaseHelper.DatabaseHandler<Position?> {
                    override fun onComplete(success: Boolean, result: Position?) {
                        if (success && result != null) {
                            location = Pair(result.latitude, result.longitude)
                            SharedPreference.get(context).latitude = result.latitude.toString()
                            SharedPreference.get(context).longitude = result.longitude.toString()


                            val model = LocationTrackRequest(
                                SharedPreference.get(context).userId,
                                0,
                                SharedPreference.get(context).latitude,
                                SharedPreference.get(context).longitude,
                                0)
                            viewModel.hitLocationTrack(returnAccessToken(context), model)
                        } else {
                            Log.w("TAG", "Location not found or failed.")
                        }
                    }
                })
                delay(60_000) // every 1 minute
            }
        }
    }

    fun visibleStatusBar(activity: ComponentActivity) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(false)
            activity.window.statusBarColor = Color.Transparent.toArgb()
            activity.window.navigationBarColor = Color.Transparent.toArgb()
            val controller =
                WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            controller?.isAppearanceLightStatusBars = true
        } else {
            @Suppress("DEPRECATION") activity.window.decorView.systemUiVisibility =
                activity.window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            @Suppress("DEPRECATION") activity.window.statusBarColor = Color.Transparent.toArgb()
        }
    }

    fun statusBar(activity: ComponentActivity) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(false)
            activity.window.statusBarColor = Color.Transparent.toArgb()
            activity.window.navigationBarColor = Color.Transparent.toArgb()

            val controller =
                WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            controller.isAppearanceLightStatusBars = true
        } else {
            @Suppress("DEPRECATION") activity.window.decorView.systemUiVisibility =
                activity.window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

            @Suppress("DEPRECATION") activity.window.statusBarColor = Color.Transparent.toArgb()
        }
    }

    //broadcast action
    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val needToRefresh = intent?.getStringExtra("NeedToRefresh") ?: ""
            if (needToRefresh == "1") {
                refreshDashboardData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(refreshReceiver, IntentFilter("com.pay10.REFRESH_ACTION"))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(refreshReceiver)
    }

    private fun refreshDashboardData() {
        Log.d("MainActivity", "Dashboard refreshed after notification")
        setupSync()
    }
}


