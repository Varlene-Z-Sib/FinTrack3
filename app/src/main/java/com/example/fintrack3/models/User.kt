

package com.example.fintrack3.models

data class User(
    val userId: String = "", // Use UID from FirebaseAuth
    val email: String = "",
    val name: String = "",
    val monthlyMin: Double = 0.0,
    val monthlyMax: Double = 0.0
)
