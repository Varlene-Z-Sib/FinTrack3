package com.example.fintrack3.models

import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.Date

@IgnoreExtraProperties
data class Transaction(
    var transactionId: String? = null, // Firestore doc ID
    val amount: Double = 0.0,
    val description: String = "",
    val image: String? = null,
    val date: Date? = null,
    val userId: String = "",
    val category: String = "",
    val type: String = "" // Added type property
)


