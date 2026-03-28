package com.example.wezawallet.screens.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wezawallet.usermodel.TransactionRecord
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class SavingsGoal(
    val id: String = "",
    val name: String = "",
    val currentAmount: Double = 0.0,
    val targetAmount: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(onBack: () -> Unit = {}) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var goals by remember { mutableStateOf<List<SavingsGoal>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("goals")
                .addSnapshotListener { snapshot, _ ->
                    goals = snapshot?.documents?.mapNotNull {
                        it.toObject(SavingsGoal::class.java)?.copy(id = it.id)
                    } ?: emptyList()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Savings Goals", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            if (goals.isEmpty()) {
                Text(
                    "No goals yet. Start saving for something big!",
                    modifier = Modifier.padding(vertical = 20.dp),
                    color = Color.Gray
                )
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(goals) { goal ->
                    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(goal.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "KES ${goal.currentAmount.toInt()} / ${goal.targetAmount.toInt()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Add New Goal", fontWeight = FontWeight.Bold)
            }
        }

        if (showDialog) {
            AddGoalDialog(
                onDismiss = { showDialog = false },
                onConfirm = { name, target ->
                    if (userId.isNotEmpty() && name.isNotEmpty()) {
                        val targetVal = target.toDoubleOrNull() ?: 0.0

                        // 1. Save Goal
                        val goalData = mapOf(
                            "name" to name,
                            "targetAmount" to targetVal,
                            "currentAmount" to 0.0
                        )
                        db.collection("users").document(userId).collection("goals").add(goalData)

                        // 2. Record in Transactions with Timestamp
                        val record = TransactionRecord(
                            title = "New Goal: $name",
                            amount = targetVal,
                            type = "Goal",
                            isNegative = false,
                            timestamp = Timestamp.now() // Added for sorting
                        )
                        db.collection("users").document(userId).collection("transactions").add(record)

                        showDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Savings Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("What are you saving for?") },
                    placeholder = { Text("e.g. New Laptop") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { if (it.all { char -> char.isDigit() }) target = it },
                    label = { Text("Target Amount (KES)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, target) }) { Text("Create Goal") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GoalScreenPreview() {
    MaterialTheme { GoalScreen() }
}