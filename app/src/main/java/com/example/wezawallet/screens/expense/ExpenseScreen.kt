package com.example.wezawallet.screens.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Add
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
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

    // POINT 2: Expense Red Branding
    val expenseRed = Color(0xFFEB5757)

    var expenseCategories by remember { mutableStateOf<List<ExpenseCategory>>(emptyList()) }
    var recentPaidExpenses by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    // Form states
    var nameInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }

    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            // 1. Listen for Active Expense Plans (Current obligations)
            db.collection("users").document(userId).collection("expenses")
                .addSnapshotListener { snapshot, _ ->
                    expenseCategories = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(ExpenseCategory::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                }

            // 2. POINT 6: Listen for Recently Paid Expenses (Filtered History)
            db.collection("users").document(userId).collection("transactions")
                .whereEqualTo("type", "Expense")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(8) // Show up to 8 recent expenses
                .addSnapshotListener { snapshot, _ ->
                    recentPaidExpenses = snapshot?.documents?.mapNotNull {
                        it.toObject(TransactionRecord::class.java)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Monthly Obligations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Check off items to deduct from balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (expenseCategories.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                            Text("No pending expenses.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }

                items(expenseCategories) { expense ->
                    val dueDay = expense.dueDate.filter { it.isDigit() }.toIntOrNull() ?: 0
                    val isOverdue = dueDay != 0 && today > dueDay

                    // Card with Red-tint if Overdue
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOverdue) Color(0xFFFFEBEE) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("KES ${String.format("%,.0f", expense.amount)}", color = Color.Gray)

                                if (expense.dueDate.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isOverdue) Icon(
                                            Icons.Default.NotificationsActive,
                                            null,
                                            tint = Color.Red,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = " Due Day: ${expense.dueDate}${if (isOverdue) " (OVERDUE)" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isOverdue) Color.Red else Color(0xFF2F80ED),
                                            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            // Marking as Paid
                            Checkbox(
                                checked = false,
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF27AE60)),
                                onCheckedChange = { isChecked ->
                                    if (isChecked && userId.isNotEmpty()) {
                                        val record = TransactionRecord(
                                            title = "Paid: ${expense.name}",
                                            amount = expense.amount,
                                            type = "Expense",
                                            isNegative = true,
                                            timestamp = Timestamp.now()
                                        )

                                        // Update Firestore
                                        db.collection("users").document(userId).collection("transactions").add(record)
                                        db.collection("users").document(userId).update("balance", FieldValue.increment(-expense.amount))
                                        db.collection("users").document(userId).collection("expenses").document(expense.id).delete()

                                        scope.launch {
                                            snackbarHostState.showSnackbar("Payment Recorded & Balance Updated!")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                // POINT 6: Recent Paid Section
                if (recentPaidExpenses.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Recent Spending History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(recentPaidExpenses) { tx ->
                        ListItem(
                            headlineContent = { Text(tx.title, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                            supportingContent = {
                                Text(
                                    text = tx.timestamp?.toDate()?.let {
                                        java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault()).format(it)
                                    } ?: "",
                                    fontSize = 12.sp
                                )
                            },
                            trailingContent = {
                                // POINT 2: RED for money going out
                                Text(
                                    "-KES ${String.format("%,.0f", tx.amount)}",
                                    color = expenseRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }

            // RED Branding Action Button
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = expenseRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Plan New Expense", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Dialog for adding new expense plans
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Monthly Plan", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Expense Name") },
                            placeholder = { Text("e.g. Rent, Electricity") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { if (it.all { c -> c.isDigit() }) amountInput = it },
                            label = { Text("Amount KES") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { dateInput = it },
                            label = { Text("Due Day (1-31)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = expenseRed),
                        onClick = {
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
    Surface(color = MaterialTheme.colorScheme.background) {
        ExpenseScreen()
    }
}