package com.example.wezawallet.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wezawallet.usermodel.TransactionRecord
import com.example.wezawallet.usermodel.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onExpenseClick: () -> Unit = {},
    onGoalClick: () -> Unit = {},
    onAddMoneyClick: () -> Unit = {},
    onSendMoneyClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onViewAllClick: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var userProfile by remember { mutableStateOf<User?>(null) }
    var transactions by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            // Listen for User Profile & Balance
            db.collection("users").document(userId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        userProfile = snapshot.toObject(User::class.java)
                    }
                }

            // Listen for Recent Transactions (Limit 5)
            db.collection("users").document(userId).collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener { snapshot, _ ->
                    transactions = snapshot?.documents?.mapNotNull {
                        it.toObject(TransactionRecord::class.java)
                    } ?: emptyList()
                }
        }
    }

    Scaffold(containerColor = Color(0xFFF5F6FA)) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HomeHeader(userName = userProfile?.name ?: "User", onProfileClick = onProfileClick)
                Spacer(modifier = Modifier.height(20.dp))

                BalanceCard(balance = userProfile?.balance ?: 0.0)

                Spacer(modifier = Modifier.height(24.dp))
                QuickActionsRow(onAddMoneyClick, onSendMoneyClick, onGoalClick, onExpenseClick)
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Recent Transactions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "View All",
                        color = Color(0xFF2F80ED),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onViewAllClick() }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(transactions) { tx ->
                // Null-safe check for the Firestore Timestamp
                val dateStr = tx.timestamp?.let {
                    SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(it.toDate())
                } ?: "Syncing..."

                TransactionItem(
                    title = tx.title,
                    date = dateStr,
                    amount = tx.amount,
                    type = tx.type,
                    isNegative = tx.isNegative
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        "No recent transactions",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                WeeklySpendingChart()
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/* ----------------------- UI COMPONENTS ----------------------- */

@Composable
fun BalanceCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.horizontalGradient(listOf(Color(0xFF009688), Color(0xFF26A69A))))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "KES ${String.format(Locale.getDefault(), "%,.0f", balance)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Available Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun TransactionItem(title: String, date: String, amount: Double, type: String, isNegative: Boolean) {
    val icon = when {
        type == "Add" -> Icons.Default.AccountBalanceWallet
        type == "Goal" -> Icons.Default.Star
        title.contains("Rent", true) -> Icons.Default.Home
        title.contains("Food", true) || title.contains("Lunch", true) -> Icons.Default.Restaurant
        else -> Icons.Default.Payments
    }

    val statusColor = if (isNegative) Color(0xFFEB5757) else Color(0xFF27AE60)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = statusColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text(text = date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Text(
                text = "${if (isNegative) "-" else "+"} KES ${String.format(Locale.getDefault(), "%,.0f", amount)}",
                color = statusColor,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun QuickActionsRow(onAdd: () -> Unit, onSend: () -> Unit, onGoal: () -> Unit, onExpense: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        QuickActionItem("Add Money", Icons.Default.Add, Color(0xFFFFB800), onAdd)
        QuickActionItem("Send", Icons.AutoMirrored.Filled.Send, Color(0xFF2F80ED), onSend)
        QuickActionItem("Goals", Icons.Default.Star, Color(0xFF27AE60), onGoal)
        QuickActionItem("Expenses", Icons.Default.CardGiftcard, Color(0xFFEB5757), onExpense)
    }
}

@Composable
fun QuickActionItem(title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Card(
            modifier = Modifier.size(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = color)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun HomeHeader(userName: String, onProfileClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = "WezaWallet Dashboard", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Text(text = "Hello, $userName!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onProfileClick) {
            Icon(Icons.Default.AccountCircle, "Profile", modifier = Modifier.size(36.dp), tint = Color(0xFF2F80ED))
        }
    }
}

@Composable
fun WeeklySpendingChart() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Weekly Spending", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                listOf(0.4f, 0.6f, 0.3f, 0.5f, 0.4f, 0.7f, 0.9f).forEach { height ->
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxHeight(height)
                            .background(Color(0xFF2F80ED).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    )
                }
            }
        }
    }
}

/* ----------------------- PREVIEW ----------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}