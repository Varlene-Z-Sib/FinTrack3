package com.example.fintrack3.models

data class Budget(
    val goal: Double = 0.0,
    val limit: Double = 0.0,
    val period: String = "Month"
)
