package com.example.wezawallet.screens.sendmoney

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.Timestamp

@Composable
fun SendMoneyScreen(onBack: () -> Unit = {}) { // FIXED parameter name
    val db = FirebaseFirestore.getInstance()
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var history by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("users").document("bella_test").collection("transactions")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                history = snapshot?.documents?.map { it.data ?: emptyMap() } ?: emptyList()
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }

        OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("KES Amount") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("What for? (e.g. rent)") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val valAmount = amount.toDoubleOrNull() ?: 0.0
                val data: Map<String, Any> = mapOf("amount" to valAmount, "note" to note, "timestamp" to Timestamp.now())
                db.collection("users").document("bella_test").collection("transactions").add(data)
                // Subtract from balance
                db.collection("users").document("bella_test").update("balance", FieldValue.increment(-valAmount))
                amount = ""; note = ""
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) { Text("Send & Save Entity") }

        Text("Transaction History", modifier = Modifier.padding(bottom = 8.dp))
        LazyColumn {
            items(history) { tx ->
                ListItem(headlineContent = { Text("${tx["note"]} - KES ${tx["amount"]}") })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SendMoneyPreview() { MaterialTheme { SendMoneyScreen() } }