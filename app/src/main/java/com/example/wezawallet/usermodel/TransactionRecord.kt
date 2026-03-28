package com.example.wezawallet.usermodel

import com.google.firebase.Timestamp

data class TransactionRecord(
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "",
    val isNegative: Boolean = false,
    val timestamp: Timestamp? = null
)