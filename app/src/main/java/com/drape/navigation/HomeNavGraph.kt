package com.drape.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.drape.ui.wardrobe.WardrobeScreen
import com.drape.ui.home.HomeScreen
import com.drape.ui.outfit_creator.OutfitCreatorScreen
import com.drape.ui.profile.ProfileScreen
import com.drape.ui.upload_clothes.UploadItemScreen
import com.drape.ui.my_outfit.SavedOutfitsScreen
import com.drape.ui.profile.season.ProfileSeasonOutfitsScreen
import com.drape.ui.planner.PlannerScreen
import com.drape.ui.clothing_detail.ClothingItemDetailScreen
import com.drape.ui.planner.SelectOutfitScreen
import com.drape.ui.planner.SelectOutfitViewModel
import com.drape.ui.statistics.StatisticsScreen

/**
 * Home navigation graph.
 * Contains all screens accessible from the bottom navigation bar.
 *
 * Note: Bottom navigation is handled at app level (MainActivity),
 * so individual screens don't need NavHostController for bottom bar navigation.
 */
fun NavGraphBuilder.homeNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    navigation<HomeGraph>(startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onNavigateToProfile = { navController.navigate(Profile) },
                onNavigateToStatistics = { navController.navigate(Statistics) },
                onOutfitClick = { outfit ->
                    navController.navigate(EditOutfit(outfitId = outfit.id))
                },
                onClothingItemClick = { item ->
                    navController.navigate(ClothingItemDetail(itemId = item.id))
                },
                onNavigateToPlanner = {
                    navController.navigate(Planner) {
                        popUpTo(HomeGraph) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable<Statistics> {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ClothingItemDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<ClothingItemDetail>()
            ClothingItemDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onItemDeleted = { 
                    navController.popBackStack()
                }
            )
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
                onNavigateToOutfitCreator = {
                    navController.navigate(Camerino())
                },
                onNavigateToProfile = {
                    navController.navigate(Profile)
                },
                onNavigateToClothingDetail = { item ->
                    navController.navigate(ClothingItemDetail(itemId = item.id))
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
                },
                onCreateOutfit = {
                    navController.navigate(Camerino())
                }
            )
        }

        composable<Profile> {
            ProfileScreen(
                onSavedOutfitsClick = {
                    // Navigate to SavedOutfits tab, preserving bottom bar state
                    navController.navigate(SavedOutfits) {
                        popUpTo(HomeGraph) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onWardrobeClick = {
                     // Navigate to Wardrobe tab
                     navController.navigate(Wardrobe) {
                        popUpTo(HomeGraph) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onEditProfileClick = {
                    navController.navigate(EditProfile)
                },
                onSeasonClick = { season ->
                    navController.navigate(ProfileSeasonOutfits(season))
                },
                onBackToHome = {
                    navController.navigate(Home) {
                        popUpTo(HomeGraph) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onLogout = onLogout
            )
        }

        composable<EditProfile> {
            com.drape.ui.profile.edit.EditProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<EmptyPage> {
            com.drape.ui.empty.EmptyScreen()
        }

        composable<ProfileSeasonOutfits> { backStackEntry ->
            val route = backStackEntry.toRoute<ProfileSeasonOutfits>()
            ProfileSeasonOutfitsScreen(
                season = route.season,
                onBackClick = { navController.popBackStack() },
                onNavigateToOutfit = { outfitId ->
                    navController.navigate(EditOutfit(outfitId))
                },
                onCreateOutfit = {
                    navController.navigate(Camerino())
                }
            )
        }

        composable<Planner> {
            PlannerScreen(
                onNavigateToSelectOutfit = { day, month, year ->
                    navController.navigate(SelectOutfit(day, month, year))
                }
            )
        }

        composable<SelectOutfit> { backStackEntry ->
            val route = backStackEntry.toRoute<SelectOutfit>()
            val viewModel: SelectOutfitViewModel = hiltViewModel()
            SelectOutfitScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
