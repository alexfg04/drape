package com.drape.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.drape.ui.splash.SplashScreen

/**
 * Main navigation graph for the Drape app.
 * Contains Splash screen and nested graphs for Auth and Home features.
 */
@Composable
fun DrapeNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Splash,
        modifier = modifier
    ) {
        // Splash Screen
        composable<Splash> {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(AuthGraph) {
                        popUpTo<Splash> { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(HomeGraph) {
                        popUpTo<Splash> { inclusive = true }
                    }
                }
            )
        }

        // Auth Flow: Welcome, SignIn, SignUp, SceltaLog
        authNavGraph(
            navController = navController,
            onNavigateToHome = {
                navController.navigate(HomeGraph) {
                    popUpTo<AuthGraph> { inclusive = true }
                }
            }
        )

        // Home Flow: Home, Camerino, Add, Profile
        // Note: Bottom navigation is handled at app level (MainActivity)
        homeNavGraph(navController = navController)
    }
}
