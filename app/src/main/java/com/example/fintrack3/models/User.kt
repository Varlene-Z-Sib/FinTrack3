

package com.example.fintrack3.models

data class User(
    val userId: String = "", // Use UID from FirebaseAuth
    val email: String = "",
    val name: String = "",

)
