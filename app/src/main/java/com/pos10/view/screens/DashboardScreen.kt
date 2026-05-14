package com.pos10.view.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pos10.R
import com.pos10.view.MainActivity

@Composable
fun DashboardScreen(dashboardNavController: NavHostController) {
   // val dashboardNavController = rememberNavController()

    val navBackStackEntry by dashboardNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

     (context as MainActivity).visibleStatusBar(context)

    BackHandler {
        (context as MainActivity).finish()
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(dashboardNavController, currentRoute)
        }
    ) { innerPadding ->
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar {
        Screen.bottomNavItems.forEach { screen ->
            val selected = screen.route == currentRoute
            NavigationBarItem(
                icon = {
                    Image(
                        painter = painterResource(
                            id = if (selected) screen.selectedIcon else screen.unselectedIcon
                        ),
                        contentDescription = screen.title
                    )
                },
                label = {
                    Text(
                        screen.title,
                        color = if (selected) Color(0xFFff6900) else Color(0xFF000000)
                    )
                },
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Jobs.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: Int,
    val unselectedIcon: Int
) {
    object Jobs : Screen("jobs", "Jobs", R.drawable.ic_job_orange, R.drawable.ic_job_grey)
    object Routes : Screen("routes", "Routes", R.drawable.ic_route_orange, R.drawable.ic_route_grey)
    object Profile : Screen("profile", "Profile", R.drawable.ic_job_orange, R.drawable.ic_job_grey)

    companion object {
        val bottomNavItems = listOf(Jobs, Routes, Profile)
    }
}
