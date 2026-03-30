package com.example.wezawallet.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var name by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var phone by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    var isEditing by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    // Launcher for picking images
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            val ref = storage.reference.child("profile_pics/$userId.jpg")
            ref.putFile(it).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    val url = downloadUri.toString()
                    photoUrl = url
                    db.collection("users").document(userId).update("photoUrl", url)
                    isUploading = false
                }
            }.addOnFailureListener { isUploading = false }
        }
    }

    // POINT 7: Persistence - Fetching details on load
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    name = snapshot.getString("name") ?: "User Name"
                    phone = snapshot.getString("phone") ?: ""
                    photoUrl = snapshot.getString("photoUrl")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Circle
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                }

                if (isUploading) CircularProgressIndicator(modifier = Modifier.size(120.dp))

                Box(modifier = Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.BottomEnd) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.padding(6.dp), tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
            } else {
                Text(name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(email, color = Color.Gray)
                if (phone.isNotEmpty()) Text(phone, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isEditing) db.collection("users").document(userId).update(mapOf("name" to name, "phone" to phone))
                    isEditing = !isEditing
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isEditing) "Save Profile" else "Edit Profile")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.History, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transaction History")
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = {
                    auth.signOut()
                    onLogoutClick()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                // Fixed the deprecation warning by using AutoMirrored version
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme { ProfileScreen() }
}