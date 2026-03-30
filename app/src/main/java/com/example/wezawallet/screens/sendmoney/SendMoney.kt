package com.example.wezawallet.screens.sendmoney

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
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

    // POINT 2: Money Out Red Branding
    val moneyOutRed = Color(0xFFEB5757)

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var history by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var isSending by remember { mutableStateOf(false) }

    // POINT 6: Real-time Listener for "Send" activity
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("transactions")
                .whereEqualTo("type", "Send")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
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

            // Input Card with light red tint to signal "Debit" action
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = moneyOutRed.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Transfer Details",
                        style = MaterialTheme.typography.labelLarge,
                        color = moneyOutRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("KES ") },
                        enabled = !isSending,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = moneyOutRed,
                            focusedLabelColor = moneyOutRed
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Recipient / Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. John Doe, Lunch") },
                        enabled = !isSending,
                        singleLine = true
                    )
                }
            }

            // POINT 2: Primary Action Button in Red
            Button(
                onClick = {
                    val valAmount = amount.toDoubleOrNull() ?: 0.0
                    if (userId.isNotEmpty() && valAmount > 0) {
                        isSending = true
                        val record = TransactionRecord(
                            title = if (note.isEmpty()) "Sent Money" else "To: $note",
                            amount = valAmount,
                            type = "Send",
                            isNegative = true,
                            timestamp = Timestamp.now()
                        )

                        // Transaction Write -> Then Balance Update
                        db.collection("users").document(userId).collection("transactions").add(record)
                            .addOnSuccessListener {
                                db.collection("users").document(userId)
                                    .update("balance", FieldValue.increment(-valAmount))
                                    .addOnSuccessListener {
                                        isSending = false
                                        amount = ""; note = ""
                                    }
                            }
                            .addOnFailureListener { isSending = false }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = amount.isNotEmpty() && amount != "0" && !isSending,
                colors = ButtonDefaults.buttonColors(containerColor = moneyOutRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.Send, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Transfer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // POINT 6: Recent Send History
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Recent Transfers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No recent transfers found.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
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
                                    color = moneyOutRed,
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SendMoneyPreview() {
    Surface(color = MaterialTheme.colorScheme.background) {
        SendMoneyScreen()
    }
}