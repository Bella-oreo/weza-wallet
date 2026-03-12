package com.example.wezawallet.screens.addmoney

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

@Composable
fun AddMoneyScreen(onBack: () -> Unit = {}) { // FIXED parameter name
    val db = FirebaseFirestore.getInstance()
    var amount by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }

        Text("Top Up Balance", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount KES") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val value = amount.toDoubleOrNull() ?: 0.0
                // Increment the balance field in Firestore
                db.collection("users").document("bella_test").update("balance", FieldValue.increment(value))
                onBack()
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) { Text("Confirm Add") }
    }
}

@Preview(showBackground = true)
@Composable
fun AddMoneyPreview() { MaterialTheme { AddMoneyScreen() } }
