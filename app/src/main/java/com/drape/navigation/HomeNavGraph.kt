package com.drape.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.drape.ui.home.AddScreen
import com.drape.ui.home.CamerinoScreen
import com.drape.ui.home.HomeScreen
import com.drape.ui.home.ProfileScreen

/**
 * Home navigation graph.
 * Contains: Home, Camerino, Add, Profile
 */
fun NavGraphBuilder.homeNavGraph(
    navController: NavController,
    onLogout: () -> Unit
) {
    navigation<HomeGraph>(startDestination = Home) {
        composable<Home> {
            HomeScreen(navController = navController as androidx.navigation.NavHostController)
        }

        composable<Camerino> {
            CamerinoScreen(navController = navController as androidx.navigation.NavHostController)
        }

        composable<Add> {
            AddScreen(navController = navController as androidx.navigation.NavHostController)
        }

        composable<Profile> {
            ProfileScreen(navController = navController as androidx.navigation.NavHostController)
        }
    }
}
