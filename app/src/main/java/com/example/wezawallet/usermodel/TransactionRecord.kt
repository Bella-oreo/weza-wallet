package com.example.wezawallet.usermodel


import com.google.firebase.Timestamp

data class TransactionRecord(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "", // e.g., "Add", "Send", "Expense", "Goal"
    val timestamp: Timestamp = Timestamp.now(),
    val isNegative: Boolean = true
)