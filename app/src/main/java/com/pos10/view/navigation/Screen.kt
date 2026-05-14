package com.pos10.view.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.pos10.view.screens.SplashScreen
import com.pos10.di.AuthEvent
import com.pos10.di.AuthEventManager
import com.pos10.domain.AuthViewModel
import com.pos10.helper.SharedPreference
import com.pos10.view.FullWorkOrderDetail
import com.pos10.view.SelectedRequestHolder
import com.pos10.view.SingletonObject
import com.pos10.view.UploadDocumentsScreen
import com.pos10.view.screens.ActualRouteMapScreen
import com.pos10.view.screens.ComplaintDetailScreen
import com.pos10.view.screens.DashboardScreen
import com.pos10.view.screens.FeedbackScreen
import com.pos10.view.screens.InstallationDeviceDetails
import com.pos10.view.screens.JobUploadDocScren
import com.pos10.view.screens.JobsScreen
import com.pos10.view.screens.LoginScreen
import com.pos10.view.screens.PaperRollConfirmationScreen
import com.pos10.view.screens.PaperRollDetailScreen
import com.pos10.view.screens.ReplacementDetailScreen
import com.pos10.view.screens.ReturnDeviceDetails
import com.pos10.view.screens.RouteTrackHistory

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object LoginScreen : Screen("LoginScreen")
    object DashboardScreen:Screen("Dashboard")
    object JobsScreen:Screen("JobsScreen")
    object JobInstallationDetailScreen:Screen("JobInstallationDetailScreen")
    object JobUploadScreen:Screen("JobUploadScreen")
    object ComplaintScreen:Screen("ComplaintScreen")
    object PaperRollScreen:Screen("PaperRollScreen")
    object PaperRollConfirmationCodeScreen:Screen("PaperCodeScreen")
    object ActualRoute:Screen("ActualRouteMapScreen")
    object RouteTrackHistry:Screen("RouteTrackScreen")
    object FeedbackScreen:Screen("FeedbackScreen")
    object UploadDocumentsScreen:Screen("UploadDocumentsScreen")
    object FullWorkOrderDetail:Screen("FullWorkOrderDetail")

    // New screens for Return & Replacement
    object ReturnDeviceDetails : Screen("ReturnDeviceDetails")
    object ReplacementDetailScreen : Screen("ReplacementDetailScreen")
}

@Composable
fun AppNavigation(navController: NavHostController= rememberNavController(),authViewModel: AuthViewModel,authEventManager: AuthEventManager,context: Context) {
    LaunchedEffect(Unit) {
        authEventManager.events.collect { event ->
            when (event) {
                AuthEvent.Unauthorized -> {
                    SelectedRequestHolder.clearRequest()
                    SingletonObject.clear()
                    SharedPreference.get(context).accessToken = ""
                    SharedPreference.get(context).name = ""
                    SharedPreference.get(context).email = ""
                    SharedPreference.get(context).userId = ""
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.LoginScreen.route) { LoginScreen(navController) }
        composable(Screen.JobInstallationDetailScreen.route) { InstallationDeviceDetails(navController) }
        composable(Screen.JobUploadScreen.route){ JobUploadDocScren(navController) }
        composable(Screen.DashboardScreen.route) { DashboardScreen(navController) }
        composable(Screen.ComplaintScreen.route){ ComplaintDetailScreen(navController) }
        composable(Screen.PaperRollScreen.route){ PaperRollDetailScreen(navController) }
        composable(Screen.RouteTrackHistry.route){ RouteTrackHistory(navController) }
        composable(Screen.FeedbackScreen.route){ FeedbackScreen(navController) }
        composable(Screen.UploadDocumentsScreen.route){ UploadDocumentsScreen(navController) }
        composable(Screen.FullWorkOrderDetail.route) { FullWorkOrderDetail(navController)  }
        composable(Screen.ActualRoute.route){
            ActualRouteMapScreen(navController)
        }
        composable(Screen.PaperRollConfirmationCodeScreen.route){ PaperRollConfirmationScreen(navController) }

        // New composables
        composable(Screen.ReturnDeviceDetails.route) { ReturnDeviceDetails(navController) }
        composable(Screen.ReplacementDetailScreen.route) { ReplacementDetailScreen(navController) }

        navigation(
            startDestination = Screen.JobsScreen.route,
            route = Screen.DashboardScreen.route
        ) {
            composable(Screen.JobsScreen.route) { JobsScreen(navController) }
            composable(Screen.JobsScreen.route) { JobsScreen(navController) }
            composable(Screen.JobsScreen.route) { JobsScreen(navController) }
        }
    }
}