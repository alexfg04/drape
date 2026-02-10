package com.drape.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home

import com.drape.R
import com.drape.ui.components.DrapeNavigationItem

import com.drape.ui.components.IconSource

/**
 * Centralized list of items for the bottom navigation bar.
 */
val BottomNavItems = listOf(
    DrapeNavigationItem(
        titleRes = R.string.home_nav_home,
        icon = IconSource.Vector(Icons.Filled.Home),
        route = Home
    ),
    DrapeNavigationItem(
        titleRes = R.string.home_nav_camerino,
        icon = IconSource.Vector(Icons.Filled.Checkroom),
        route = SavedOutfits
    ),
    DrapeNavigationItem(
        titleRes = R.string.home_nav_wardrobe,
        icon = IconSource.Drawable(R.drawable.apparel_24px),
        route = Wardrobe
    ),
    DrapeNavigationItem(
        titleRes = R.string.home_nav_planner,
        icon = IconSource.Vector(Icons.Filled.CalendarMonth),
        route = Planner
    )
)
