package com.example.wezawallet.usermodel

data class User(
    val name: String = "",
    val balance: Int = 0,
    val currency: String = "KES"
)