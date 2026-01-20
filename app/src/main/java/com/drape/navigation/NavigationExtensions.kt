package com.drape.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Handles bottom bar navigation in a centralized way.
 * 
 * @deprecated Use [com.drape.ui.DrapeAppState.navigateToBottomBarDestination] instead.
 * This function is kept for backwards compatibility.
 * 
 * @param index The index of the selected item.
 * @param currentIndex The index of the current page.
 */
@Deprecated(
    message = "Use DrapeAppState.navigateToBottomBarDestination() instead",
    replaceWith = ReplaceWith(
        "appState.navigateToBottomBarDestination(index)",
        "com.drape.ui.DrapeAppState"
    )
)
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
