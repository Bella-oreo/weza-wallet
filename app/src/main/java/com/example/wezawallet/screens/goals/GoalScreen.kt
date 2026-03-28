package com.example.wezawallet.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wezawallet.usermodel.TransactionRecord
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(onBack: () -> Unit = {}) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var amount by remember { mutableStateOf("") }
    var goalName by remember { mutableStateOf("") }
    var goalHistory by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var isSaving by remember { mutableStateOf(false) }

    // IMPROVEMENT 6: Specific "Goal" activity history
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("transactions")
                .whereEqualTo("type", "Goal")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener { snapshot, _ ->
                    goalHistory = snapshot?.documents?.mapNotNull {
                        it.toObject(TransactionRecord::class.java)
                    } ?: emptyList()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings Goals", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(16.dp)
        ) {
            // IMPROVEMENT 4: Gold Branding for Goals
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB800).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "What are you saving for?",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFB8860B) // Darker gold for text readability
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = goalName,
                        onValueChange = { goalName = it },
                        label = { Text("Goal Name") },
                        placeholder = { Text("e.g. New Laptop, Vacation") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isSaving
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                        label = { Text("Contribution Amount") },
                        prefix = { Text("KES ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isSaving,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB800),
                            focusedLabelColor = Color(0xFFB8860B)
                        )
                    )
                }
            }

            Button(
                onClick = {
                    val valAmount = amount.toDoubleOrNull() ?: 0.0
                    if (userId.isNotEmpty() && valAmount > 0) {
                        isSaving = true
                        val record = TransactionRecord(
                            title = "Goal: ${if (goalName.isEmpty()) "Savings" else goalName}",
                            amount = valAmount,
                            type = "Goal",
                            isNegative = true, // It deducts from wallet to move to "Goals"
                            timestamp = Timestamp.now()
                        )

                        db.collection("users").document(userId).collection("transactions").add(record)
                        db.collection("users").document(userId)
                            .update("balance", FieldValue.increment(-valAmount))
                            .addOnCompleteListener {
                                isSaving = false
                                amount = ""; goalName = ""
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = amount.isNotEmpty() && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB800)), // Gold Button
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.Star, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Toward Goal", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // RECENT GOAL CONTRIBUTIONS (Point 6)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recent Contributions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (goalHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No goal activity yet.", color = Color.Gray)
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(goalHistory) { tx ->
                    val dateStr = tx.timestamp?.let {
                        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(it.toDate())
                    } ?: ""

                    ListItem(
                        headlineContent = { Text(tx.title, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(dateStr) },
                        trailingContent = {
                            Text(
                                "KES ${String.format("%,.0f", tx.amount)}",
                                color = Color(0xFFFFB800),
                                fontWeight = FontWeight.ExtraBold
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GoalPreview() {
    MaterialTheme { GoalScreen() }
}