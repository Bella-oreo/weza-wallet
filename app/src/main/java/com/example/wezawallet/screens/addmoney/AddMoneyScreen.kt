package com.example.wezawallet.screens.addmoney

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
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
fun AddMoneyScreen(onBack: () -> Unit = {}) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    // POINT 3: Money In Branding (Green Accent)
    val depositGreen = Color(0xFF27AE60)

    var amount by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var recentTopUps by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }

    // POINT 6: Fetch recent "Add" transactions for this specific screen
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("transactions")
                .whereEqualTo("type", "Add")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(8)
                .addSnapshotListener { snapshot, _ ->
                    recentTopUps = snapshot?.documents?.mapNotNull {
                        it.toObject(TransactionRecord::class.java)
                    } ?: emptyList()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Top Up Wallet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            // Money In Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = depositGreen.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Enter Amount to Deposit",
                        style = MaterialTheme.typography.labelLarge,
                        color = depositGreen,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. 1000") },
                        prefix = { Text("KES ") },
                        singleLine = true,
                        enabled = !isSubmitting,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = depositGreen,
                            focusedLabelColor = depositGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // POINT 3: Green "Confirm" Button
            Button(
                onClick = {
                    val value = amount.toDoubleOrNull() ?: 0.0
                    if (userId.isNotEmpty() && value > 0) {
                        isSubmitting = true
                        val record = TransactionRecord(
                            title = "Wallet Top-up",
                            amount = value,
                            type = "Add",
                            isNegative = false,
                            timestamp = Timestamp.now()
                        )

                        // Atomic Update
                        db.collection("users").document(userId)
                            .update("balance", FieldValue.increment(value))

                        db.collection("users").document(userId)
                            .collection("transactions")
                            .add(record)
                            .addOnSuccessListener {
                                isSubmitting = false
                                amount = ""
                            }
                            .addOnFailureListener { isSubmitting = false }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = amount.isNotEmpty() && amount != "0" && !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = depositGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Top Up", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // RECENT TOP-UPS SECTION
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Top-up History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (recentTopUps.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No recent top-ups found.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(recentTopUps) { tx ->
                        val dateStr = tx.timestamp?.let {
                            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(it.toDate())
                        } ?: ""

                        ListItem(
                            headlineContent = { Text(tx.title, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text(dateStr) },
                            trailingContent = {
                                Text(
                                    "+KES ${String.format("%,.0f", tx.amount)}",
                                    color = depositGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
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
}

// --- PREVIEW SECTION ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddMoneyPreview() {
    Surface(color = MaterialTheme.colorScheme.background) {
        AddMoneyScreen()
    }
}