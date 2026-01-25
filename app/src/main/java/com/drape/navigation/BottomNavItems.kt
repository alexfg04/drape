package com.drape.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person

import com.drape.R
import com.drape.ui.components.DrapeNavigationItem

import com.drape.ui.components.IconSource

/**
 * Centralized list of items for the bottom navigation bar.
 */
val BottomNavItems = listOf(
    DrapeNavigationItem(
        titleRes = R.string.home_nav_home,
        icon = IconSource.Vector(Icons.Default.Home),
        route = Home
    ),
    DrapeNavigationItem(
        titleRes = R.string.home_nav_camerino,
        icon = IconSource.Vector(Icons.Default.Face),
        route = Camerino
    ),
    DrapeNavigationItem(
        titleRes = R.string.home_nav_wardrobe,
        icon = IconSource.Drawable(R.drawable.apparel_24px),
        route = Wardrobe
    ),
    DrapeNavigationItem(
        titleRes = R.string.home_nav_profile,
        icon = IconSource.Vector(Icons.Default.Person),
        route = Profile
    )
)
