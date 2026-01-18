package com.drape.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.drape.control.AccountService
import com.drape.screen.EmailSignUpScreen
import com.drape.screen.HomeScreen
import com.drape.screen.SceltaLogScreen
import com.drape.screen.SignInScreen
import com.drape.screen.WelcomeScreen

@Composable
fun DrapeNavHost(
    navController: NavHostController,
    accountService: AccountService,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = modifier
    ) {
        composable(route = "welcome") {
            WelcomeScreen(
                onStartClick = { navController.navigate("sceltaLog") },
                onSignInClick = { navController.navigate("signIn") }
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
                accountService = accountService,
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
        composable(route = "signIn") {
            SignInScreen(
                accountService = accountService,
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
        composable(route = "home") {
            HomeScreen(
                onNavigateToProfile = { /* Naviga al profilo */ }
            )
        }
    }
}
