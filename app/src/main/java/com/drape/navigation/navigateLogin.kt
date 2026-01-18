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
        startDestination = Welcome,
        modifier = modifier
    ) {
        composable<Welcome> {
            WelcomeScreen(
                onStartClick = { navController.navigate(SceltaLog) },
                onSignInClick = { navController.navigate(SignIn) }
            )
        }
        composable<SceltaLog> {
            SceltaLogScreen(
                onBackClick = { navController.popBackStack() },
                onEmailSignUpClick = { navController.navigate(SignUpEmail) },
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo<Welcome> { inclusive = true }
                    }
                },
                onGoogleSignInClick = {
                    // TODO: Implementare Google Sign-In con ActivityResultLauncher
                }
            )
        }
        composable<SignUpEmail> {
            EmailSignUpScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo<Welcome> { inclusive = true }
                    }
                }
            )
        }
        composable<SignIn> {
            SignInScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo<Welcome> { inclusive = true }
                    }
                }
            )
        }
        composable<Home> {
            HomeScreen(
                onNavigateToProfile = { /* Naviga al profilo */ }
            )
        }
    }
}
