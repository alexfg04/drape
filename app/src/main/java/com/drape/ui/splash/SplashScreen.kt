package com.drape.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.drape.R
import com.drape.ui.theme.DrapeTheme
import kotlinx.coroutines.delay

/**
 * Initial splash screen of the app.
 *
 * Displays the logo briefly, then checks authentication state
 * and automatically navigates to the appropriate destination.
 *
 * @param modifier optional layout modifier
 * @param viewModel ViewModel providing authentication state
 * @param onNavigateToWelcome callback invoked if user is not authenticated
 * @param onNavigateToHome callback invoked if user is already authenticated
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToWelcome: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(1500) // Breve delay per mostrare lo splash
        if (viewModel.isUserLoggedIn) {
            onNavigateToHome()
        } else {
            onNavigateToWelcome()
        }
    }

    SplashScreenContent(modifier = modifier)
}

@Composable
private fun SplashScreenContent(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(R.string.welcome_logo_description),
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    DrapeTheme {
        SplashScreenContent()
    }
}
