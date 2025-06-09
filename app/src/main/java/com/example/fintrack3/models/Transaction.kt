package com.example.fintrack3.models

import java.util.*

data class Transaction(
    val transactionId: String = "", // Firestore auto-ID or custom
    val amount: Double = 0.0,
    val description: String = "",
    val image: String? = null,
    val date: Date = Date(),
    val userId: String = "",
    val category: String = ""
)