package com.drape.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.drape.screen.EmailSignUpScreen
import com.drape.screen.SceltaLogScreen
import com.drape.screen.WelcomeScreen

@Composable
fun DrapeNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = modifier
    ) {
        composable(route = "welcome") {
            WelcomeScreen(
                onStartClick = { navController.navigate("sceltaLog") }
            )
        }
        composable(route = "sceltaLog") {
            SceltaLogScreen(
                onBackClick = { navController.popBackStack() },
                onEmailSignUpClick = { navController.navigate("signUpEmail") }
            )
        }
        composable(route = "signUpEmail") {
            EmailSignUpScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
