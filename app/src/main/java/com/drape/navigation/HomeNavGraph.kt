package com.drape.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.drape.ui.home.AddScreen
import com.drape.ui.home.CamerinoScreen
import com.drape.ui.home.HomeScreen
import com.drape.ui.home.ProfileScreen

/**
 * Home navigation graph.
 * Contains all screens accessible from the bottom navigation bar.
 */
fun NavGraphBuilder.homeNavGraph(
    navController: NavHostController
) {
    navigation<HomeGraph>(startDestination = Home) {
        composable<Home> {
            HomeScreen(navController = navController)
        }

        composable<Camerino> {
            CamerinoScreen(navController = navController)
        }

        composable<Add> {
            AddScreen(navController = navController)
        }

        composable<Profile> {
            ProfileScreen(navController = navController)
        }
    }
}
