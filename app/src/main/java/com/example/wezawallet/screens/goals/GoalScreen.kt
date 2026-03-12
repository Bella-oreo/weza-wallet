package com.example.wezawallet.screens.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

data class SavingsGoal(val id: String = "", val name: String = "", val currentAmount: Double = 0.0, val targetAmount: Double = 0.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(onBack: () -> Unit = {}) {
    val db = FirebaseFirestore.getInstance()
    var goals by remember { mutableStateOf<List<SavingsGoal>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    // Live sync with Firestore
    LaunchedEffect(Unit) {
        db.collection("users").document("bella_test").collection("goals")
            .addSnapshotListener { snapshot, _ ->
                goals = snapshot?.documents?.mapNotNull {
                    it.toObject(SavingsGoal::class.java)?.copy(id = it.id)
                } ?: emptyList()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Savings Goals", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (goals.isEmpty()) {
                Text("No goals yet. Add one below!", modifier = Modifier.padding(bottom = 16.dp))
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(goals) { goal ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(goal.name, fontWeight = FontWeight.Bold)
                            Text("KES ${goal.currentAmount} / ${goal.targetAmount}")
                        }
                    }
                }
            }
            Button(onClick = { showDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Add New Goal")
            }
        }

        if (showDialog) {
            AddGoalDialog(onDismiss = { showDialog = false }, onConfirm = { name, target ->
                val data: Map<String, Any> = mapOf(
                    "name" to name,
                    "targetAmount" to (target.toDoubleOrNull() ?: 0.0),
                    "currentAmount" to 0.0
                )
                db.collection("users").document("bella_test").collection("goals").add(data)
                showDialog = false
            })
        }
    }
}

@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Goal") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Goal Name") })
                OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("Target Amount") })
            }
        },
        confirmButton = { Button(onClick = { onConfirm(name, target) }) { Text("Add") } }
    )
}

@Preview(showBackground = true)
@Composable
fun GoalScreenPreview() { MaterialTheme { GoalScreen() } }