package com.drape.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import com.drape.R
import com.drape.ui.components.DrapeNavigationItem

/**
 * Centralized list of items for the bottom navigation bar.
 */
val BottomNavItems = listOf(
    DrapeNavigationItem(
        titleRes = R.string.home_nav_home,
        icon = Icons.Default.Home,
        route = Home
    ),
    DrapeNavigationItem(
        titleRes = R.string.home_nav_camerino,
        icon = Icons.Default.Face,
        route = Camerino
    ),
    DrapeNavigationItem(
        titleRes = R.string.home_nav_add,
        icon = Icons.Default.Add,
        route = Add
    ),
    DrapeNavigationItem(
        titleRes = R.string.home_nav_profile,
        icon = Icons.Default.Person,
        route = Profile
    )
)
