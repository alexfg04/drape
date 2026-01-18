package com.drape.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.drape.ui.home.HomeScreen

/**
 * Home navigation graph.
 * Contains: Home, Profile (future), Settings (future)
 */
fun NavGraphBuilder.homeNavGraph(
    navController: NavController,
    onLogout: () -> Unit
) {
    navigation<HomeGraph>(startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onNavigateToProfile = { navController.navigate(Profile) }
            )
        }

        composable<Profile> {
            // TODO: ProfileScreen
            // Per ora placeholder, verrà implementato in futuro
        }
    }
}
