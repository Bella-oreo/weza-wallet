package com.example.wezawallet.screens.addmoney

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wezawallet.usermodel.TransactionRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneyScreen(onBack: () -> Unit = {}) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Get the dynamic User ID from the logged-in user
    val userId = auth.currentUser?.uid ?: ""

    var amount by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

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
            Text(
                text = "Enter Amount",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                label = { Text("KES Amount") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 1000") },
                prefix = { Text("KES ") },
                singleLine = true,
                enabled = !isSubmitting
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val value = amount.toDoubleOrNull() ?: 0.0
                    if (userId.isNotEmpty() && value > 0) {
                        isSubmitting = true

                        // 1. Update the main balance
                        db.collection("users").document(userId)
                            .update("balance", FieldValue.increment(value))

                        // 2. Create the Transaction Record for the "Recent Transactions" list
                        val record = TransactionRecord(
                            title = "Wallet Top-up",
                            amount = value,
                            type = "Add",
                            isNegative = false // This ensures it stays Green/Positive
                        )

                        db.collection("users").document(userId)
                            .collection("transactions")
                            .add(record)
                            .addOnSuccessListener {
                                isSubmitting = false
                                onBack() // Return to Home
                            }
                            .addOnFailureListener {
                                isSubmitting = false
                                // Optional: Handle error with a snackbar
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                enabled = amount.isNotEmpty() && !isSubmitting,
                shape = MaterialTheme.shapes.medium
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirm Top Up", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "The amount will be added to your balance immediately.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddMoneyPreview() {
    MaterialTheme {
        AddMoneyScreen()
    }
}