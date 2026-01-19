package com.drape.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.drape.ui.login.SceltaLogScreen
import com.drape.ui.signin.SignInScreen
import com.drape.ui.signup.EmailSignUpScreen
import com.drape.ui.welcome.WelcomeScreen

/**
 * Authentication navigation graph.
 * Contains: Welcome, SceltaLog, SignIn, SignUpEmail
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    onNavigateToHome: () -> Unit
) {
    navigation<AuthGraph>(startDestination = Welcome) {
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
                onNavigateToHome = onNavigateToHome,
                onGoogleSignInClick = {
                    // TODO: Implementare Google Sign-In con ActivityResultLauncher
                }
            )
        }

        composable<SignUpEmail> {
            EmailSignUpScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = onNavigateToHome
            )
        }

        composable<SignIn> {
            SignInScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = onNavigateToHome
            )
        }
    }
}
