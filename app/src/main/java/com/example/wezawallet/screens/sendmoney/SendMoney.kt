package com.example.wezawallet.screens.sendmoney

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wezawallet.usermodel.TransactionRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query

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

    // Sync history for the specific logged-in user
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("transactions")
                .whereEqualTo("type", "Send") // Only show "Send" type in this screen's local history
                .orderBy("timestamp", Query.Direction.DESCENDING)
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

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                label = { Text("Amount KES") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("KES ") },
                enabled = !isSending
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Reason / Reference") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Rent, Dinner") },
                enabled = !isSending
            )

            Button(
                onClick = {
                    val valAmount = amount.toDoubleOrNull() ?: 0.0
                    if (userId.isNotEmpty() && valAmount > 0) {
                        isSending = true

                        // 1. Create TransactionRecord
                        val record = TransactionRecord(
                            title = if (note.isEmpty()) "Sent Money" else note,
                            amount = valAmount,
                            type = "Send",
                            isNegative = true
                        )

                        // 2. Save to Transactions sub-collection
                        db.collection("users").document(userId)
                            .collection("transactions").add(record)

                        // 3. Deduct from Balance
                        db.collection("users").document(userId)
                            .update("balance", FieldValue.increment(-valAmount))
                            .addOnCompleteListener {
                                isSending = false
                                amount = ""; note = ""
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                enabled = amount.isNotEmpty() && !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color =
                        Color.White)
                } else {
                    Text("Confirm Transfer")
                }
            }

            Text(
                "Recent Transfers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(history) { tx ->
                    ListItem(
                        headlineContent = { Text(tx.title) },
                        supportingContent = { Text("Today") },
                        trailingContent = {
                            Text("-KES ${tx.amount}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SendMoneyPreview() {
    MaterialTheme {
        SendMoneyScreen()
    }
}