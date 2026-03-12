package com.example.wezawallet.screens.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.wezawallet.usermodel.User
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@Composable
fun HomeScreen(
    onExpenseClick: () -> Unit = {},
    onGoalClick: () -> Unit = {},
    onAddMoneyClick: () -> Unit = {},
    onSendMoneyClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var userProfile by remember { mutableStateOf<User?>(null) }
    val db = FirebaseFirestore.getInstance()

    // Fetching live data from your "bella_test" doc
    LaunchedEffect(Unit) {
        db.collection("users").document("bella_test")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    userProfile = snapshot.toObject(User::class.java)
                }
            }
    }

    val transactions = listOf(
        Transaction("Supermarket", "Today", 1200.0, true),
        Transaction("Transport", "Today", 300.0, true)
    )

    Scaffold(containerColor = Color(0xFFF5F6FA)) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HomeHeader(userName = userProfile?.name ?: "Loading...", onProfileClick = onProfileClick)
                Spacer(modifier = Modifier.height(20.dp))

                // Card with Horizontal Gradient & Divider
                BalanceCard(balance = userProfile?.balance?.toDouble() ?: 0.0, tokenPoints = 320)

                Spacer(modifier = Modifier.height(24.dp))

                // Color-coded Actions
                QuickActionsRow(onAddMoneyClick, onSendMoneyClick, onGoalClick, onExpenseClick)

                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader()
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(transactions.size) { index ->
                TransactionItem(transactions[index])
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                WeeklySpendingChart()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double, tokenPoints: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.horizontalGradient(listOf(Color(0xFF2F80ED), Color(0xFF56CCF2))))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "KES ${String.format(Locale.getDefault(), "%,.0f", balance)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                // The sleek divider from the target design
//                HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
//                Spacer(modifier = Modifier.height(12.dp))
//                Text(
//                    text = "Token Points : $tokenPoints",
//                    color = Color.White.copy(alpha = 0.9f),
//                    style = MaterialTheme.typography.bodyMedium
//                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(onAddMoneyClick: () -> Unit, onSendMoneyClick: () -> Unit, onGoalClick: () -> Unit, onExpenseClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        QuickActionItem("Add Money", Icons.Default.Add, Color(0xFFFFB800), onAddMoneyClick)
        QuickActionItem("Send", Icons.AutoMirrored.Filled.Send, Color(0xFF2F80ED), onSendMoneyClick)
        QuickActionItem("Goals", Icons.Default.Star, Color(0xFF27AE60), onGoalClick)
        QuickActionItem("Expenses", Icons.Default.CardGiftcard, Color(0xFFEB5757), onExpenseClick)
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
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Fixed the typo from image_dd269e.jpg here
                Box(modifier = Modifier.size(44.dp).background(Color(0xFFF0F2F8), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (transaction.title == "Transport") Icons.Default.DirectionsBus else Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color(0xFF2F80ED),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = transaction.title, fontWeight = FontWeight.Bold)
                    Text(text = transaction.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Text(
                text = "${if (transaction.isNegative) "-" else ""}KES ${String.format(Locale.getDefault(), "%,.0f", transaction.amount)}",
                color = if (transaction.isNegative) Color(0xFFEB5757) else Color(0xFF27AE60),
                fontWeight = FontWeight.ExtraBold
            )
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
                val bars = listOf(0.4f, 0.6f, 0.3f, 0.5f, 0.4f, 0.7f, 0.9f)
                bars.forEach { heightRatio ->
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxHeight(heightRatio)
                            .background(Color(0xFF2F80ED).copy(alpha = 0.6f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeader(userName: String, onProfileClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = "Home / Dashboard", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
            Text(text = "Hello, $userName!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = onProfileClick) {
            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Profile", modifier = Modifier.size(36.dp), tint = Color(0xFF2F80ED))
        }
    }
}

@Composable
fun SectionHeader() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Recent Transactions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(text = "View All", color = Color(0xFF2F80ED), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

data class Transaction(val title: String, val date: String, val amount: Double, val isNegative: Boolean)

/* ----------------------- PREVIEW ----------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        Box(modifier = Modifier.background(Color(0xFFF5F6FA)).fillMaxSize().padding(16.dp)) {
            Column {
                HomeHeader(userName = "Bella Ndirangu", onProfileClick = {})
                Spacer(modifier = Modifier.height(20.dp))
                BalanceCard(balance = 1000.0, tokenPoints = 320)
                Spacer(modifier = Modifier.height(24.dp))
                QuickActionsRow({}, {}, {}, {})
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader()
                TransactionItem(Transaction("Supermarket", "Today", 1200.0, true))
                WeeklySpendingChart()
            }
        }
    }
}