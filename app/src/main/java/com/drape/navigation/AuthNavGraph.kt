package com.drape.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.drape.ui.components.DrapeNavigationItem
import com.drape.ui.home.AddScreen
import com.drape.ui.home.CamerinoScreen
import com.drape.ui.home.HomeScreen
import com.drape.ui.home.ProfileScreen
import com.drape.ui.login.SceltaLogScreen
import com.drape.ui.signin.SignInScreen
import com.drape.ui.signup.EmailSignUpScreen
import com.drape.ui.welcome.WelcomeScreen

/**
 * Lista degli elementi per la barra di navigazione inferiore.
 * Centralizza la configurazione di icone, titoli e rotte.
 */
val BottomNavItems = listOf(
    DrapeNavigationItem(
        title = "Home",
        icon = Icons.Default.Home,
        route = Home
    ),
    DrapeNavigationItem(
        title = "Componi",
        icon = Icons.Default.Face,
        route = Componi
    ),
    DrapeNavigationItem(
        title = "Aggiungi",
        icon = Icons.Default.Add,
        route = Aggiungi
    ),
    DrapeNavigationItem(
        title = "Profilo",
        icon = Icons.Default.Person,
        route = Profilo
    )
)

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
            HomeScreen(navController = navController)
        }
        composable<Componi> {
            CamerinoScreen(navController = navController)
        }
        composable<Aggiungi> {
            AddScreen(navController = navController)
        }
        composable<Profilo> {
            ProfileScreen(navController = navController)
        }
    }
}
