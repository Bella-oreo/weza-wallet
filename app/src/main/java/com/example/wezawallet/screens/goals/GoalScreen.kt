package com.example.wezawallet.screens.goals

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

    // POINT 4: Gold Branding Colors
    val goldPrimary = Color(0xFFFFB800)
    val goldDark = Color(0xFFB8860B)

    var amount by remember { mutableStateOf("") }
    var goalName by remember { mutableStateOf("") }
    var goalHistory by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var isSaving by remember { mutableStateOf(false) }

    // POINT 6: Real-time listener for "Goal" transactions
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("transactions")
                .whereEqualTo("type", "Goal")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
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
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = goldPrimary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "What are you saving for?",
                        color = goldDark,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = goalName,
                        onValueChange = { goalName = it },
                        label = { Text("Goal Name") },
                        placeholder = { Text("e.g. New Laptop") },
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
                            focusedBorderColor = goldPrimary,
                            focusedLabelColor = goldDark
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
                            title = if (goalName.isEmpty()) "Savings" else "Goal: $goalName",
                            amount = valAmount,
                            type = "Goal",
                            isNegative = true,
                            timestamp = Timestamp.now()
                        )

                        db.collection("users").document(userId).collection("transactions").add(record)
                            .addOnSuccessListener {
                                db.collection("users").document(userId)
                                    .update("balance", FieldValue.increment(-valAmount))
                                    .addOnSuccessListener {
                                        isSaving = false
                                        amount = ""; goalName = ""
                                    }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = amount.isNotEmpty() && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = goldPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.Star, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Toward Goal", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recent Goal Activity", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (goalHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No goal activity yet.", color = Color.Gray)
                }
            } else {
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
                                    color = goldDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

// --- PREVIEW SECTION ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GoalScreenPreview() {
    // We wrap it in a Surface to ensure the background is white in the preview
    Surface(color = MaterialTheme.colorScheme.background) {
        GoalScreen()
    }
}