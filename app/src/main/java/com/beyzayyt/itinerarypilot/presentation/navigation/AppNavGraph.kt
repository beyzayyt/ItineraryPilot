package com.beyzayyt.itinerarypilot.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.beyzayyt.itinerarypilot.presentation.home.HomeScreen
import com.beyzayyt.itinerarypilot.presentation.itinerary.ItineraryScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToItinerary = { navController.navigate(ItineraryRoute) }
            )
        }
        composable<ItineraryRoute> {
            ItineraryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
