package com.drape.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.drape.navigation.BottomNavItems
import com.drape.navigation.HomeGraph
import com.drape.ui.components.DrapeNavigationItem

/**
 * Centralized app state for managing navigation throughout the Drape app.
 * Follows Google's recommended pattern from Now in Android.
 *
 * @param navController The main navigation controller for the app.
 */
@Stable
class DrapeAppState(
    val navController: NavHostController
) {
    /**
     * Current navigation destination.
     */
    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    /**
     * List of top-level navigation items for the bottom bar.
     */
    val topLevelDestinations: List<DrapeNavigationItem> = BottomNavItems

    /**
     * Index of the currently selected bottom navigation item.
     * Returns -1 if not on a bottom nav destination.
     */
    val currentBottomNavIndex: Int
        @Composable get() {
            val destination = currentDestination
            return topLevelDestinations.indexOfFirst { item ->
                destination?.hasRoute(item.route::class) == true
            }
        }

    /**
     * Whether the bottom navigation bar should be visible.
     * Only visible when within the HomeGraph.
     */
    val shouldShowBottomBar: Boolean
        @Composable get() {
            val destination = currentDestination
            return topLevelDestinations.any { item ->
                destination?.hasRoute(item.route::class) == true
            }
        }

    /**
     * Whether the Floating Action Button should be visible.
     * Only visible when the current destination is Wardrobe.
     */
    val shouldShowFab: Boolean
        @Composable get() {
            val destination = currentDestination
            return destination?.hasRoute(com.drape.navigation.Wardrobe::class) == true
        }

    /**
     * Navigate to a bottom bar destination.
     * Handles proper back stack management.
     *
     * @param index The index of the destination in BottomNavItems.
     */
    fun navigateToBottomBarDestination(index: Int) {
        if (index in topLevelDestinations.indices) {
            val route = topLevelDestinations[index].route
            navController.navigate(route) {
                // Pop up to the HomeGraph to avoid stack accumulation
                popUpTo(HomeGraph) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination
                launchSingleTop = true
                // Restore previous state when re-selecting a tab
                restoreState = true
            }
        }
    }
}

/**
 * Remember and create a [DrapeAppState] instance.
 */
@Composable
fun rememberDrapeAppState(
    navController: NavHostController = rememberNavController()
): DrapeAppState {
    return remember(navController) {
        DrapeAppState(navController)
    }
}
