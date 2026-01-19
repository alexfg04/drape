package com.drape.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drape.R
import com.drape.ui.theme.DrapeTheme

@Composable
fun SceltaLogScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onEmailSignUpClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            onNavigateToHome()
        }
    }

    SceltaLogScreenContent(
        modifier = modifier,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onBackClick = onBackClick,
        onEmailSignUpClick = onEmailSignUpClick,
        onGoogleSignInClick = onGoogleSignInClick
    )
}

@Composable
private fun SceltaLogScreenContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onBackClick: () -> Unit = {},
    onEmailSignUpClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = stringResource(R.string.back_description),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                ),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            SceltaLogSocialButton(
                text = stringResource(R.string.login_continue_google),
                iconRes = R.drawable.ic_google,
                enabled = !isLoading,
                onClick = onGoogleSignInClick
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = stringResource(R.string.login_or_divider),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onEmailSignUpClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login_email_signup),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            SceltaLogLegalFooter(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SceltaLogSocialButton(
    iconRes: Int,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun SceltaLogLegalFooter(modifier: Modifier = Modifier) {
    val prefix = stringResource(R.string.login_legal_prefix)
    val terms = stringResource(R.string.login_terms_of_service)
    val and = stringResource(R.string.login_legal_and)
    val privacy = stringResource(R.string.login_privacy_policy)
    val suffix = stringResource(R.string.login_legal_suffix)
    
    val annotatedString = buildAnnotatedString {
        append(prefix)
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append(terms)
        }
        append(and)
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append(privacy)
        }
        append(suffix)
    }
    Text(
        text = annotatedString,
        modifier = modifier,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SceltaLogScreenPreview() {
    DrapeTheme {
        SceltaLogScreenContent()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SceltaLogSocialButtonPreview() {
    DrapeTheme {
        SceltaLogSocialButton(
            iconRes = R.drawable.ic_google,
            text = "Continua con Google", // Preview-only, uses stringResource in production
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SceltaLogLegalFooterPreview() {
    DrapeTheme {
        SceltaLogLegalFooter(modifier = Modifier.padding(16.dp))
    }
}
