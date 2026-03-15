package com.example.wezawallet.screens.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wezawallet.usermodel.TransactionRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.util.Calendar

// Data model for Firestore mapping
data class ExpenseCategory(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val dueDate: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(onBack: () -> Unit = {}) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: "" // Dynamic User ID

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var expenseCategories by remember { mutableStateOf<List<ExpenseCategory>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    // Form states
    var nameInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }

    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    // Listen for changes in the expenses collection using dynamic userId
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).collection("expenses")
                .addSnapshotListener { snapshot, _ ->
                    expenseCategories = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(ExpenseCategory::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Expense Planner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(55.dp)
            ) { Text("+ Plan New Expense") }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(expenseCategories) { expense ->
                val dueDay = expense.dueDate.filter { it.isDigit() }.toIntOrNull() ?: 0
                val isOverdue = dueDay != 0 && today > dueDay

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(expense.name, fontWeight = FontWeight.Bold)
                            Text("KES ${expense.amount}", color = Color.Gray)
                            if (expense.dueDate.isNotEmpty()) {
                                Text(
                                    text = "Due Day: ${expense.dueDate}${if (isOverdue) " (OVERDUE)" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isOverdue) Color.Red else MaterialTheme.colorScheme.primary,
                                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        // Checkbox triggers the payment automation
                        Checkbox(
                            checked = false,
                            onCheckedChange = { isChecked ->
                                if (isChecked && userId.isNotEmpty()) {
                                    // 1. Create a TransactionRecord for the Home Dashboard
                                    val record = TransactionRecord(
                                        title = "Paid: ${expense.name}",
                                        amount = expense.amount,
                                        type = "Expense",
                                        isNegative = true // Shows as red/minus on dashboard
                                    )

                                    // 2. Save to Transactions
                                    db.collection("users").document(userId)
                                        .collection("transactions").add(record)

                                    // 3. Deduct from Balance
                                    db.collection("users").document(userId)
                                        .update("balance", FieldValue.increment(-expense.amount))

                                    // 4. Delete from Planner list
                                    db.collection("users").document(userId)
                                        .collection("expenses").document(expense.id).delete()

                                    scope.launch {
                                        snackbarHostState.showSnackbar("Payment Recorded & Balance Updated!")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Monthly Expense") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Name (e.g. Rent)") })
                        OutlinedTextField(value = amountInput, onValueChange = { amountInput = it }, label = { Text("Amount KES") })
                        OutlinedTextField(value = dateInput, onValueChange = { dateInput = it }, label = { Text("Due Day (e.g. 5)") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (userId.isNotEmpty()) {
                            val data = mapOf(
                                "name" to nameInput,
                                "amount" to (amountInput.toDoubleOrNull() ?: 0.0),
                                "dueDate" to dateInput
                            )
                            db.collection("users").document(userId).collection("expenses").add(data)
                            nameInput = ""; amountInput = ""; dateInput = ""; showDialog = false
                        }
                    }) { Text("Save Plan") }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpensePreview() {
    MaterialTheme {
        ExpenseScreen()
    }
}