package com.example.fintrack3.models

data class Category(
    val categoryId: String = "", // Use Firestore auto-ID or custom ID
    val userId: String = "",
    val name: String = "",
    val type: String = "",
    val description: String = "",

)