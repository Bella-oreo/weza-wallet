package com.example.wezawallet.usermodel

data class User(
    val name: String = "",
    val email: String = "",
    val balance: Double = 0.0,
    val profilePic: String = ""
)