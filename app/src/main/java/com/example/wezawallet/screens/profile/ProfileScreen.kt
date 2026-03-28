package com.example.wezawallet.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onHistoryClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    // State for user data
    var name by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var phone by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // 1. PERSISTENCE: Fetch data from Firestore when the screen loads
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = document.getString("name") ?: "No Name Set"
                        phone = document.getString("phone") ?: ""
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(email, color = Color.Gray)
                if (phone.isNotEmpty()) Text(phone, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (isEditing) {
                        // 2. PERSISTENCE: Save changes back to Firestore
                        val updates = mapOf("name" to name, "phone" to phone)
                        db.collection("users").document(userId).update(updates)
                    }
                    isEditing = !isEditing
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Save Profile" else "Edit Profile")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onHistoryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transaction History")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fixed the deprecated Help Icon warning from your screenshot
            OutlinedButton(
                onClick = { /* Support Logic */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Help")
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = {
                    auth.signOut()
                    onLogoutClick()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    // Wrapping in MaterialTheme ensures the colors and fonts match your app
    MaterialTheme {
        // We pass empty lambdas {} for the navigation clicks in the preview
        ProfileScreen(
            onHistoryClick = {},
            onLogoutClick = {}
        )
    }
}