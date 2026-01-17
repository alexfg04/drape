package com.drape.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drape.R
import com.drape.ui.theme.DrapeTheme

@Composable
fun EmailSignUpScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val localCustomGreen = Color(0xFF0F6D46)
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isEmailValid = email.contains("@")
    val isPasswordValid = password.length >= 8 &&
            password.any { it.isDigit() } &&
            password.any { !it.isLetterOrDigit() } &&
            password.any { it.isUpperCase() }
    val isFormValid = isEmailValid && name.isNotEmpty() && isPasswordValid

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
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Usa la tua email.",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome Completo") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = localCustomGreen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = localCustomGreen,
                    cursorColor = localCustomGreen
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Indirizzo Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = email.isNotEmpty() && !isEmailValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = localCustomGreen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = localCustomGreen,
                    cursorColor = localCustomGreen,
                    errorBorderColor = Color.Red,
                    errorLabelColor = Color.Red
                ),
                singleLine = true,
                supportingText = {
                    if (email.isNotEmpty() && !isEmailValid) {
                        Text("Inserisci un'email valida (deve contenere @)")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Crea Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = password.isNotEmpty() && !isPasswordValid,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = localCustomGreen,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = localCustomGreen,
                    cursorColor = localCustomGreen,
                    errorBorderColor = Color.Red,
                    errorLabelColor = Color.Red
                ),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                            ),
                            contentDescription = if (isPasswordVisible) "Nascondi password" else "Mostra password",
                            tint = Color.Gray
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Min. 8 caratteri, un numero, un simbolo e una maiuscola",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = if (password.isNotEmpty() && !isPasswordValid) Color.Red else Color.Gray
                ),
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = rememberMe,
                    onClick = { rememberMe = !rememberMe },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = localCustomGreen,
                        unselectedColor = Color.LightGray
                    )
                )
                Text(
                    text = "Ricordami",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = localCustomGreen,
                    disabledContainerColor = localCustomGreen.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Crea Account",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun EmailSignUpPreview() {
    DrapeTheme {
        EmailSignUpScreen()
    }
}
