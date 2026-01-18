package com.drape.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.drape.ui.home.HomeScreen
import com.drape.ui.login.SceltaLogScreen
import com.drape.ui.signin.SignInScreen
import com.drape.ui.signup.EmailSignUpScreen
import com.drape.ui.welcome.WelcomeScreen

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
                onStartClick = { navController.navigate("sceltaLog") },
                onSignInClick = { navController.navigate("signIn") }
            )
        }
        composable(route = "sceltaLog") {
            SceltaLogScreen(
                onBackClick = { navController.popBackStack() },
                onEmailSignUpClick = { navController.navigate("signUpEmail") },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onGoogleSignInClick = {
                    // TODO: Implementare Google Sign-In con ActivityResultLauncher
                }
            )
        }
        composable(route = "signUpEmail") {
            EmailSignUpScreen(
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
