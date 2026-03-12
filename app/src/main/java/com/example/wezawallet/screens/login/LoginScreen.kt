package com.example.wezawallet.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    onGoogleClick: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(50.dp)
                    )
            ) {
                ToggleButton(
                    text = "Login",
                    selected = isLogin,
                    onClick = { isLogin = true; errorMessage = null },
                    modifier = Modifier.weight(1f)
                )

                ToggleButton(
                    text = "Sign Up",
                    selected = !isLogin,
                    onClick = { isLogin = false; errorMessage = null },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (!isLogin) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            errorMessage?.let {
                Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isLogin) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener { onLoginSuccess() }
                            .addOnFailureListener { errorMessage = it.localizedMessage }
                    } else {
                        if (password == confirmPassword) {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener { onSignUpSuccess() }
                                .addOnFailureListener { errorMessage = it.localizedMessage }
                        } else {
                            errorMessage = "Passwords do not match"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(50.dp),
                enabled = email.isNotEmpty() && password.isNotEmpty()
            ) {
                Text(if (isLogin) "Login" else "Sign Up")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Or continue with", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onGoogleClick() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text("Continue with Google")
            }
        }
    }
}

@Composable
private fun ToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) Color(0xFF673AB7) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

// THE PREVIEW COMPOSABLE
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}