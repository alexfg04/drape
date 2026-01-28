package com.drape.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.drape.ui.wardrobe.WardrobeScreen
import com.drape.ui.home.HomeScreen
import com.drape.ui.outfit_creator.OutfitCreatorScreen
import com.drape.ui.profile.ProfileScreen
import com.drape.ui.upload_clothes.UploadItemScreen

/**
 * Home navigation graph.
 * Contains all screens accessible from the bottom navigation bar.
 *
 * Note: Bottom navigation is handled at app level (MainActivity),
 * so individual screens don't need NavHostController for bottom bar navigation.
 */
fun NavGraphBuilder.homeNavGraph(
    navController: NavHostController
) {
    navigation<HomeGraph>(startDestination = Home) {
        composable<Home> {
            HomeScreen()
        }

        composable<Camerino> {
            OutfitCreatorScreen(
                onBackClick = { 
                    if (!navController.popBackStack()) {
                        navController.navigate(Home) {
                            popUpTo(HomeGraph) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable<Wardrobe> {
            WardrobeScreen()
        }

        composable<UploadClothes> {
            UploadItemScreen(
                onBackClick = { 
                    if (!navController.popBackStack()) {
                        navController.navigate(Home) {
                            popUpTo(HomeGraph) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable<Profile> {
            ProfileScreen()
        }
    }
}
