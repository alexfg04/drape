package com.drape.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.navigation.navigation
import com.drape.ui.wardrobe.WardrobeScreen
import com.drape.ui.home.HomeScreen
import com.drape.ui.outfit_creator.OutfitCreatorScreen
import com.drape.ui.profile.ProfileScreen
import com.drape.ui.upload_clothes.UploadItemScreen
import com.drape.ui.myOutfit.SavedOutfitsScreen

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

        composable<Camerino> { backStackEntry ->
            // Standard Camerino entry (Bottom Bar) - usually new outfit
            val camerinoRoute = backStackEntry.toRoute<Camerino>()
            OutfitCreatorScreen(
                outfitId = camerinoRoute.outfitId,
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

        composable<EditOutfit> { backStackEntry ->
            val route = backStackEntry.toRoute<EditOutfit>()
            OutfitCreatorScreen(
                outfitId = route.outfitId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Wardrobe> {
            WardrobeScreen(
                onNavigateToOutfits = {
                    navController.navigate(SavedOutfits)
                },
                onEditOutfit = { outfit ->
                    navController.navigate(EditOutfit(outfitId = outfit.id))
                }
            )
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

        composable<SavedOutfits> {
            SavedOutfitsScreen(
                onEditOutfit = { outfit ->
                    navController.navigate(EditOutfit(outfitId = outfit.id))
                }
            )
        }

        composable<Profile> {
            ProfileScreen(
                onSavedOutfitsClick = {
                    navController.navigate(SavedOutfits)
                }
            )
        }
    }
}
