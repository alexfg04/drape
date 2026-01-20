package com.drape.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.drape.navigation.BottomNavItems
import com.drape.ui.components.CurvedBottomNavigation

@Composable
fun ProfileScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = {
            CurvedBottomNavigation(
                items = BottomNavItems,
                selectedIndex = 3, // Index for "Profile"
                onItemSelected = { index ->
                    if (index != 3) {
                        navController.navigate(BottomNavItems[index].route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Profile Page",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00458D)
            )
        }
    }
}
