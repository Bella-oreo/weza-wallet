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
    val userId = auth.currentUser?.uid ?: ""

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var expenseCategories by remember { mutableStateOf<List<ExpenseCategory>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    // Form states
    var nameInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }

    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

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
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    items(expenseCategories) { expense ->
                        val dueDay = expense.dueDate.filter { it.isDigit() }.toIntOrNull() ?: 0
                        val isOverdue = dueDay != 0 && today > dueDay

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOverdue) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
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

                                Checkbox(
                                    checked = false,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked && userId.isNotEmpty()) {
                                            // Create record with Timestamp for proper dashboard sorting
                                            val record = TransactionRecord(
                                                title = "Paid: ${expense.name}",
                                                amount = expense.amount,
                                                type = "Expense",
                                                isNegative = true,
                                                timestamp = Timestamp.now()
                                            )

                                            db.collection("users").document(userId)
                                                .collection("transactions").add(record)

                                            db.collection("users").document(userId)
                                                .update("balance", FieldValue.increment(-expense.amount))

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

                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(55.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("+ Plan New Expense", fontWeight = FontWeight.Bold)
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
                    TextButton(onClick = {
                        if (userId.isNotEmpty() && nameInput.isNotEmpty()) {
                            val data = mapOf(
                                "name" to nameInput,
                                "amount" to (amountInput.toDoubleOrNull() ?: 0.0),
                                "dueDate" to dateInput
                            )
                            db.collection("users").document(userId).collection("expenses").add(data)
                            nameInput = ""; amountInput = ""; dateInput = ""; showDialog = false
                        }
                    }) { Text("Save Plan") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ExpensePreview() {
    MaterialTheme {
        ExpenseScreen()
    }
}