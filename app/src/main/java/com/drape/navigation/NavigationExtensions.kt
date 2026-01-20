package com.drape.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Handles bottom bar navigation in a centralized way.
 * Avoids duplication of navigation logic in each screen.
 * 
 * @param index The index of the selected item.
 * @param currentIndex The index of the current page.
 */
fun NavHostController.navigateToBottomBarItem(index: Int, currentIndex: Int) {
    if (index != currentIndex) {
        val route = BottomNavItems[index].route
        this.navigate(route) {
            // Go back to the start destination to avoid stack accumulation
            popUpTo(this@navigateToBottomBarItem.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination
            launchSingleTop = true
            // Restore previous state
            restoreState = true
        }
    }
}
