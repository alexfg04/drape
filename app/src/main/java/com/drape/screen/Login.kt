package com.drape.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drape.R
import com.drape.ui.theme.DrapeTheme

@Composable
fun SceltaLogScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onEmailSignUpClick: () -> Unit = {}
) {
    val localCustomGreen = Color(0xFF3F51B5)
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
                    contentDescription = null,
                    tint = Color.Black
                )
            }
        },
        containerColor = Color.White
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
                text = "Crea il tuo profilo.",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.Black
                ),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Scegli un metodo di accesso adatto al tuo profilo qui sotto.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Gray,
                    fontSize = 16.sp
                ),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(32.dp))
            SceltaLogSocialButton(
                text = "Continua con Google",
                iconRes = R.drawable.ic_google
            )
            Spacer(modifier = Modifier.height(16.dp))
            SceltaLogSocialButton(
                text = "Continua con Apple",
                iconRes = R.drawable.ic_apple
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
                Text(
                    text = "oppure",
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 14.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onEmailSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = localCustomGreen),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Iscriviti con l'Email",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
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
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun SceltaLogLegalFooter(modifier: Modifier = Modifier) {
    val annotatedString = buildAnnotatedString {
        append("Continuando, accetti i nostri ")
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append("Termini di Servizio")
        }
        append(" e la ")
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append("Privacy Policy")
        }
        append(".")
    }
    Text(
        text = annotatedString,
        modifier = modifier,
        fontSize = 12.sp,
        color = Color.Gray,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SceltaLogScreenPreview() {
    DrapeTheme {
        SceltaLogScreen()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun SceltaLogSocialButtonPreview() {
    DrapeTheme {
        SceltaLogSocialButton(
            iconRes = R.drawable.ic_google,
            text = "Continua con Google",
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
