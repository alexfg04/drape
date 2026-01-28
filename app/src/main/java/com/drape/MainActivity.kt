package com.drape

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.drape.navigation.DrapeNavGraph
import com.drape.ui.components.CurvedBottomNavigation

import com.drape.ui.rememberDrapeAppState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drape.navigation.UploadClothes
import com.drape.ui.theme.DrapeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Let the app draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Hide System Status Bar (True Immersive Mode)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        
        enableEdgeToEdge()
        setContent {
            DrapeTheme {
                DrapeApp()
            }
        }
    }
}

/**
 * Main app composable following Google's recommended architecture.
 * 
 * The bottom navigation bar is managed at app level, not within individual screens.
 * This ensures consistent behavior and avoids duplicated Scaffolds.
 */
@Composable
fun DrapeApp() {
    val appState = rememberDrapeAppState()
    
    // Read composable state values before using them
    val shouldShowBottomBar = appState.shouldShowBottomBar
    val currentIndex = appState.currentBottomNavIndex

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show bottom bar when in HomeGraph destinations
            if (shouldShowBottomBar) {
                CurvedBottomNavigation(
                    items = appState.topLevelDestinations,
                    selectedIndex = currentIndex,
                    onItemSelected = { index ->
                        appState.navigateToBottomBarDestination(index)
                    }
                )
            }
        },
        floatingActionButton = {
            if (appState.shouldShowFab) {
                FloatingActionButton(
                    onClick = { appState.navController.navigate(UploadClothes) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_item))
                }
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        DrapeNavGraph(
            navController = appState.navController,
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        )
    }
}
