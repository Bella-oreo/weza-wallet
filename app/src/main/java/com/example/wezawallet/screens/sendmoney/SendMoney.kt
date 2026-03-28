package com.example.wezawallet.screens.sendmoney

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun SendMoneyScreen(onBack: () -> Unit = {}) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var history by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var isSending by remember { mutableStateOf(false) }

    // IMPROVEMENT 6: List "Recent Transfers" specific to this screen
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("transactions")
                .whereEqualTo("type", "Send")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10) // Only show the last 10 for performance
                .addSnapshotListener { snapshot, _ ->
                    history = snapshot?.documents?.mapNotNull {
                        it.toObject(TransactionRecord::class.java)
                    } ?: emptyList()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Money", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {

            // Money Out Branding (Red Accent)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEB5757).copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                        label = { Text("How much?") },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("KES ") },
                        enabled = !isSending,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEB5757),
                            focusedLabelColor = Color(0xFFEB5757)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Who is this for? / Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. John Doe, Rent") },
                        enabled = !isSending,
                        singleLine = true
                    )
                }
            }

            // IMPROVEMENT: Validation & Red Color (Point 2)
            Button(
                onClick = {
                    val valAmount = amount.toDoubleOrNull() ?: 0.0
                    if (userId.isNotEmpty() && valAmount > 0) {
                        isSending = true
                        val record = TransactionRecord(
                            title = if (note.isEmpty()) "Sent Money" else note,
                            amount = valAmount,
                            type = "Send",
                            isNegative = true,
                            timestamp = Timestamp.now()
                        )

                        db.collection("users").document(userId).collection("transactions").add(record)
                        db.collection("users").document(userId)
                            .update("balance", FieldValue.increment(-valAmount))
                            .addOnCompleteListener {
                                isSending = false
                                amount = ""; note = ""
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = amount.isNotEmpty() && amount != "0" && !isSending,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEB5757)), // RED
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Confirm Transfer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // RECENT TRANSFERS SECTION (Point 6)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Recent Send History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No recent transfers found.", color = Color.Gray)
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(history) { tx ->
                    val dateStr = tx.timestamp?.let {
                        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(it.toDate())
                    } ?: ""

                    ListItem(
                        headlineContent = { Text(tx.title, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(dateStr) },
                        trailingContent = {
                            Text(
                                "-KES ${String.format("%,.0f", tx.amount)}",
                                color = Color(0xFFEB5757),
                                fontWeight = FontWeight.ExtraBold
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SendMoneyPreview() {
    MaterialTheme {
        SendMoneyScreen()
    }
}