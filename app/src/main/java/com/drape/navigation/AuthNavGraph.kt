package com.drape.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.drape.ui.login.SceltaLogScreen
import com.drape.ui.signin.SignInScreen
import com.drape.ui.signup.EmailSignUpScreen
import com.drape.ui.welcome.WelcomeScreen

/**
 * Auth navigation graph.
 * Contains: Welcome, SignIn, SignUp, SceltaLog
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
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
                    // TODO: Implement Google Sign-In logic
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
